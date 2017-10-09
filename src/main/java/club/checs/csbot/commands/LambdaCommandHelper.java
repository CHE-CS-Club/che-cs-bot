package club.checs.csbot.commands;

import club.checs.csbot.commands.implementations.*;
import club.checs.csbot.commands.implementations.vexcommands.AwardsCommand;
import club.checs.csbot.commands.implementations.vexcommands.CompetitionsCommand;
import club.checs.csbot.commands.implementations.vexcommands.RankCommand;
import club.checs.csbot.commands.implementations.vexcommands.TeamCommand;
import club.checs.csbot.managers.CommandManager;
import club.checs.csbot.managers.PermManager;
import sx.blah.discord.api.IDiscordClient;
import sx.blah.discord.handle.obj.IRole;
import sx.blah.discord.util.DiscordException;
import sx.blah.discord.util.MissingPermissionsException;
import sx.blah.discord.util.RateLimitException;

public class LambdaCommandHelper {
    // Not intended to be constructed
    private LambdaCommandHelper() {

    }

    public static void registerLambdaCommands(CommandManager cmanager, PermManager pmanager, IDiscordClient client) {
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
        cmanager.addCommand("competitions", new CompetitionsCommand("competitions"));
        cmanager.addCommand("rank", new RankCommand("rank"));
        cmanager.addCommand("mute", new MuteCommand("mute", client, pmanager));
        cmanager.addCommand("unmute", new UnmuteCommand("unmute", client, pmanager));
        cmanager.addCommand("lambda", new LambdaAddCommand("lambda"));
        cmanager.addCommand("define", new DefineCommand("define"));
        cmanager.addCommand("help", new HelpCommand("help", cmanager));

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
