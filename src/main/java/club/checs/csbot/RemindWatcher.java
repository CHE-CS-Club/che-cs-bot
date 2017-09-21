package club.checs.csbot;

import club.checs.csbot.bot.HttpsRequest;
import club.checs.csbot.bot.Keystore;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.security.InvalidParameterException;
import java.util.HashMap;

public class RemindWatcher {
    public RemindWatcher() {
        JsonObject response = getResponse(new HttpsRequest(HttpsRequest.reqType.POST, "https://api.remind.com:443/v2/access_tokens",
                "user[email]", Keystore.getKey("quizletUser"),
                "user[password]", Keystore.getKey("quizletPass")));
        System.out.println(response);
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
