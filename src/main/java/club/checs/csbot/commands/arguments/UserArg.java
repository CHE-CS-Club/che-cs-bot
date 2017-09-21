package club.checs.csbot.commands.arguments;

import sx.blah.discord.api.IDiscordClient;
import sx.blah.discord.handle.obj.IUser;

import java.util.HashMap;

public class UserArg extends SingleCommandArg<IUser> {
    private IDiscordClient client;

    public UserArg(String name, IDiscordClient client) {
        super(name, "User");
        this.client = client;
    }

    @Override
    public void processArgs(String string, HashMap<String, Object> results) {
        if (string.charAt(2) == '!')
            setResult(results, client.getUserByID(Long.parseLong(string.substring(3, string.length() - 1))));
        else
            setResult(results, client.getUserByID(Long.parseLong(string.substring(2, string.length() - 1))));
    }

    @Override
    public boolean hasValidInput(String string) {
        return string.startsWith("<@") && string.endsWith(">");
    }
}
