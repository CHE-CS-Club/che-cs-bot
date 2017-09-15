package club.checs.csbot.commands.implementations;

import club.checs.csbot.PermManager;
import club.checs.csbot.builder.ResponseBuilder;
import club.checs.csbot.commands.CommandCall;
import club.checs.csbot.commands.SmartCommand;
import club.checs.csbot.commands.arguments.StringArg;
import club.checs.csbot.commands.arguments.UserArg;
import sx.blah.discord.api.IDiscordClient;
import sx.blah.discord.handle.obj.IRole;
import sx.blah.discord.handle.obj.IUser;
import sx.blah.discord.util.DiscordException;
import sx.blah.discord.util.MissingPermissionsException;
import sx.blah.discord.util.RateLimitException;

import java.util.List;

public class UnmuteCommand extends SmartCommand {

    IDiscordClient client;
    PermManager pmanager;

    public UnmuteCommand(String command, IDiscordClient client, PermManager pmanager) {
        super(command);
        this.client = client;
        this.pmanager = pmanager;
        this.addArgument(new UserArg("user", client));
        this.addArgument(new StringArg("message").setOptional(true).continueIfMissing().setDefault("Being a better boy"));
    }

    @Override
    public void onCommand(CommandCall call) {
        if (hasPerms(call, "356897883062272010", "291027779477176320")) { // TODO Somehow make this dynamic
            if (!call.hasArg("user")) {
                call.sendMessage(new ResponseBuilder().mention(call.getSender())
                        .withText(" I couldn't find that user!").build());
                return;
            }
            call.sendMessage(((IUser) call.getArg("user")).getName() + " has been unmuted for: " + call.getArg("message"));
            try {
                if (!((IUser) call.getArg("user")).getID().equals(call.getSender().getID()))
                    ((IUser) call.getArg("user")).getOrCreatePMChannel().sendMessage("You have been unmuted in the CHE Robotics Server for: " + call.getArg("message"));
            } catch (MissingPermissionsException | RateLimitException | DiscordException e) {
                e.printStackTrace();
            }
            pmanager.unmute((IUser) call.getArg("user"));
        } else {
            call.sendMessage("Sorry, but you do not have permissions to run this command " + call.getSender().getDisplayName(call.getEvent().getGuild()));
        }
    }

    private boolean hasPerms(CommandCall command, String... allowedRoles) {
        List<IRole> roles = command.getSender().getRolesForGuild(command.getEvent().getGuild());
        for (IRole role : roles)
            for (int i = 0; i < allowedRoles.length; i++)
                if (role.getID().equalsIgnoreCase(allowedRoles[i]))
                    return true;
        return false;
    }

}
