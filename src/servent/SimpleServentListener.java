package servent;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import app.AppConfig;
import app.Cancellable;
import cli.CLIParser;
import servent.handler.GetResultHandler;
import servent.handler.GetStatusHandler;
import servent.handler.JoinHandler;
import servent.handler.LeaveHandler;
import servent.handler.MessageHandler;
import servent.handler.MissingNodesHandler;
import servent.handler.NullHandler;
import servent.handler.ReGroupingHandler;
import servent.handler.ReGroupingReplyHandler;
import servent.handler.ReorganizeHandler;
import servent.handler.SubmitJobHandler;
import servent.handler.TellResultHandler;
import servent.handler.TellStatusHandler;
import servent.handler.UpdateHandler;
import servent.handler.WelcomeHandler;
import servent.message.Message;
import servent.message.util.MessageUtil;
import worker.FractalWorker;

public class SimpleServentListener implements Runnable, Cancellable {

	private volatile boolean working = true;
	private long lastMessage = System.currentTimeMillis();
	private CLIParser parser;
	private FractalWorker fractalWorker;
	
	public SimpleServentListener() {
		
	}

	/*
	 * Thread pool for executing the handlers. Each client will get it's own handler thread.
	 */
	private final ExecutorService threadPool = Executors.newWorkStealingPool();
	
	@Override
	public void run() {
		ServerSocket listenerSocket = null;
		try {
			listenerSocket = new ServerSocket(AppConfig.myServentInfo.getListenerPort(), 100);
			/*
			 * If there is no connection after 1s, wake up and see if we should terminate.
			 */
			listenerSocket.setSoTimeout(1000);
		} catch (IOException e) {
			AppConfig.timestampedErrorPrint("Couldn't open listener socket on: " + AppConfig.myServentInfo.getListenerPort());
			System.exit(0);
		}
		
		
		while (working) {
			try {
				Message clientMessage;
				
				Socket clientSocket = listenerSocket.accept();
				
				//GOT A MESSAGE! <3
				clientMessage = MessageUtil.readMessage(clientSocket);
				
				lastMessage = System.currentTimeMillis();
				
				MessageHandler messageHandler = new NullHandler(clientMessage);
				
				/*
				 * Each message type has it's own handler.
				 * If we can get away with stateless handlers, we will,
				 * because that way is much simpler and less error prone.
				 */
				switch (clientMessage.getMessageType()) {
				case JOIN:
					messageHandler = new JoinHandler(clientMessage, fractalWorker);
					break;
				case WELCOME:
					messageHandler = new WelcomeHandler(clientMessage);
					break;
				case UPDATE:
					messageHandler = new UpdateHandler(clientMessage, fractalWorker);
					break;
				case MISSING_NODES:
					messageHandler = new MissingNodesHandler(clientMessage);
					break;
				case SUBMIT_JOB:
					messageHandler = new SubmitJobHandler(clientMessage);
					break;
				case GET_RESULT:
					messageHandler = new GetResultHandler(clientMessage, fractalWorker);
					break;
				case TELL_RESULT:
					messageHandler = new TellResultHandler(clientMessage, fractalWorker);
					break;
				case REORGANIZE:
					messageHandler = new ReorganizeHandler(clientMessage, fractalWorker);
					break;
				case REGROUPING:
					messageHandler = new ReGroupingHandler(clientMessage, fractalWorker);
					break;
				case REGROUPING_REPLY:
					messageHandler = new ReGroupingReplyHandler(clientMessage, fractalWorker);
					break;
				case GET_STATUS:
					messageHandler = new GetStatusHandler(clientMessage, fractalWorker);
					break;
				case TELL_STATUS:
					messageHandler = new TellStatusHandler(clientMessage, fractalWorker);
					break;
				case LEAVE:
					messageHandler = new LeaveHandler(clientMessage);
					break;
				}
				
				threadPool.submit(messageHandler);
			} catch (SocketTimeoutException timeoutEx) {
				//Uncomment the next line to see that we are waking up every second.
//				AppConfig.timedStandardPrint("Waiting...");
				if (AppConfig.LEAVING && System.currentTimeMillis() - lastMessage >= AppConfig.STRONG_LIMIT) {
					stop();
					parser.stop();
					AppConfig.timestampedStandardPrint("Terminated successfully.");
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	public void setLastMessage() {
		lastMessage = System.currentTimeMillis();
	}
	
	public void setFractalWorker(FractalWorker fractalWorker) {
		this.fractalWorker = fractalWorker;
	}
	
	public void setParser(CLIParser parser) {
		this.parser = parser;
	}

	@Override
	public void stop() {
		this.working = false;
	}

}
