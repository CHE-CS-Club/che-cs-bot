package club.checs.csbot.commands.arguments;

import java.util.HashMap;

public class IntArg extends CommandArg<Integer> {
    public IntArg(String name) {
        super(name, "Integer");
    }

    public String[] processArgs(String[] inputArgs, HashMap<String, Object> results) {
        String bool = inputArgs[0].toLowerCase();
        // If we contain one of the following, it means we're true
        setResult(results, Integer.parseInt(inputArgs[0]));
        return removeStartArgs(inputArgs, 0);
    }

    public boolean hasValidInput(String[] inputArgs) {
        if (inputArgs.length < 1)
            return false;
        String num = inputArgs[0].toLowerCase();
        try {
            int ignored = Integer.parseInt(num);
        } catch (Exception ignored) {
            return false;
        }
        return true;
    }
}
