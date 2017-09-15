package club.checs.csbot.commands.arguments;

import java.util.HashMap;

public class StringArg extends CommandArg<String> {
    public StringArg(String name) {
        super(name, "String");
    }

    public String[] processArgs(String[] inputArgs, HashMap<String, Object> results) {
        // Assume we have valid input since this is getting called
        StringBuilder result = new StringBuilder();
        for (String part : inputArgs)
            result.append(" ").append(part.replaceAll("/", ""));
        result = new StringBuilder(result.toString().trim()); // Get rid of leading space
        setResult(results, result.toString());

        return new String[0];
    }

    public boolean hasValidInput(String[] inputArgs) {
        return inputArgs.length > 0;
    }
}
