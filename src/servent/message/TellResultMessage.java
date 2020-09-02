package servent.message;

import app.ServentInfo;

public class TellResultMessage extends BasicMessage {
	
	private int[][] matrix;

	public TellResultMessage(ServentInfo senderInfo, ServentInfo recieverInfo, int[][] matrix) {
		super(MessageType.TELL_RESULT, senderInfo, recieverInfo);
		this.matrix = matrix;
	}
	
	public int[][] getMatrix() {
		return matrix;
	}

}
