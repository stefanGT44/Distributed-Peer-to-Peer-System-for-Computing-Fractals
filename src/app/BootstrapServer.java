package app;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Scanner;

import servent.message.ConnectMessage;
import servent.message.Message;

public class BootstrapServer {

	private volatile boolean working = true;
	private List<ServentInfo> activeServents;
	private int counter = 0;
	
	private class CLIWorker implements Runnable {
		@Override
		public void run() {
			Scanner sc = new Scanner(System.in);
			
			String line;
			while(true) {
				line = sc.nextLine();
				
				if (line.equals("stop")) {
					working = false;
					break;
				}
			}
			
			sc.close();
		}
	}
	
	public BootstrapServer() {
		activeServents = new ArrayList<>();
	}
	
	public void doBootstrap(int bsPort) {
		Thread cliThread = new Thread(new CLIWorker());
		cliThread.start();
		
		ServerSocket listenerSocket = null;
		try {
			listenerSocket = new ServerSocket(bsPort);
			listenerSocket.setSoTimeout(1000);
		} catch (IOException e1) {
			AppConfig.timestampedErrorPrint("Problem while opening listener socket.");
			System.exit(0);
		}
		
		Random rand = new Random(System.currentTimeMillis());
		
		while (working) {
			try {
				Socket newServentSocket = listenerSocket.accept();
				
				ObjectOutputStream oos = new ObjectOutputStream(newServentSocket.getOutputStream());
				ObjectInputStream ois = new ObjectInputStream(newServentSocket.getInputStream());
				
				Message clientMessage = (Message) ois.readObject();
				
				if (clientMessage.getMessageText().equals("hi")) {
					if (activeServents.isEmpty()) {
						ServentInfo assigned = new ServentInfo(clientMessage.getSenderInfo().getIpAddress(), clientMessage.getSenderInfo().getListenerPort(),
								counter++);
						activeServents.add(assigned);
						oos.writeObject(new ConnectMessage(AppConfig.bootstrapInfo, assigned, "first"));
						oos.flush();
						AppConfig.timestampedStandardPrint("Added first Node");
					} else {
						ServentInfo assigned = new ServentInfo(clientMessage.getSenderInfo().getIpAddress(), clientMessage.getSenderInfo().getListenerPort(),
								counter++);
						ServentInfo randomNode = activeServents.get(rand.nextInt(activeServents.size()));
						oos.writeObject(new ConnectMessage(randomNode, assigned, "rand"));
						oos.flush();
						AppConfig.timestampedStandardPrint("Assigning id " + (counter - 1));
					}
				} else if (clientMessage.getMessageText().equals("joined")){
					//joined
					activeServents.add(clientMessage.getSenderInfo());
					AppConfig.timestampedStandardPrint("Added active servent " + clientMessage.getSenderInfo().getId());
				} else {
					//leave
					activeServents.remove(clientMessage.getSenderInfo());
					AppConfig.timestampedStandardPrint("Servent " + clientMessage.getSenderInfo().getId() + " left the network.");
				}
				
				oos.close();
				ois.close();
				
			} catch (SocketTimeoutException e) {
				
			} catch (IOException e) {
				e.printStackTrace();
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
			}
		}
	}
	
	/**
	 * Expects one command line argument - the port to listen on.
	 */
	public static void main(String[] args) {
		if (args.length != 2) {
			AppConfig.timestampedErrorPrint("Bootstrap started without port argument.");
		}
		
		int bsPort = 0;
		try {
			bsPort = Integer.parseInt(args[0]);
		} catch (NumberFormatException e) {
			AppConfig.timestampedErrorPrint("Bootstrap port not valid: " + args[0]);
			System.exit(0);
		}
		
		AppConfig.timestampedStandardPrint("Bootstrap server started on port: " + bsPort);
		
		BootstrapServer bs = new BootstrapServer();
		bs.doBootstrap(bsPort);
	}
}
