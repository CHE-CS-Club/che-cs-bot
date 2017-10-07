package club.checs.csbot.commands.implementations;

import club.checs.csbot.bot.HttpsRequest;
import club.checs.csbot.bot.Keystore;
import club.checs.csbot.commands.CommandCall;
import club.checs.csbot.commands.SmartCommand;
import club.checs.csbot.commands.arguments.WordArg;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import sx.blah.discord.util.EmbedBuilder;
import sx.blah.discord.util.RequestBuffer;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.awt.*;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.CompletableFuture;

public class DefineCommand extends SmartCommand {

    public DefineCommand(String command) {
        super(command);
        addArgs(new WordArg("word")); // Word to define
    }

    @Override
    public void onCommand(CommandCall call) {
        // Format everything we need
        String wordUnformatted = call.getArg("word").toString().toLowerCase();
        final String word = "**" + wordUnformatted.substring(0, 1).toUpperCase() + wordUnformatted.substring(1) + ":**";
        EmbedBuilder builder = new EmbedBuilder()
                .withColor(Color.red)
                .withDesc(word + "\n" + "Defining term...");

        // Create the request for the definition
        final CompletableFuture<String> request = new HttpsRequest(HttpsRequest.reqType.GET,
                "https://www.dictionaryapi.com/api/v1/references/collegiate/xml/" + wordUnformatted, "key",
                Keystore.getKey("dictionaryKey")).getRawResponseFuture();

        // Send the message and get it when it goes through
        call.sendEmbedMessage("", builder.build()).thenAcceptAsync(message -> {
            // Once we get a reply with a definition
            request.thenAcceptAsync(response -> {
                // TODO Remove
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
                DocumentBuilder db = null;
                try {
                    db = dbf.newDocumentBuilder();
                } catch (ParserConfigurationException e) {
                    e.printStackTrace();
                }
                try {
                    Document document =  db.parse(new ByteArrayInputStream(response.getBytes(StandardCharsets.UTF_8.name())));
                    document.getDocumentElement().normalize();
                    NodeList definitions = document.getElementsByTagName("def");
                    for (int i = 0; i < definitions.getLength(); i++) {
                        Element eElement = (Element) definitions.item(i);
                        System.out.println(eElement.toString());
                    }
                    System.out.println(response);
                } catch (SAXException | IOException e) {
                    e.printStackTrace();
                }

                String definition = "Defined";

                // Edit in our definition
                RequestBuffer.request(() -> {
                    message.edit(builder.withDesc(word + "\n" + definition).build());
                });
            });
        });
    }
}
