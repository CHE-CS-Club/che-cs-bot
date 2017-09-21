package club.checs.csbot.bot;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;

public class Keystore {
    private static JsonObject keyStore;

    private Keystore() {

    }

    public static void setKeyStore(String filename) throws FileNotFoundException {
        BufferedReader br = new BufferedReader(new FileReader(filename));
        JsonParser parser = new JsonParser();
        keyStore = parser.parse(br).getAsJsonObject();
    }

    public static String getKey(String identifier) {
        if (keyStore == null)
            return null;
        return keyStore.get(identifier).getAsString();
    }
}
