package club.checs.csbot.commands.implementations;

import club.checs.csbot.Main;
import club.checs.csbot.commands.CommandCall;
import club.checs.csbot.commands.SmartCommand;
import club.checs.csbot.commands.arguments.CodeArgument;
import club.checs.csbot.commands.arguments.WordArg;
import sx.blah.discord.handle.obj.IRole;

import javax.tools.*;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

// TODO Cleanup and comment, messy af
public class LambdaAddCommand extends SmartCommand {
    private int i = 0;

    public LambdaAddCommand(String command) {
        super(command);
        addArgument(new WordArg("commandName"));
        addArgument(new CodeArgument("code"));
    }

    @Override
    public void onCommand(CommandCall call) {
        // Make sure in Java
        if (!((CodeArgument.CodeBlock) call.getArg("code")).getLang().equalsIgnoreCase("java")) {
            call.sendMessage(call.getSender().mention() + ", all lambdas must be programmed in java.");
            return;
        }

        // Check permissions
        if (!hasPerms(call, "356897883062272010", "291027779477176320")) { // TODO Somehow make this dynamic
            call.sendMessage("Sorry, but you do not have permissions to run this command " + call.getSender().getDisplayName(call.getEvent().getGuild()) + ".");
            return;
        }

        /* Build code */
        // Add all the wrapping we need before user code
        StringBuilder codeBuilder = new StringBuilder();
        codeBuilder.append("package dynamiccompile;");
        codeBuilder.append("import club.checs.csbot.commands.CommandCall;\n" +
                "import club.checs.csbot.commands.SmartCommand;");
        codeBuilder.append("public class DynamicCompile").append(++i).append(" implements SmartCommand.OnCommand {");
        codeBuilder.append("@Override\n" +
                "    public void onCommand(CommandCall call) {");

        // Add in the user code
        for (String line : ((CodeArgument.CodeBlock) call.getArg("code")).getCode())
            codeBuilder.append(line).append(" ");
        String lang = ((CodeArgument.CodeBlock) call.getArg("code")).getLang();

        // Add all the wrapping we need after user code
        codeBuilder.append("}\n" +
                "}");
        /* Build code */

        call.deleteMessage();
        call.sendMessage(call.getSender().mention() + ", I'll compile that command. Please wait.");

        new Thread(() -> {
            File compileCache = new File("dynamiccompile/DynamicCompile" + i + ".java");
            System.out.println("File name: " + compileCache.getName());
            if (compileCache.getParentFile().exists() || compileCache.getParentFile().mkdirs()) {

                try {
                    Writer writer = null;
                    try {
                        writer = new FileWriter(compileCache);
                        writer.write(codeBuilder.toString());
                        writer.flush();
                    } catch (IOException e) {
                        e.printStackTrace();
                    } finally {
                        try {
                            if (writer != null)
                                writer.close();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }

                /* Compilation Requirements */
                    DiagnosticCollector<JavaFileObject> diagnostics = new DiagnosticCollector<JavaFileObject>();
                    JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
                    StandardJavaFileManager fileManager = compiler.getStandardFileManager(diagnostics, null, null);

                    // This sets up the class path that the compiler will use.
                    // I've added the .jar file that contains the DoStuff interface within in it...
                    List<String> optionList = new ArrayList<String>();
                    optionList.add("-classpath");
                    optionList.add(System.getProperty("java.class.path") + ";dist/InlineCompiler.jar");

                    Iterable<? extends JavaFileObject> compilationUnit
                            = fileManager.getJavaFileObjectsFromFiles(Collections.singletonList(compileCache));
                    JavaCompiler.CompilationTask task = compiler.getTask(
                            null,
                            fileManager,
                            diagnostics,
                            optionList,
                            null,
                            compilationUnit);
                /* Compilation Requirements */

                    if (task.call()) {
                    /* Load and execute */
                        System.out.println("Yipe");
                        // Create a new custom class loader, pointing to the directory that contains the compiled
                        // classes, this should point to the top of the package structure!
                        URLClassLoader classLoader = new URLClassLoader(new URL[]{new File("./").toURI().toURL()});
                        // Load the class from the classloader by name....
                        Class<?> loadedClass = classLoader.loadClass("dynamiccompile.DynamicCompile" + i);
                        // Create a new instance...
                        Object obj = loadedClass.newInstance();
                        // Santity check
                        if (obj instanceof OnCommand) {
                            // Cast to the DoStuff interface
                            OnCommand stuffToDo = (OnCommand) obj;
                            // Run it baby
                            Main.cmanager.addCommand(call.getArg("commandName").toString(),
                                    new SmartCommand(call.getArg("commandName").toString(), stuffToDo));
                        }
                    /* Load and execute */
                    } else {
                        for (Diagnostic<? extends JavaFileObject> diagnostic : diagnostics.getDiagnostics()) {
                            System.out.format("Error on line %d in %s%n",
                                    diagnostic.getLineNumber(),
                                    diagnostic.getSource().toUri());
                            System.out.println(diagnostic.getMessage(Locale.US));
                        }
                    }
                    fileManager.close();
                } catch (IOException | ClassNotFoundException | InstantiationException | IllegalAccessException exp) {
                    exp.printStackTrace();
                }
            }
            call.sendMessage(call.getSender().mention() + ", your command has been compiled!");
        }).start();
    }

    private boolean hasPerms(CommandCall command, String... allowedRoles) {
        List<IRole> roles = command.getSender().getRolesForGuild(command.getEvent().getGuild());
        for (IRole role : roles)
            for (int i = 0; i < allowedRoles.length; i++)
                if (role.getID().equalsIgnoreCase(allowedRoles[i]))
                    return true;
        return false;
    }
}
