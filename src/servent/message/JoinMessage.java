package servent.message;

import app.ServentInfo;

public class JoinMessage extends BasicMessage {

	public JoinMessage(ServentInfo senderInfo, ServentInfo recieverInfo) {
		super(MessageType.JOIN, senderInfo, recieverInfo);
	}

}
