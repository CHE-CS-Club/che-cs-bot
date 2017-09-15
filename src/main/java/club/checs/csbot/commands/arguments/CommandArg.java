package club.checs.csbot.commands.arguments;

import java.security.Permission;
import java.util.ArrayList;
import java.util.HashMap;

// TODO Javadoc
public abstract class CommandArg<T> {
    private final String name;
    private final String typeName;
    private ArrayList<Permission> permissions;
    private boolean optional = false;
    private boolean continueIfMissing = false;
    private T defaultVal;

    public CommandArg(String name, String typeName) {
        this.name = name;
        this.typeName = typeName;
    }

    public abstract String[] processArgs(String[] inputArgs, HashMap<String, Object> results);

    public abstract boolean hasValidInput(String[] inputArgs);

    public ArrayList<Permission> getPermissions() {
        return permissions;
    }

    public boolean hasPermissions() {
        return permissions != null;
    }

    public CommandArg<T> withPermission(Permission perm) {
        setOptional(true); // Permission specific arguments should always be optional
        if (permissions == null)
            permissions = new ArrayList<Permission>();
        permissions.add(perm);
        return this; // For a builder type layout
    }

    protected void setResult(HashMap<String, Object> results, T result) {
        results.put(name, result);
    }

    public CommandArg<T> optional() {
        setOptional(true);
        return this;
    }

    public boolean isOptional() {
        return optional;
    }

    public CommandArg<T> setOptional(boolean optional) {
        this.optional = optional;
        if (!optional)
            continueIfMissing = false;
        return this; // For a builder type layout
    }

    public boolean isContinueIfMissing() {
        return continueIfMissing;
    }

    public CommandArg<T> setContinueIfMissing(boolean continueIfMissing) {
        this.continueIfMissing = continueIfMissing;
        if (continueIfMissing)
            setOptional(true);
        return this; // For a builder type layout
    }

    public CommandArg<T> continueIfMissing() {
        setContinueIfMissing(true);
        return this;
    }

    protected String[] removeArgs(String[] input, int start, int end) {
        String[] output = new String[input.length - (end - start) - 1]; // How big we'll be after chopping
        int pos = 0;
        for (int i = 0; i < start; i++) // Add before start
            output[pos++] = input[i];
        for (int i = end + 1; i < input.length; i++) // Add after end
            output[pos++] = input[i];
        return output;
    }

    protected String[] removeStartArgs(String[] input, int end) {
        return removeArgs(input, 0, end);
    }

    protected String[] removeEndArgs(String[] input, int start) {
        return removeArgs(input, start, input.length - 1);
    }

    public String getName() {
        return name;
    }

    public String getTypeName() {
        return typeName;
    }

    public T getDefault() {
        return this.defaultVal;
    }

    public CommandArg<T> setDefault(T defaultVal) {
        this.defaultVal = defaultVal;
        return this;
    }

    public boolean hasDefault() {
        return this.defaultVal != null;
    }
}