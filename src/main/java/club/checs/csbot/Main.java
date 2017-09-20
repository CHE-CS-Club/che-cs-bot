package club.checs.csbot;

import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.auth.FirebaseCredentials;
import sx.blah.discord.api.ClientBuilder;
import sx.blah.discord.api.IDiscordClient;
import sx.blah.discord.api.events.EventDispatcher;
import sx.blah.discord.util.DiscordException;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;

public class Main {
    public static CommandManager cmanager;

    public static void main(String[] args) {
        // Setup the bot
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

        IDiscordClient client = createClient(id);

        if (client == null) {
            System.out.println("Failed to create client, it was null!");
            return;
        }

        EventDispatcher dispatcher = client.getDispatcher(); // Gets the EventDispatcher instance for this client instance

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

        // Set up commands
        LambdaCommandHelper.registerLambdaCommands(cmanager, pmanager, client);

        // Register command listener
        dispatcher.registerListener(cmanager); // Registers the command manager's listener

        // Register verify listeners
        dispatcher.registerListener(new RoleManager(client));
    }

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
