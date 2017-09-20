package club.checs.csbot;

import club.checs.csbot.commands.SmartCommand;
import club.checs.csbot.commands.implementations.*;
import club.checs.csbot.commands.implementations.vexcommands.AwardsCommand;
import club.checs.csbot.commands.implementations.vexcommands.RankCommand;
import club.checs.csbot.commands.implementations.vexcommands.TeamCommand;
import sx.blah.discord.api.IDiscordClient;
import sx.blah.discord.handle.obj.IRole;
import sx.blah.discord.util.DiscordException;
import sx.blah.discord.util.MissingPermissionsException;
import sx.blah.discord.util.RateLimitException;

class LambdaCommandHelper {
    // Not intended to be constructed
    private LambdaCommandHelper() {

    }

    static void registerLambdaCommands(CommandManager cmanager, PermManager pmanager, IDiscordClient client) {
        // General commands
        cmanager.addCommand("boldtest", new TestCommand("boldtest"));
        cmanager.addCommand("error", new SmartCommand("error", (call) -> {
            @SuppressWarnings("NumericOverflow") int i = 10 / 0;
        }));
        cmanager.addCommand("ryan", new SmartCommand("ryan", (call) -> {
            try {
                call.getSender().getOrCreatePMChannel().sendMessage("LOL nerd");
            } catch (MissingPermissionsException | RateLimitException | DiscordException e) {
                e.printStackTrace();
            }
        }));
        cmanager.addCommand("help", new SmartCommand("help", (call) -> {
            StringBuilder builder = new StringBuilder().append("*Commands:* ");
            for (SmartCommand command : cmanager.getCommands().values())
                builder.append(command.getCommand()).append(", ");
            call.sendMessage(builder.toString().substring(0, builder.toString().length() - 2));
        }));
        cmanager.addCommand("ryan", new SmartCommand("ryan", (call) -> {
            call.sendMessage("***Look, it has too much give!***");
        }));
        cmanager.addCommand("backwards", new BackwardsCommand("backwards"));
        cmanager.addCommand("badword", new CensorCommand("badword"));
        cmanager.addCommand("censor", new CensorCommand("censor"));
        cmanager.addCommand("translate", new TranslateCommand("translate"));
        cmanager.addCommand("usertest", new UserCommand("usertest", client));
        cmanager.addCommand("awards", new AwardsCommand("awards"));
        cmanager.addCommand("team", new TeamCommand("team"));
        cmanager.addCommand("rank", new RankCommand("rank"));
        cmanager.addCommand("mute", new MuteCommand("mute", client, pmanager));
        cmanager.addCommand("unmute", new UnmuteCommand("unmute", client, pmanager));
        cmanager.addCommand("lambda", new LambdaAddCommand("lambda"));

        // Utility/dev commands
        cmanager.addCommand("getguild", new SmartCommand("getguild", (call) -> {
            call.sendMessage("Guild ID: " + call.getEvent().getGuild().getStringID());
        }));
        cmanager.addCommand("getroles", new SmartCommand("getroles", (call) -> {
            StringBuilder builder = new StringBuilder();
            for (IRole role : call.getEvent().getGuild().getRolesForUser(call.getSender()))
                builder.append(role).append(": ").append(role.getStringID()).append(" ");
            call.sendMessage("Roles: " + builder.toString());
        }));
        cmanager.addCommand("getchannel", new SmartCommand("getchannel", (call) -> {
            call.sendMessage("Channel ID: " + call.getEvent().getChannel().getStringID());
        }));
    }
}
