package servent.message;

import java.util.List;
import java.util.Map;

import app.Job;
import app.ServentInfo;

public class SubmitJobMessage extends BasicMessage {

	private Map<ServentInfo, Job> finalJobs;
	private Map<Job, List<Job>> queueMap;
	private Map<String, Integer> groups;
	
	public SubmitJobMessage(ServentInfo senderInfo, ServentInfo recieverInfo, Map<ServentInfo, Job> finalJobs, Map<Job, List<Job>> queueMap, Map<String, Integer> groups) {
		super(MessageType.SUBMIT_JOB, senderInfo, recieverInfo);
		this.finalJobs = finalJobs;
		this.queueMap = queueMap;
		this.groups = groups;
	}
	
	public Map<ServentInfo, Job> getFinalJobs() {
		return finalJobs;
	}
	
	public Map<Job, List<Job>> getQueueMap() {
		return queueMap;
	}
	
	public Map<String, Integer> getGroups() {
		return groups;
	}

}
