package club.checs.csbot.commands.implementations;

import club.checs.csbot.commands.CommandCall;
import club.checs.csbot.commands.SmartCommand;
import club.checs.csbot.commands.arguments.StringArg;

public class BackwardsCommand extends SmartCommand {

    public BackwardsCommand(String command) {
        super(command);
        this.addArgument(new StringArg("message"));
    }

    public void onCommand(CommandCall call) {
        String message = (String) call.getArg("message");
        message = reverseStr(message);
        call.sendMessage(message);
    }

    private String reverseStr(String str) {
        String output = "";
        for (int i = str.length() - 1; i >= 0; i--) {
            output += str.charAt(i);
        }
        return output;
    }
}
