package club.checs.csbot;

import club.checs.csbot.bot.HttpsRequest;
import club.checs.csbot.bot.Keystore;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.util.HashMap;

public class RemindWatcher {
    private final JsonObject key;

    public RemindWatcher() {
        // Get an access key
        key = getResponse(new HttpsRequest(HttpsRequest.reqType.POST,
                "https://api.remind.com:443/v2/access_tokens", // Destination URL
                "user[email]", Keystore.getKey("quizletUser"), // Username
                "user[password]", Keystore.getKey("quizletPass"), // Pasword
                "persist", "true")); // Tell key to persist
    }

    public HttpsRequest sendHttpRequest(HttpsRequest.reqType type, String url, HashMap<String, String> args) {
        String[] stringArgs = new String[args.size() * 2];
        // Add the key and value to the array in order
        for (int i = 0; i < args.keySet().size(); i++) {
            stringArgs[i * 2] = (String) args.keySet().toArray()[0];
            stringArgs[(i * 2) + 1] = args.get(stringArgs[i]);
        }
        return new HttpsRequest(type, url, stringArgs);
    }

    private JsonObject getResponse(HttpsRequest req) {
        String rawResponse = req.getRawResponse();
        if (rawResponse == null)
            return null;
        return new JsonParser().parse(req.getRawResponse()).getAsJsonObject();
    }
}
