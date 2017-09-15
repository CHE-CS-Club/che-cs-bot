package club.checs.csbot.commands.arguments;

import java.util.HashMap;

public abstract class SingleCommandArg<T> extends CommandArg<T> {
    public SingleCommandArg(String name, String typeName) {
        super(name, typeName);
    }

    @Override
    public String[] processArgs(String[] inputArgs, HashMap<String, Object> results) {
        processArgs(inputArgs[0], results);
        return removeStartArgs(inputArgs, 0);
    }

    public abstract void processArgs(String string, HashMap<String, Object> results);

    @Override
    public boolean hasValidInput(String[] inputArgs) {
        if (inputArgs.length < 1)
            return false;
        return hasValidInput(inputArgs[0]);
    }

    public abstract boolean hasValidInput(String string);
}
