package club.checs.csbot.commands.implementations;

import club.checs.csbot.HttpsRequest;
import club.checs.csbot.commands.CommandCall;
import club.checs.csbot.commands.SmartCommand;
import club.checs.csbot.commands.arguments.LangArg;
import club.checs.csbot.commands.arguments.StringArg;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

//Super Secret Api key: trnsl.1.1.20170316T223724Z.8238214aa838c9dc.681e23d2bdb0abd2b0d2d9e52b10f212bc257a39
public class TranslateCommand extends SmartCommand {
    private final String API_KEY;

    public TranslateCommand(String command) {
        super(command);
        this.addArgument(new LangArg("to"));
        this.addArgument(new LangArg("from").setOptional(true).continueIfMissing());
        this.addArgument(new StringArg("message"));
        File clientIdFile = new File("russiankey.txt");
        if (!clientIdFile.exists()) {
            System.err.println("Couldn't find russiankey.txt! This file should contain the russian api key.");
            API_KEY = "";
            return;
        }
        String id;
        try {
            id = Files.lines(clientIdFile.toPath()).iterator().next();
        } catch (IOException e) {
            e.printStackTrace();
            API_KEY = "";
            return;
        }
        API_KEY = id;
    }

    public void onCommand(CommandCall call) {
        String message = (String) call.getArg("message");
        boolean withFrom = call.hasArg("from");
        if (withFrom)
            message = translate((String) call.getArg("to"), (String) call.getArg("from"), message);
        else
            message = translate((String) call.getArg("to"), message);
        call.sendMessage(message);
    }

    private String translate(String to, String from, String str) {
        HttpsRequest req = new HttpsRequest("https://translate.yandex.net/api/v1.5/tr.json/translate?key=" + API_KEY
                + "&text=" + str + "&lang=" + from + "-" + to);
        return getResponseText(req);
    }

    private String translate(String to, String str) {
        HttpsRequest req = new HttpsRequest("https://translate.yandex.net/api/v1.5/tr.json/translate?key=" + API_KEY
                + "&text=" + str + "&lang=" + to);
        return getResponseText(req);
    }

    private String getResponseText(HttpsRequest req) {
        JsonObject res = new JsonParser().parse(req.getRawResponse()).getAsJsonObject();
        return res.getAsJsonArray("text").get(0).getAsJsonPrimitive().getAsString();
    }
}
