package club.checs.csbot.commands.arguments;

import java.util.HashMap;

// ```java System.out.println("Test!"); ```
public class CodeArgument extends CommandArg<CodeArgument.CodeBlock> {
    public CodeArgument(String name) {
        super(name, "Code");
    }

    public String[] processArgs(String[] inputArgs, HashMap<String, Object> results) {
        String lang = inputArgs[0].replaceAll("```", "");
        int len = -1;
        for (int i = 1; i < inputArgs.length; i++) // Search for an ending triple tick
            if (inputArgs[i].trim().endsWith("```")) { // Found it, save the index
                len = i;
                break;
            }
        String[] code = new String[inputArgs.length - 2];
        System.arraycopy(inputArgs, 1, code, 0, len - 1);
        setResult(results, new CodeBlock(lang, code));
        return removeStartArgs(inputArgs, 0);
    }

    public boolean hasValidInput(String[] inputArgs) {
        if (inputArgs.length > 0)
            if (inputArgs[0].startsWith("```")) // Starts with a triple tick
                for (int i = 1; i < inputArgs.length; i++) // Search for an ending triple tick
                    if (inputArgs[i].trim().endsWith("```"))
                        return true; // Got match, we have valid input
        return false;
    }

    public class CodeBlock {
        private final String lang;
        private final String[] code;

        public CodeBlock(String lang, String[] code) {
            this.lang = lang;
            this.code = code;
        }

        public String getLang() {
            return lang;
        }

        public String[] getCode() {
            return code;
        }
    }
}