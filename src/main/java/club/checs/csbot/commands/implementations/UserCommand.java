package club.checs.csbot.commands.implementations;

import club.checs.csbot.builder.ResponseBuilder;
import club.checs.csbot.commands.CommandCall;
import club.checs.csbot.commands.SmartCommand;
import club.checs.csbot.commands.arguments.StringArg;
import club.checs.csbot.commands.arguments.UserArg;
import sx.blah.discord.api.IDiscordClient;
import sx.blah.discord.handle.obj.IUser;

public class UserCommand extends SmartCommand {
    public UserCommand(String command, IDiscordClient client) {
        super(command);
        this.addArgument(new UserArg("user", client));
        this.addArgument(new StringArg("message"));
    }

    @Override
    public void onCommand(CommandCall call) {
        System.out.println(call.getArg("message"));
        if (!call.hasArg("user")) {
            call.sendMessage(new ResponseBuilder().mention(call.getSender())
                    .withText(" I couldn't find that user!").build());
            return;
        }
        call.sendMessage(((IUser) call.getArg("user")).getName() + " " + call.getArg("message"));
    }
}
