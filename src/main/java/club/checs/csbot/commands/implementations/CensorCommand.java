package club.checs.csbot.commands.implementations;

import club.checs.csbot.commands.CommandCall;
import club.checs.csbot.commands.SmartCommand;
import club.checs.csbot.commands.arguments.StringArg;

public class CensorCommand extends SmartCommand {

    public CensorCommand(String command) {
        super(command);
        this.addArgument(new StringArg("message"));
    }

    public void onCommand(CommandCall call) {
        call.deleteMessage();
        call.sendMessage(call.getSender().getNicknameForGuild(call.getEvent().getClient().getGuilds().get(0)) + ": " +
                starStr((String) call.getArg("message")));
    }

    // Replace every character with a star or space
    private String starStr(String str) {
        StringBuilder output = new StringBuilder();
        for (int i = 0; i < str.length(); i++) {
            output.append(str.charAt(i) == ' ' ? " " : "\\*");
        }
        return output.toString();
    }
}
