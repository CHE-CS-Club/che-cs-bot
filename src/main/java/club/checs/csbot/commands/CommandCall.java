package club.checs.csbot.commands;

import club.checs.csbot.commands.arguments.CommandArg;
import club.checs.csbot.managers.CommandManager;
import sx.blah.discord.api.internal.json.objects.EmbedObject;
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent;
import sx.blah.discord.handle.obj.IMessage;
import sx.blah.discord.handle.obj.IUser;
import sx.blah.discord.util.DiscordException;
import sx.blah.discord.util.MissingPermissionsException;
import sx.blah.discord.util.RateLimitException;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

// TODO Javadoc
public class CommandCall {
    private final IUser sender;
    private final String alias;
    private final LinkedHashMap<String, CommandArg> smartArgs;
    private final HashMap<String, Object> argResults = new HashMap<>();
    private final String[] args;
    private final CommandManager manager;
    private final MessageReceivedEvent event;
    private final SmartCommand superCommand;

    public CommandCall(CommandManager manager, MessageReceivedEvent event, IUser sender, String cmd, String alias, String[] args, LinkedHashMap<String,
            CommandArg> smartArgs, SmartCommand superCommand) {
        this.smartArgs = smartArgs;
        this.sender = sender;
        this.alias = alias;
        this.args = args;
        this.manager = manager;
        this.event = event;
        this.superCommand = superCommand;

        for (CommandArg arg : smartArgs.values()) {
            if (arg.hasDefault())
                argResults.put(arg.getName(), arg.getDefault());
        }
    }

    public IUser getSender() {
        return sender;
    }

    public CommandManager getManager() {
        return manager;
    }

    public MessageReceivedEvent getEvent() {
        return event;
    }

    /**
     * Shit is this a messy method, but it's gotta be what it's gotta be. This method loops the arguments, and <\br>
     * and determines whether or not we have the right stuff to run the command, and if so sets the values of the
     * args.
     *
     * @return Whether or not we're good to run
     */
    public boolean processArgs() {
        String[] textArgs = args.clone(); // Make sure we don't kill old args

        for (Map.Entry<String, CommandArg> argSet : smartArgs.entrySet()) {
            CommandArg arg = argSet.getValue();

            // First, let's see if we even have valid input
            if (!arg.hasValidInput(textArgs)) { // If we don't have correct arguments
                // NOTE: This is the same as below but can't be methodized due to returns and continues
                if (!arg.isContinueIfMissing()) { // And if we don't want to continue
                    if (!arg.isOptional()) { // And if this wasn't optional, tell the person
                        try {
                            manager.sendMessage(sender.mention
                                    (true) + " you're missing  or have invalid input for " +
                                    "the argument `" + argSet.getKey() + "`. Please include it to run the command." +
                                    "\nProper usage: " + superCommand.getHelpText(arg.getName()), event);
                        } catch (DiscordException | MissingPermissionsException | RateLimitException e) {
                            e.printStackTrace();
                        }
                        return false;
                    }
                    return true; // But if it was optional, just say we can run
                } else { // But if we do want to continue
                    continue; // Check the next arg
                }
                // END NOTE
            }

            // Okay, everything checks out and we can use this. Let's process the argument and move on
            textArgs = arg.processArgs(textArgs, argResults);
        }

        return true;
    }

    public String getAlias() {
        return alias;
    }

    public boolean hasArg(String arg) {
        return argResults.containsKey(arg);
    }

    public Object getArg(String arg) {
        return argResults.get(arg);
    }

    public CompletableFuture<IMessage> sendMessage(String message) {
        // TODO Output better missing args error
        try {
            return manager.sendMessage(message, event);
        } catch (DiscordException | MissingPermissionsException | RateLimitException e) {
            e.printStackTrace();
        }
        return null;
    }

    public CompletableFuture<IMessage> sendEmbedMessage(String message, EmbedObject obj) {
        // TODO Output better missing args error
        try {
            return manager.sendEmbedMessage(message, event, obj);
        } catch (DiscordException | MissingPermissionsException | RateLimitException e) {
            e.printStackTrace();
        }
        return null;
    }

    public void deleteMessage() {
        try {
            event.getMessage().delete();
        } catch (MissingPermissionsException | RateLimitException | DiscordException e) {
            e.printStackTrace();
        }
    }
}
