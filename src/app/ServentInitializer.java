package app;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

import servent.message.ConnectMessage;
import servent.message.JoinMessage;
import servent.message.Message;
import servent.message.util.MessageUtil;

public class ServentInitializer implements Runnable {

	@Override
	public void run() {
		try {
			synchronized (AppConfig.leaveLock) {
				if (AppConfig.LEAVING) return;
				
				Socket bootstrap = new Socket(AppConfig.bootstrapInfo.getIpAddress(),
						AppConfig.bootstrapInfo.getListenerPort());
				ObjectOutputStream oos = new ObjectOutputStream(bootstrap.getOutputStream());
				ObjectInputStream ois = new ObjectInputStream(bootstrap.getInputStream());

				oos.writeObject(new ConnectMessage(AppConfig.myServentInfo, AppConfig.bootstrapInfo, "hi"));
				oos.flush();

				Message clientMessage = (Message) ois.readObject();

				// setujemo assajnovan ID da bude nas
				AppConfig.myServentInfo = clientMessage.getRecieverInfo();
				// dodajemo sebe u listu aktivnih cvorova, ne mora da se sinhronizuje jer se
				// sigurno nece konkurentan dogadjaj desiti u ovom trenutku
				AppConfig.activeServents.add(AppConfig.myServentInfo);
				AppConfig.timestampedStandardPrint("Assigned ID = " + AppConfig.myServentInfo.getId());

				if (clientMessage.getMessageText().equals("first")) {
					AppConfig.initialised = true;
					AppConfig.timestampedStandardPrint("Servent initialised");
					AppConfig.timestampedStandardPrint("We are the first node in the network!");
				} else {
					AppConfig.timestampedStandardPrint("Got servent: " + clientMessage.getSenderInfo().getId());
					JoinMessage msg = new JoinMessage(AppConfig.myServentInfo, clientMessage.getSenderInfo());
					MessageUtil.sendMessage(msg);
				}

				oos.close();
				ois.close();
			}

		} catch (IOException e) {
			AppConfig.timestampedErrorPrint("Problem connecting to bootstrap server!");
		} catch (ClassNotFoundException e) {
			AppConfig.timestampedErrorPrint("Cannot cast object to Message type");
		}

	}

}
