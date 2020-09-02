package cli.command;

import app.AppConfig;
import app.ServentInfo;

public class InfoCommand implements CLICommand {

	@Override
	public String commandName() {
		return "info";
	}

	@Override
	public void execute(String args) {
		AppConfig.timestampedStandardPrint("Active servents: ");
		synchronized(AppConfig.lock) {
			for (ServentInfo servent: AppConfig.activeServents) {
				AppConfig.timestampedStandardPrint(servent.getId() + "");
			}
		}
	}

}
