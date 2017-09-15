package club.checs.csbot.commands.implementations;

import club.checs.csbot.commands.CommandCall;
import club.checs.csbot.commands.SmartCommand;
import club.checs.csbot.commands.arguments.BooleanArg;
import club.checs.csbot.commands.arguments.StringArg;

public class TestCommand extends SmartCommand {

    public TestCommand(String command) {
        super(command);
        this.addArgument(new BooleanArg("bold").setOptional(true).continueIfMissing());
        this.addArgument(new StringArg("message"));
    }

    public void onCommand(CommandCall call) {
        String message = (String) call.getArg("message");
        boolean withBold = call.hasArg("bold") ? (Boolean) call.getArg("bold") : false;

        if (withBold)
            message = "**" + message + "**";

        call.sendMessage(message);
    }
}
