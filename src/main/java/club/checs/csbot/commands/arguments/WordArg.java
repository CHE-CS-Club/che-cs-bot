package club.checs.csbot.commands.arguments;

import java.util.HashMap;

public class WordArg extends SingleCommandArg<String> {
    public WordArg(String name) {
        super(name, "Word");
    }

    @Override
    public void processArgs(String arg, HashMap<String, Object> results) {
        // Assume we have valid input since this is getting called
        String result = (arg.replaceAll("/", ""));
        result = result.trim();
        setResult(results, result);
    }

    @Override
    public boolean hasValidInput(String string) {
        return true; // Super already checks for arg
    }
}
