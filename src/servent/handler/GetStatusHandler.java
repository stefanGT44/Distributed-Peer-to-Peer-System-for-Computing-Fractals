package servent.handler;

import app.AppConfig;
import servent.message.Message;
import servent.message.MessageType;
import servent.message.TellStatusMessage;
import servent.message.util.MessageUtil;
import worker.FractalWorker;

public class GetStatusHandler implements MessageHandler {

	private Message clientMessage;
	private FractalWorker worker;

	public GetStatusHandler(Message clientMessage, FractalWorker worker) {
		this.clientMessage = clientMessage;
		this.worker = worker;
	}

	@Override
	public void run() {
		if (clientMessage.getMessageType() == MessageType.GET_STATUS) {
			
			TellStatusMessage msg = new TellStatusMessage(AppConfig.myServentInfo, clientMessage.getSenderInfo(), worker.getIterations());
			MessageUtil.sendMessage(msg);
			
		} else {
			AppConfig.timestampedErrorPrint("GetStatus handler got a message that is not GET_STATUS");
		}
	}
	
	
}
