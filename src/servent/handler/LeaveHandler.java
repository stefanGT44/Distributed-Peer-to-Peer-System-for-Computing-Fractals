package servent.handler;

import java.util.ArrayList;
import java.util.List;

import app.AppConfig;
import app.ServentInfo;
import servent.message.LeaveMessage;
import servent.message.Message;
import servent.message.MessageType;
import servent.message.util.MessageUtil;

public class LeaveHandler implements MessageHandler {

	private Message clientMessage;

	public LeaveHandler(Message clientMessage) {
		this.clientMessage = clientMessage;
	}

	@Override
	public void run() {
		if (clientMessage.getMessageType() == MessageType.LEAVE) {
			LeaveMessage msg = (LeaveMessage) clientMessage;

			synchronized (AppConfig.lock) {

				System.out.println("LEAVE HANDLER");
				System.out.println("MSG = " + msg.getActiveServents());
				System.out.println("OUR = " + AppConfig.activeServents);
				
				if (AppConfig.activeServents.contains(msg.getSenderInfo())) {
					AppConfig.timestampedStandardPrint("Node " + msg.getSenderInfo().getId() + " leaving the network.");
					List<ServentInfo> missingServents = new ArrayList<>();
					
					for (ServentInfo servent : AppConfig.activeServents) {
						if (!msg.getActiveServents().contains(servent)) 
							missingServents.add(servent);
					}
					
					if (!missingServents.isEmpty()) {
						AppConfig.timestampedStandardPrint("Informing new nodes: " + missingServents);
						List<ServentInfo> newList = new ArrayList<>();
						newList.addAll(missingServents);
						newList.addAll(msg.getActiveServents());
						for (ServentInfo servent: missingServents) {
							LeaveMessage leaveMsg = new LeaveMessage(msg.getSenderInfo(), servent, newList);
							MessageUtil.sendMessage(leaveMsg);
						}
					}
					
					AppConfig.activeServents.remove(msg.getSenderInfo());
					AppConfig.timestampedStandardPrint("Current Active Nodes: ");
					for (ServentInfo servent: AppConfig.activeServents)
						AppConfig.timestampedStandardPrint("Servent " + servent.getId());
					
				}
			}

		} else {
			AppConfig.timestampedErrorPrint("Leave handler got a message that is not LEAVE");
		}
	}

}
