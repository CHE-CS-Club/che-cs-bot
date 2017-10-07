package club.checs.csbot.managers;

import club.checs.csbot.commands.SmartCommand;
import com.vdurmont.emoji.EmojiManager;
import sx.blah.discord.api.IDiscordClient;
import sx.blah.discord.api.events.EventSubscriber;
import sx.blah.discord.api.internal.json.objects.EmbedObject;
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent;
import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.handle.obj.IMessage;
import sx.blah.discord.handle.obj.IUser;
import sx.blah.discord.util.*;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public class CommandManager {
    private IDiscordClient client;
    private HashMap<String, SmartCommand> commands = new HashMap<>();
    private PermManager pmanager;
    private HashMap<IUser, Long> lastMessage = new HashMap<>();
    private HashMap<IUser, Integer> offenseCount = new HashMap<>();

    public CommandManager(IDiscordClient client, PermManager pmanager) {
        this.client = client;
        this.pmanager = pmanager;
    }

    @EventSubscriber
    public void onMesageEvent(MessageReceivedEvent event) throws DiscordException, MissingPermissionsException,
            RateLimitException {
        IMessage message = event.getMessage(); // Get the message

        // Check to make sure this user isn't spamming
        if (lastMessage.containsKey(message.getAuthor())) {
            // If last message was sent within 3 seconds
            if (System.currentTimeMillis() - lastMessage.get(message.getAuthor()) < 3.0 * 1000) {
                offenseCount.putIfAbsent(message.getAuthor(), 0);
                offenseCount.put(message.getAuthor(), offenseCount.get(message.getAuthor()) + 1);
                // TODO Clean this up with some methods for the reactions
                if (offenseCount.get(message.getAuthor()) == 3 || offenseCount.get(message.getAuthor()) == 4) {
                    RequestBuffer.request(() -> message.addReaction(offenseCount.get(message.getAuthor()) == 4 ?
                            EmojiManager.getForAlias("rotating_light") : EmojiManager.getForAlias("warning")));
                    new Thread(() -> {
                        try {
                            Thread.sleep(3000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        } finally {
                            RequestBuffer.request(message::removeAllReactions);
                        }
                    }).start();
                }
                if (offenseCount.get(message.getAuthor()) >= 5) {
                    message.addReaction(EmojiManager.getForAlias("skull"));
                    if (offenseCount.get(message.getAuthor()) > 5)
                        message.delete();
                    new Thread(() -> {
                        try {
                            Thread.sleep(500);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        } finally {
                            pmanager.mute(message.getAuthor());
                        }
                    }).start();
                    sendMessage(message.getAuthor().mention() + " has been automatically muted for " +
                            "5 minutes for spamming.", message.getChannel());
                    new Thread(() -> {
                        final long mutetime = System.currentTimeMillis();
                        while (true) {
                            if (System.currentTimeMillis() - mutetime > 5 * 60 * 1000) {
                                pmanager.unmute(message.getAuthor());
                                sendMessage(message.getAuthor().mention() + " has been unmuted after " +
                                        "spamming.", message.getChannel());
                                return;
                            }
                            try {
                                Thread.sleep(3000);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                                pmanager.unmute(message.getAuthor());
                                return;
                            }
                        }
                    }).start();
                }
            } else {
                offenseCount.remove(message.getAuthor());
                lastMessage.remove(message.getAuthor());
            }
        }
        lastMessage.put(message.getAuthor(), System.currentTimeMillis());

        // If the user is muted, delete message and bail processing
        if (pmanager.isMuted(message.getAuthor())) {
            message.delete();
            return;
        }

        // Check commands and see if we have a match
        for (Map.Entry<String, SmartCommand> set : commands.entrySet()) {
            // If we have a match....
            if (message.getContent().split(" ")[0].toLowerCase().equals("!" + set.getKey().toLowerCase())) {
                // Split message into spaces for processing
                String[] argsL = message.getContent().split("\\s");
                // Remove first arg (it's the command)
                String[] args = new String[argsL.length - 1];
                System.arraycopy(argsL, 1, args, 0, argsL.length - 1);
                // Call the command
                set.getValue().onCommand(this, event, event.getMessage().getAuthor(), set.getKey(),
                        set.getKey(), args);
                // No need to check other commands now
                break;
            }
        }
    }

    public SmartCommand getCommand(String command) {
        return commands.get(command.toLowerCase());
    }

    public void addCommand(String command, SmartCommand smartCommand) {
        commands.put(command, smartCommand);
    }

    public CompletableFuture<IMessage> sendMessage(String message, MessageReceivedEvent event) throws DiscordException,
            MissingPermissionsException, RateLimitException {
        return sendMessage(message, event.getChannel());
    }

    public CompletableFuture<IMessage> sendMessage(String message, IChannel channel) {
        CompletableFuture<IMessage> completableFuture = new CompletableFuture<>();
        RequestBuffer.request(() -> {
            completableFuture.complete(channel.sendMessage(message));
        });
        return completableFuture;
    }

    public CompletableFuture<IMessage> sendEmbedMessage(String message, MessageReceivedEvent event, EmbedObject obj)
            throws DiscordException, MissingPermissionsException, RateLimitException {
        CompletableFuture<IMessage> completableFuture = new CompletableFuture<>();
        RequestBuffer.request(() -> {
            completableFuture.complete(new MessageBuilder(client).appendContent(message).withChannel(event.getMessage()
                    .getChannel()).withEmbed(obj).build());
        });
        return completableFuture;
    }

    public HashMap<String, SmartCommand> getCommands() {
        return commands;
    }
}
