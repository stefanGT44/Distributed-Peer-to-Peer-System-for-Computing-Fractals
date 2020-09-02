package servent.message;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import app.AppConfig;
import app.Job;
import app.ServentInfo;

public class WelcomeMessage extends BasicMessage {

	private List<ServentInfo> activeServents;
	private Map<ServentInfo, Job> finalJobs;
	private Map<Job, List<Job>> queueMap;
	private Map<String, Integer> groups;
	
	public WelcomeMessage(ServentInfo senderInfo, ServentInfo recieverInfo, List<ServentInfo> activeServents, 
			Map<ServentInfo, Job> finalJobs, Map<Job, List<Job>> queueMap, Map<String, Integer> groups) {
		super(MessageType.WELCOME, senderInfo, recieverInfo);
		this.activeServents = activeServents;
		this.finalJobs = finalJobs;
		this.queueMap = queueMap;
		this.groups = groups;
	}
	
	public List<ServentInfo> getActiveServents() {
		return activeServents;
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
