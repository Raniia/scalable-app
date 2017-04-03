package command.commands;

import command.Command;
import services.Dispatcher;

import java.util.Map;


public class FreezeCommand extends Command {
    public StringBuffer execute(Map<String, Object> requestMapData) throws Exception {
        Dispatcher.sharedInstance().get_threadPoolCmds().shutdown();
        return null;
    }
}
