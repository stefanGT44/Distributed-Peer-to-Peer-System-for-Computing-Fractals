package cli.command;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

import app.AppConfig;
import app.Job;
import app.ServentInfo;
import cli.CLIParser;
import servent.SimpleServentListener;
import servent.message.ConnectMessage;
import servent.message.LeaveMessage;
import servent.message.util.MessageUtil;

public class StopCommand implements CLICommand {

	private CLIParser parser;
	private SimpleServentListener listener;

	public StopCommand(CLIParser parser, SimpleServentListener listener) {
		this.parser = parser;
		this.listener = listener;
	}

	@Override
	public String commandName() {
		return "stop";
	}

	@Override
	public void execute(String args) {
		synchronized (AppConfig.leaveLock) {
			if (AppConfig.LEAVING) return;
			AppConfig.LEAVING = true;
			AppConfig.timestampedStandardPrint("Stopping...");

			Job poison = new Job();
			poison.setName("poison");
			
			AppConfig.jobQueue.add(poison);
			
			try {

				Socket bootstrap = new Socket(AppConfig.bootstrapInfo.getIpAddress(), AppConfig.bootstrapInfo.getListenerPort());
				ObjectOutputStream oos = new ObjectOutputStream(bootstrap.getOutputStream());

				oos.writeObject(new ConnectMessage(AppConfig.myServentInfo, AppConfig.bootstrapInfo, "leave"));
				oos.flush();

			} catch (IOException e) {
				e.printStackTrace();
			}

			synchronized (AppConfig.lock) {
				List<ServentInfo> servents = new ArrayList<>();
				servents.addAll(AppConfig.activeServents);
				for (ServentInfo servent : AppConfig.activeServents) {
					if (servent.equals(AppConfig.myServentInfo))
						continue;
					LeaveMessage msg = new LeaveMessage(AppConfig.myServentInfo, servent, servents);
					MessageUtil.sendMessage(msg);
				}
			}
		}
	}

}
