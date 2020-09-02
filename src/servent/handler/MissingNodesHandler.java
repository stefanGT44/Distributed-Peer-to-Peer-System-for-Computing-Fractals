package servent.handler;

import java.util.ArrayList;
import java.util.List;

import app.AppConfig;
import app.ServentInfo;
import servent.message.Message;
import servent.message.MessageType;
import servent.message.MissingNodesMessage;
import servent.message.UpdateMessage;
import servent.message.util.MessageUtil;

public class MissingNodesHandler implements MessageHandler {

	private Message clientMessage;
	
	public MissingNodesHandler(Message clientMessage) {
		this.clientMessage = clientMessage;
	}
	
	@Override
	public void run() {
		if (clientMessage.getMessageType() == MessageType.MISSING_NODES) {
			MissingNodesMessage msg = (MissingNodesMessage)clientMessage;
			
			synchronized(AppConfig.lock) {
				for (ServentInfo node: msg.getMissingNodes()) {
					if (!AppConfig.activeServents.contains(node))
						AppConfig.activeServents.add(node);
				}
			}
			
		} else {
			AppConfig.timestampedErrorPrint("MissingNodes handler got a message that is not MISSING_NODES");
		}
	}
	
}
