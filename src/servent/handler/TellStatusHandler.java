package servent.handler;

import java.util.Map.Entry;

import app.AppConfig;
import app.Job;
import app.ServentInfo;
import servent.message.Message;
import servent.message.MessageType;
import servent.message.TellStatusMessage;
import worker.FractalWorker;

public class TellStatusHandler implements MessageHandler {

	private Message clientMessage;
	private FractalWorker worker;

	public TellStatusHandler(Message clientMessage, FractalWorker worker) {
		this.clientMessage = clientMessage;
		this.worker = worker;
	}

	@Override
	public void run() {
		if (clientMessage.getMessageType() == MessageType.TELL_STATUS) {
			synchronized (AppConfig.lock) {
				TellStatusMessage msg = (TellStatusMessage) clientMessage;

				AppConfig.waitingStatusReplies.put(msg.getSenderInfo(), msg.getJob());

				boolean test = true;
				for (Entry<ServentInfo, Job> entry : AppConfig.waitingStatusReplies.entrySet()) {
					if (entry.getValue() == null) {
						test = false;
						break;
					}
				}

				if (test) {
					System.out.println("Svi odgovorili");

					if (AppConfig.finalJobs.get(AppConfig.myServentInfo) != null
							&& AppConfig.waitingStatusReplies.size() != 1)
						AppConfig.waitingStatusReplies.put(AppConfig.myServentInfo, worker.getIterations());

					AppConfig.timestampedStandardPrint("STATUS: ");
					for (Entry<ServentInfo, Job> entry : AppConfig.waitingStatusReplies.entrySet()) {
						AppConfig.timestampedStandardPrint(
								"Servent[" + entry.getKey().getId() + "] - job [" + entry.getValue().getName() + "]: "
										+ entry.getValue().getIterations() + " iterations");
					}

				}

			}
		} else {
			AppConfig.timestampedErrorPrint("TellStatus handler got a message that is not TELL_STATUS");
		}
	}

}
