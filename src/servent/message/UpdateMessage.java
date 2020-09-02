package servent.message;

import java.util.List;

import app.ServentInfo;

public class UpdateMessage extends BasicMessage {
	
	private List<ServentInfo> activeServents;
	
	public UpdateMessage(ServentInfo senderInfo, ServentInfo recieverInfo, List<ServentInfo> activeServents) {
		super(MessageType.UPDATE, senderInfo, recieverInfo);
		this.activeServents = activeServents;
	}
	
	public List<ServentInfo> getActiveServents() {
		return activeServents;
	}

}
