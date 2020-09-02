package servent.message;

import app.Job;
import app.ServentInfo;

public class TellStatusMessage extends BasicMessage {

	private Job job;
	
	public TellStatusMessage(ServentInfo senderInfo, ServentInfo recieverInfo, Job job) {
		super(MessageType.TELL_STATUS, senderInfo, recieverInfo);
		this.job = job;
	}
	
	public Job getJob() {
		return job;
	}
	
}
