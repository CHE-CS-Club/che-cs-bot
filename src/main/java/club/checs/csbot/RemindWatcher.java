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
        System.out.println(
                getResponse(sendHttpRequest("https://api.remind.com:443/v2/access_tokens", "user[email]",
                        Keystore.getKey("quizletUser"), "user[password]",
                        Keystore.getKey("quizletPass"))).toString());
    }

    public HttpsRequest sendHttpRequest(String url, HashMap<String, String> args) {
        String[] stringArgs = new String[args.size() * 2];
        // Add the key and value to the array in order
        for (int i = 0; i < args.keySet().size(); i++) {
            stringArgs[i * 2] = (String) args.keySet().toArray()[0];
            stringArgs[(i * 2) + 1] = args.get(stringArgs[i]);
        }
        return sendHttpRequest(url, stringArgs);
    }

    public HttpsRequest sendHttpRequest(String url, String... args) {
        // Make sure we have valid args
        if ((args.length & 1) != 0) // If number is odd
            throw new InvalidParameterException("Need even number of arguments");
        // Go through the keys/values and build the arg string with them
        StringBuilder argString = new StringBuilder();
        for (int i = 0; i < args.length; i += 2) {
            argString.append((i == 0) ? "?" : "&");
            argString.append(args[i]).append("=");
            try {
                argString.append(URLEncoder.encode(args[i + 1], "UTF-8"));
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
                return null;
            }
        }
        System.out.println("URL: " + url + argString.toString());
        // Build the request and return it
        return new HttpsRequest(url + argString.toString());
    }

    private JsonObject getResponse(HttpsRequest req) {
        JsonObject res = new JsonParser().parse(req.getRawResponse()).getAsJsonObject();
        return res;
    }
}
