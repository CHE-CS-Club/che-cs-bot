package club.checs.csbot.commands.implementations;

import club.checs.csbot.commands.CommandCall;
import club.checs.csbot.commands.SmartCommand;
import club.checs.csbot.commands.arguments.WordArg;
import club.checs.csbot.managers.CommandManager;

public class HelpCommand extends SmartCommand {
    private final CommandManager cmanager;

    public HelpCommand(String command, CommandManager cmanager) {
        super(command);
        this.cmanager = cmanager;
        addArgs(new WordArg("command").setContinueIfMissing(true));
    }

    @Override
    public void onCommand(CommandCall call) {
        if (!call.hasArg("command")) {
            StringBuilder builder = new StringBuilder().append("*Commands:* ");
            for (SmartCommand command : cmanager.getCommands().values())
                builder.append(command.getCommand()).append(", ");
            call.sendMessage(builder.toString().substring(0, builder.toString().length() - 2));
        } else {
            SmartCommand command = cmanager.getCommand(call.getArg("command").toString());
            if (command != null) {
                if (command.getHelpText().length() != 0)
                    call.sendMessage(call.getSender().mention() + " Usage for `" + call.getArg("command") + "`:\n"
                            + "   !" + command.getCommand() + " " + command.getHelpText());
                else
                    call.sendMessage(call.getSender().mention() + " The command `" + call.getArg("command")
                            + "` has no arguments.");
            } else
                call.sendMessage(call.getSender().mention() + " Hmmm, couldn't find a command by the name of "
                        + call.getArg("command"));
        }
    }
}
