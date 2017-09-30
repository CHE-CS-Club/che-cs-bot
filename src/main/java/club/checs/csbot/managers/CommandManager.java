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
    public void onMesageEvent(MessageReceivedEvent event) throws DiscordException, MissingPermissionsException, RateLimitException {
        IMessage message = event.getMessage(); // Get the message
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
                    sendMessage(message.getAuthor().mention() + " has been automatically muted for 5 minutes for spamming.", message.getChannel());
                    new Thread(() -> {
                        final long mutetime = System.currentTimeMillis();
                        while (true) {
                            if (System.currentTimeMillis() - mutetime > 5 * 60 * 1000) {
                                pmanager.unmute(message.getAuthor());
                                sendMessage(message.getAuthor().mention() + " has been unmuted after spamming.", message.getChannel());
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
        if (!pmanager.isMuted(message.getAuthor())) {
            for (Map.Entry<String, SmartCommand> set : commands.entrySet()) {
                // TODO Split and compare with equalsIgnoreCase the 0th index to prevent duplicate matches
                if (message.getContent().split(" ")[0].toLowerCase().equals("!" + set.getKey().toLowerCase())) {
                    String[] argsL = message.getContent().split("\\s");
                    String[] args = new String[argsL.length - 1];
                    System.arraycopy(argsL, 1, args, 0, argsL.length - 1);
                    set.getValue().onCommand(this, event, event.getMessage().getAuthor(), set.getKey(), set.getKey(), args);
                }
            }
        } else {
            // TODO Up a counter and kick them if they keep sending messages
            message.delete();
        }
    }

    public void addCommand(String command, SmartCommand smartCommand) {
        commands.put(command, smartCommand);
    }

    public void sendMessage(String message, MessageReceivedEvent event) throws DiscordException, MissingPermissionsException, RateLimitException {
        sendMessage(message, event.getChannel());
    }

    public void sendMessage(String message, IChannel channel) {
        RequestBuffer.request(() -> {
            channel.sendMessage(message);
        });
    }

    public void sendEmbedMessage(String message, MessageReceivedEvent event, EmbedObject obj) throws DiscordException, MissingPermissionsException, RateLimitException {
        new MessageBuilder(client).appendContent(message).withChannel(event.getMessage().getChannel()).withEmbed(obj).build();
    }

    public HashMap<String, SmartCommand> getCommands() {
        return commands;
    }
}
