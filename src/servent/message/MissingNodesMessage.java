package servent.message;

import java.util.List;

import app.ServentInfo;

public class MissingNodesMessage extends BasicMessage {

	private List<ServentInfo> missingNodes;
	
	public MissingNodesMessage(ServentInfo senderInfo, ServentInfo recieverInfo, List<ServentInfo> missingNodes) {
		super(MessageType.MISSING_NODES, senderInfo, recieverInfo);
		this.missingNodes = missingNodes;
	}
	
	public List<ServentInfo> getMissingNodes() {
		return missingNodes;
	}

}
