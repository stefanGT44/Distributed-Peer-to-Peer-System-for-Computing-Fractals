package servent.handler;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

import app.AppConfig;
import app.ServentInfo;
import servent.message.ConnectMessage;
import servent.message.LeaveMessage;
import servent.message.Message;
import servent.message.MessageType;
import servent.message.UpdateMessage;
import servent.message.WelcomeMessage;
import servent.message.util.MessageUtil;

public class WelcomeHandler implements MessageHandler {

	private Message clientMessage;

	public WelcomeHandler(Message clientMessage) {
		this.clientMessage = clientMessage;
	}

	@Override
	public void run() {
		if (clientMessage.getMessageType() == MessageType.WELCOME) {
			WelcomeMessage msg = (WelcomeMessage) clientMessage;

			synchronized (AppConfig.leaveLock) {
				// javljamo bootstrapu da nas doda u listu aktivnih servenata
				
				if (!AppConfig.LEAVING) {
					//ukoliko je izabrano leave brzo nakon starta, ne saljemo bootstrapu poruku da nas ubaci u listu
					//i ne saljemo ostalim cvorovima da nas dodaju
					
					AppConfig.initialised = true;
					AppConfig.timestampedStandardPrint("Servent initialised");
					
					try {

						Socket bootstrap = new Socket(AppConfig.bootstrapInfo.getIpAddress(), AppConfig.bootstrapInfo.getListenerPort());
						ObjectOutputStream oos = new ObjectOutputStream(bootstrap.getOutputStream());

						oos.writeObject(new ConnectMessage(AppConfig.myServentInfo, AppConfig.bootstrapInfo, "joined"));
						oos.flush();

					} catch (IOException e) {
						e.printStackTrace();
					}

					// javljamo svim cvorovima u mrezi da nas dodaju u svoju listu
					synchronized (AppConfig.lock) {
						
						if (AppConfig.queueMap == null) {
							AppConfig.queueMap = msg.getQueueMap();
							AppConfig.finalJobs = msg.getFinalJobs();
							AppConfig.groups = msg.getGroups();
						}
						
						List<ServentInfo> list = new ArrayList<>();
						AppConfig.activeServents.remove(AppConfig.myServentInfo);
						AppConfig.activeServents.addAll(msg.getActiveServents());
						list.addAll(AppConfig.activeServents);
						
						for (ServentInfo node : msg.getActiveServents()) {
							if (node.equals(AppConfig.myServentInfo))
								continue;
							UpdateMessage update = new UpdateMessage(AppConfig.myServentInfo, node, list);
							MessageUtil.sendMessage(update);
						}
						
					}

				} else {
					
					synchronized(AppConfig.lock) {
						
						List<ServentInfo> list = new ArrayList<>();
						AppConfig.activeServents.remove(AppConfig.myServentInfo);
						AppConfig.activeServents.addAll(msg.getActiveServents());
						list.addAll(AppConfig.activeServents);
						
						System.out.println("Before sending leave in WELCOME");
						System.out.println(AppConfig.activeServents);
						
						for (ServentInfo node: AppConfig.activeServents) {
							if (node.equals(AppConfig.myServentInfo)) 
								continue;
							LeaveMessage leaveMsg = new LeaveMessage(AppConfig.myServentInfo, node, list);
							MessageUtil.sendMessage(leaveMsg);
						}
					}
				}
				
			}

		} else {
			AppConfig.timestampedErrorPrint("Welcome handler got a message that is not WELCOME");
		}
	}

}
