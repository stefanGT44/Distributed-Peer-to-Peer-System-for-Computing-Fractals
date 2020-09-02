package servent.handler;

import app.AppConfig;
import app.Job;
import servent.message.Message;
import servent.message.MessageType;
import servent.message.SubmitJobMessage;

public class SubmitJobHandler implements MessageHandler{

	private Message clientMessage;

	public SubmitJobHandler(Message clientMessage) {
		this.clientMessage = clientMessage;
	}

	@Override
	public void run() {
		if (clientMessage.getMessageType() == MessageType.SUBMIT_JOB) {
			SubmitJobMessage msg = (SubmitJobMessage)clientMessage;
			
			System.out.println("SUBMIT JOB HANDLER  USAO");
			
			synchronized(AppConfig.lock) {
				
				AppConfig.finalJobs = msg.getFinalJobs();
				AppConfig.queueMap = msg.getQueueMap();
				AppConfig.groups = msg.getGroups();
				//mora da se cuva nekako
			
				Job newJob = msg.getFinalJobs().get(AppConfig.myServentInfo);
			
				System.out.println("JOB: " + newJob);
				if (newJob != null)
						try {
							System.out.println("UBACEN NOVI POSAO");
							AppConfig.jobQueue.put(newJob);
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
				else
					System.out.println("We are IDLE.");
			
			}
			
		} else {
			AppConfig.timestampedErrorPrint("SubmitJob handler got a message that is not SUBMIT_JOB");
		}
	}

}
