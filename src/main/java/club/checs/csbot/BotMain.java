package club.checs.csbot;

import club.checs.csbot.bot.Keystore;
import club.checs.csbot.commands.LambdaCommandHelper;
import club.checs.csbot.managers.CommandManager;
import club.checs.csbot.managers.PermManager;
import club.checs.csbot.managers.RoleManager;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.auth.FirebaseCredentials;
import sx.blah.discord.api.ClientBuilder;
import sx.blah.discord.api.IDiscordClient;
import sx.blah.discord.api.events.EventDispatcher;
import sx.blah.discord.util.DiscordException;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

public class BotMain {
    public static CommandManager cmanager;

    public static void main(String[] args) {
        // Set up our key store file and grab the bot's discord private key
        String id;
        try {
            Keystore.setKeyStore("keystore.json");
            id = Keystore.getKey("botclient");
        } catch (FileNotFoundException e) {
            // Failed to grab key, shutdown bot; we can't continue
            e.printStackTrace();
            System.err.println("Couldn't find keystore.json! This file should contain all of the bots private keys.");
            return;
        }

        // We got our key, set up the bot's client
        IDiscordClient client = createClient(id);
        if (client == null) { // Fail safe if bot is null, has happened unpredictably
            System.out.println("Failed to create client, it was null!");
            return;
        }

        // Set up firebase
        FileInputStream serviceAccount = null;
        try {
            serviceAccount = new FileInputStream("che-cs-bot-google-key.json");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return;
        }

        FirebaseOptions options = null;
        try {
            options = new FirebaseOptions.Builder()
                    .setCredential(FirebaseCredentials.fromCertificate(serviceAccount))
                    .setDatabaseUrl("https://che-cs-bot.firebaseio.com/")
                    .build();
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }

        FirebaseApp.initializeApp(options);

        // Create permission manager
        PermManager pmanager = new PermManager();
        cmanager = new CommandManager(client, pmanager);

        // Set up event listeners
        EventDispatcher dispatcher = client.getDispatcher(); // Gets the EventDispatcher instance for this client instance
        dispatcher.registerListener(cmanager); // Registers the command manager's listeners
        dispatcher.registerListener(new RoleManager(client)); // Register role manager's listeners

        // Set up commands
        LambdaCommandHelper.registerLambdaCommands(cmanager, pmanager, client);

        // Experimental remind watcher
        new RemindWatcher();
    }

    // Creates and starts the bot
    private static IDiscordClient createClient(String token) { // Returns a new instance of the Discord client
        ClientBuilder clientBuilder = new ClientBuilder(); // Creates the ClientBuilder instance
        clientBuilder.withToken(token); // Adds the login info to the builder
        try {
            return clientBuilder.login(); // Creates the client instance and logs the client in
        } catch (DiscordException e) { // This is thrown if there was a problem building the client
            e.printStackTrace();
            return null;
        }
    }
}
