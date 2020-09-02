package cli;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import app.AppConfig;
import app.Cancellable;
import cli.command.CLICommand;
import cli.command.InfoCommand;
import cli.command.PauseCommand;
import cli.command.ResultCommand;
import cli.command.StartCommand;
import cli.command.StatusCommand;
import cli.command.StopCommand;
import servent.SimpleServentListener;
import worker.FractalWorker;

/**
 * A simple CLI parser. Each command has a name and arbitrary arguments.
 * 
 * Currently supported commands:
 * 
 * <ul>
 * <li><code>info</code> - prints information about the current node</li>
 * <li><code>pause [ms]</code> - pauses exection given number of ms - useful when scripting</li>
 * <li><code>ping [id]</code> - sends a PING message to node [id] </li>
 * <li><code>broadcast [text]</code> - broadcasts the given text to all nodes</li>
 * <li><code>causal_broadcast [text]</code> - causally broadcasts the given text to all nodes</li>
 * <li><code>print_causal</code> - prints all received causal broadcast messages</li>
 * <li><code>stop</code> - stops the servent and program finishes</li>
 * </ul>
 * 
 * @author stefanGT44
 *
 */
public class CLIParser implements Runnable, Cancellable {

	private volatile boolean working = true;
	
	private SimpleServentListener listener;
	private final List<CLICommand> commandList;
	
	public CLIParser(SimpleServentListener listener, FractalWorker fractalWorker) {
		this.listener = listener;
		this.commandList = new ArrayList<>();
		
		commandList.add(new InfoCommand());
		commandList.add(new PauseCommand());
		commandList.add(new StopCommand(this, listener));
		commandList.add(new StartCommand());
		commandList.add(new ResultCommand(fractalWorker));
		commandList.add(new StatusCommand(fractalWorker));
	}
	
	@Override
	public void run() {
		Scanner sc = new Scanner(System.in);
		
		while (working) {
			String commandLine = sc.nextLine();
			
			int spacePos = commandLine.indexOf(" ");
			
			String commandName = null;
			String commandArgs = null;
			if (spacePos != -1) {
				commandName = commandLine.substring(0, spacePos);
				commandArgs = commandLine.substring(spacePos+1, commandLine.length());
			} else {
				commandName = commandLine;
			}
			
			boolean found = false;
			
			for (CLICommand cliCommand : commandList) {
				if (cliCommand.commandName().equals(commandName)) {
					cliCommand.execute(commandArgs);
					if (commandName.equals("stop")) {
						listener.setLastMessage();
						return;
					}
					found = true;
					break;
				}
			}
			
			if (!found) {
				AppConfig.timestampedErrorPrint("Unknown command: " + commandName);
			}
		}
		
		sc.close();
	}
	
	@Override
	public void stop() {
		this.working = false;
		
	}
}
