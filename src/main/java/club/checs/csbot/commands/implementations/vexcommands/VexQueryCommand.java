package club.checs.csbot.commands.implementations.vexcommands;

import club.checs.csbot.HttpsRequest;
import club.checs.csbot.commands.SmartCommand;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public abstract class VexQueryCommand extends SmartCommand {
    private final String SEASON = "inthezone";
    private final String api;

    public VexQueryCommand(String command, String api) {
        super(command);
        this.api = api;
    }

    protected JsonObject query(String ending) {
        return query(api, ending);
    }

    protected JsonObject query(String api, String ending) {
        HttpsRequest req = new HttpsRequest("https://api.vexdb.io/v1/" + api + "?season=" + SEASON + ending);
        return new JsonParser().parse(req.getRawResponse()).getAsJsonObject();
    }

    protected JsonArray getResults(JsonObject object) {
        return object.getAsJsonArray("result");
    }
}
