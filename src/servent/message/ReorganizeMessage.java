package servent.message;

import app.ServentInfo;

public class ReorganizeMessage extends BasicMessage {
	
	public ReorganizeMessage(ServentInfo senderInfo, ServentInfo recieverInfo) {
		super(MessageType.REORGANIZE, senderInfo, recieverInfo);
	}

}
