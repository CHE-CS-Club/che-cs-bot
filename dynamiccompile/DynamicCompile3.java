package dynamiccompile;import club.checs.csbot.commands.CommandCall;
import club.checs.csbot.commands.SmartCommand;public class DynamicCompile3 implements SmartCommand.OnCommand {@Override
    public void onCommand(CommandCall call) { call.sendMessage("Your words have no meaning"); try {     Thread.sleep(100); } catch (Exception e){} call.sendMessage("but you continue to do the same thing... now sorry is a sorry word, but it's all I have for you");  }
}