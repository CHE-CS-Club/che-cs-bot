package club.checs.csbot;

import club.checs.csbot.commands.SmartCommand;
import club.checs.csbot.commands.implementations.*;
import club.checs.csbot.commands.implementations.vexcommands.AwardsCommand;
import club.checs.csbot.commands.implementations.vexcommands.RankCommand;
import club.checs.csbot.commands.implementations.vexcommands.TeamCommand;
import sx.blah.discord.api.ClientBuilder;
import sx.blah.discord.api.IDiscordClient;
import sx.blah.discord.api.events.EventDispatcher;
import sx.blah.discord.handle.obj.IRole;
import sx.blah.discord.util.DiscordException;
import sx.blah.discord.util.MissingPermissionsException;
import sx.blah.discord.util.RateLimitException;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

public class Main {
    public static CommandManager cmanager;

    public static void main(String[] args) {
        File clientIdFile = new File("botclient.txt");
        if (!clientIdFile.exists()) {
            System.err.println("Couldn't find botclient.txt! This file should contain the bot client private key.");
            return;
        }

        String id;
        try {
            id = Files.lines(clientIdFile.toPath()).iterator().next();
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }

        IDiscordClient client = createClient(id, true);

        EventDispatcher dispatcher = client.getDispatcher(); // Gets the EventDispatcher instance for this client instance

        PermManager pmanager = new PermManager();
        cmanager = new CommandManager(client, pmanager);
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
        cmanager.addCommand("getguild", new SmartCommand("getguild", (call) -> {
            call.sendMessage("Guild ID: " + call.getEvent().getGuild().getStringID());
        }));
        cmanager.addCommand("getroles", new SmartCommand("getroles", (call) -> {
            StringBuilder builder = new StringBuilder();
            for (IRole role : call.getEvent().getGuild().getRolesForUser(call.getSender()))
                builder.append(role).append(": ").append(role.getStringID()).append(" ");
            call.sendMessage("Roles: " + builder.toString());
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

        dispatcher.registerListener(cmanager); // Registers the command manager's listener
    }

    public static IDiscordClient createClient(String token, boolean login) { // Returns a new instance of the Discord client
        ClientBuilder clientBuilder = new ClientBuilder(); // Creates the ClientBuilder instance
        clientBuilder.withToken(token); // Adds the login info to the builder
        try {
            if (login) {
                return clientBuilder.login(); // Creates the client instance and logs the client in
            } else {
                return clientBuilder.build(); // Creates the client instance but it doesn't log the client in yet, you would have to call client.login() yourself
            }
        } catch (DiscordException e) { // This is thrown if there was a problem building the client
            e.printStackTrace();
            return null;
        }
    }
}
