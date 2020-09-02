package servent.message;

import app.ServentInfo;

public class ConnectMessage extends BasicMessage {

	public ConnectMessage(ServentInfo senderInfo, ServentInfo recieverInfo, String messageText) {
		super(MessageType.CONNECT, senderInfo, recieverInfo, messageText);
	}

	

}
