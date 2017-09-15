package club.checs.csbot.commands;

import club.checs.csbot.CommandManager;
import club.checs.csbot.commands.arguments.CommandArg;
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent;
import sx.blah.discord.handle.obj.IUser;
import sx.blah.discord.util.EmbedBuilder;

import java.awt.*;
import java.util.LinkedHashMap;

// TODO Add in aliases
// TODO Set descriptions and stuff
// TODO Javadoc
public class SmartCommand {
    private final String command;
    private final LinkedHashMap<String, CommandArg> smartArgs = new LinkedHashMap<String, CommandArg>();
    private String description;
    private OnCommand commandInterface = this::onCommand;

    public SmartCommand(String command) {
        this.command = command;
    }

    public SmartCommand(String command, OnCommand commandInterface) {
        this(command);
        this.commandInterface = commandInterface;
    }

    public SmartCommand addArgument(CommandArg arg) {
        smartArgs.put(arg.getName(), arg);
        return this; // For a builder type interaction
    }

    public SmartCommand addArgs(CommandArg... args) {
        for (CommandArg arg : args)
            addArgument(arg);
        return this;
    }

    // Should be overwritten
    public void onCommand(CommandCall call) {
    }

    public boolean onCommand(CommandManager manager, MessageReceivedEvent event, IUser sender, String cmd, String alias,
                             String[] args) {
        CommandCall call = new CommandCall(manager, event, sender, cmd, alias, args, smartArgs);

        // Alright, we want to call this. Process args.
        boolean argSuccess = call.processArgs(); // Check to make sure we're good on args
        if (!argSuccess) // If args didn't process successfully
            return true; // Just return. Arg processing should have messaged the sender
        try {
            commandInterface.onCommand(call);
        } catch (Exception e) {
            call.sendEmbedMessage("", new EmbedBuilder().withColor(Color.red)
                    .withImage("http://i.imgur.com/ytXXWJ1.png").withDescription("Uh oh, something went wrong...\n`" +
                            e.getMessage() + "`").build());
            e.printStackTrace();
        }
        return true;
    }

    public String getCommand() {
        return command;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public CommandArg getArgObject(String arg) {
        return smartArgs.get(arg);
    }

    public interface OnCommand {
        void onCommand(CommandCall call);
    }
}
