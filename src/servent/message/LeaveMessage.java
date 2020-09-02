package servent.message;

import java.util.List;

import app.ServentInfo;

public class LeaveMessage extends BasicMessage {

	private List<ServentInfo> activeServents;
	
	public LeaveMessage(ServentInfo senderInfo, ServentInfo recieverInfo, List<ServentInfo> activeServents) {
		super(MessageType.LEAVE, senderInfo, recieverInfo);
		this.activeServents = activeServents;
	}
	
	public List<ServentInfo> getActiveServents() {
		return activeServents;
	}
	
}
