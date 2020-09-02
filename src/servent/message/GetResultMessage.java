package servent.message;

import app.ServentInfo;

public class GetResultMessage extends BasicMessage {
	
	public GetResultMessage(ServentInfo senderInfo, ServentInfo recieverInfo) {
		super(MessageType.GET_RESULT, senderInfo, recieverInfo);
	}

}
