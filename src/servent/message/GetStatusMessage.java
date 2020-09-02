package servent.message;

import app.ServentInfo;

public class GetStatusMessage extends BasicMessage {

	public GetStatusMessage(ServentInfo senderInfo, ServentInfo recieverInfo) {
		super(MessageType.GET_STATUS, senderInfo, recieverInfo);
	}

}
