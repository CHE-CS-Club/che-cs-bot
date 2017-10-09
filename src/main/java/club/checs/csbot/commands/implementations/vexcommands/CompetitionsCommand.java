package club.checs.csbot.commands.implementations.vexcommands;

import club.checs.csbot.commands.CommandCall;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import sx.blah.discord.util.EmbedBuilder;

import java.awt.*;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.TimeZone;

public class CompetitionsCommand extends VexQueryCommand {
    private DateFormat dateiso = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm':00+00:00'");
    private DateFormat dateReadable = new SimpleDateFormat("E, MMMMM d");

    public CompetitionsCommand(String command) {
        super(command, "get_events");
        dateiso.setTimeZone(TimeZone.getDefault());
    }

    @Override
    public void onCommand(CommandCall call) {
        JsonObject response = query("&region=New%20Jersey&program=vrc&status=future");
        JsonArray results = getResults(response);
        System.out.println(results);

        EmbedBuilder builder = new EmbedBuilder().withColor(Color.blue);
        StringBuilder description = new StringBuilder();
        for (int i = 0; i < ((results.size() >= 4) ? 4 : results.size()); i++) {
            JsonObject result = results.get(i).getAsJsonObject();
            String sku = result.get("sku").getAsString();
            String eventName = result.get("name").getAsString();
            description.append("✪ [").append(eventName).append("](https://vexdb.io/events/view/").append(sku)
                    .append(")\n");
            try {
                description.append(" - *").append(dateReadable.format(dateiso.parse(result.get("start").getAsString())))
                        .append("* at *").append(result.get("loc_address1").getAsString()).append("*\n");
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
        builder.withDesc(description.toString());

        call.sendEmbedMessage("__**The next four competitions:**__", builder.build());
    }
}
