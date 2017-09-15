package club.checs.csbot.commands.implementations.vexcommands;

import club.checs.csbot.commands.CommandCall;
import club.checs.csbot.commands.arguments.TeamArg;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import sx.blah.discord.util.EmbedBuilder;

import java.awt.*;

public class RankCommand extends VexQueryCommand {

    public RankCommand(String command) {
        super(command, "get_skills");
        this.addArgument(new TeamArg("team"));
    }

    @Override
    public void onCommand(CommandCall call) {
        JsonObject response = query("&team=" + call.getArg("team") + "&season_rank=true");
        JsonArray results = getResults(response);

        if (results.size() < 3) {
            call.sendMessage("No rank could be found for team **" + call.getArg("team") + "**");
            return;
        }

        String description = "";
        description += "Driver: " + getJsonByType(0, results).get("score").getAsInt() + " ([#" +
                getJsonByType(0, results).get("season_rank").getAsInt() + "](https://vexdb.io/skills/VRC/Starstruck/Driver?p=" +
                getPage(getJsonByType(0, results).get("season_rank").getAsInt()) + ") in the world)\n";
        description += "Programming: " + getJsonByType(1, results).get("score").getAsInt() + " ([#" +
                getJsonByType(1, results).get("season_rank").getAsInt() + "](https://vexdb.io/skills/VRC/Starstruck/Programming?p=" +
                getPage(getJsonByType(1, results).get("season_rank").getAsInt()) + ") in the world)\n";
        description += "Overall: " + getJsonByType(2, results).get("score").getAsInt() + " ([#" +
                getJsonByType(2, results).get("season_rank").getAsInt() + "](https://vexdb.io/skills/VRC/Starstruck/Robot?p=" +
                getPage(getJsonByType(2, results).get("season_rank").getAsInt()) + ") in the world)";


        EmbedBuilder builder = new EmbedBuilder().withColor(Color.blue);

        builder.withDesc(description);

        call.sendEmbedMessage("__**Skills ranks for team " + call.getArg("team") + "**__", builder.build());
    }

    private int getPage(int rank) {
        return (int) Math.ceil((rank - 1) / 50) + 1;
    }

    private JsonObject getJsonByType(int type, JsonArray arr) {
        if (type >= 0 && type <= 2)
            for (int i = 0; i < 3; i++)
                if (arr.get(i).getAsJsonObject().get("type").getAsInt() == type)
                    return arr.get(i).getAsJsonObject();
        return null;
    }

}
