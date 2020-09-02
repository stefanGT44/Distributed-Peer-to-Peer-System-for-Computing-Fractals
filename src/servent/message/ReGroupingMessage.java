package servent.message;

import app.ServentInfo;

public class ReGroupingMessage extends BasicMessage {

	public ReGroupingMessage(ServentInfo senderInfo, ServentInfo recieverInfo, String messageText) {
		super(MessageType.REGROUPING, senderInfo, recieverInfo, messageText);
	}
	
}
