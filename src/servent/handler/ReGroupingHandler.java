package servent.handler;

import app.AppConfig;
import servent.message.Message;
import servent.message.MessageType;
import servent.message.ReGroupingReplyMessage;
import servent.message.util.MessageUtil;
import worker.FractalWorker;

public class ReGroupingHandler implements MessageHandler {

	private Message clientMessage;
	private FractalWorker worker;

	public ReGroupingHandler(Message clientMessage, FractalWorker worker) {
		this.clientMessage = clientMessage;
		this.worker = worker;
	}

	@Override
	public void run() {
		if (clientMessage.getMessageType() == MessageType.REGROUPING) {
			
			ReGroupingReplyMessage msg = new ReGroupingReplyMessage(AppConfig.myServentInfo, clientMessage.getSenderInfo(), worker.getMatrix(), clientMessage.getMessageText());
			MessageUtil.sendMessage(msg);

		} else {
			AppConfig.timestampedErrorPrint("ReGroupingHandler handler got a message that is not REGROUPING");
		}
	}
	
}
