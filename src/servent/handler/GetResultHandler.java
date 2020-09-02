package servent.handler;

import app.AppConfig;
import servent.message.Message;
import servent.message.MessageType;
import servent.message.TellResultMessage;
import servent.message.util.MessageUtil;
import worker.FractalWorker;

public class GetResultHandler implements MessageHandler {

	private Message clientMessage;
	private FractalWorker worker;

	public GetResultHandler(Message clientMessage, FractalWorker worker) {
		this.clientMessage = clientMessage;
		this.worker = worker;
	}

	@Override
	public void run() {
		if (clientMessage.getMessageType() == MessageType.GET_RESULT) {
			
			// TREBA PROSLE
			TellResultMessage msg = new TellResultMessage(AppConfig.myServentInfo, clientMessage.getSenderInfo(), worker.getMatrix());
			MessageUtil.sendMessage(msg);
			
		} else {
			AppConfig.timestampedErrorPrint("GetResult handler got a message that is not GET_RESULT");
		}
	}
	
}
