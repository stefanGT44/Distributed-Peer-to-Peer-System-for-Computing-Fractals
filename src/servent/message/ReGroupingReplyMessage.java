package servent.message;

import app.Job;
import app.ServentInfo;

public class ReGroupingReplyMessage extends BasicMessage {
	
	private int[][] matrix;

	public ReGroupingReplyMessage(ServentInfo senderInfo, ServentInfo recieverInfo, int[][] matrix, String messageText) {
		super(MessageType.REGROUPING_REPLY, senderInfo, recieverInfo, messageText);
		this.matrix = matrix;
	}
	
	public int[][] getMatrix() {
		return matrix;
	}
	
}
