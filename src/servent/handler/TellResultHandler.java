package servent.handler;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Map.Entry;

import javax.imageio.ImageIO;

import app.AppConfig;
import app.ServentInfo;
import servent.message.Message;
import servent.message.MessageType;
import servent.message.TellResultMessage;
import worker.FractalWorker;

public class TellResultHandler implements MessageHandler {

	private FractalWorker worker;
	private Message clientMessage;

	public TellResultHandler(Message clientMessage, FractalWorker worker) {
		this.clientMessage = clientMessage;
		this.worker = worker;
	}

	@Override
	public void run() {
		if (clientMessage.getMessageType() == MessageType.TELL_RESULT) {
			synchronized (AppConfig.lock) {
				TellResultMessage msg = (TellResultMessage) clientMessage;
				AppConfig.waitingForReply.put(clientMessage.getSenderInfo(), msg.getMatrix());

				// OVO MORA DA SE SINHRONIZUJE....
				boolean test = true;
				for (Entry<ServentInfo, int[][]> entry : AppConfig.waitingForReply.entrySet()) {
					if (entry.getValue() == null) {
						test = false;
						break;
					}
				}

				if (test) {
					System.out.println("Svi odgovorili");

					if (AppConfig.finalJobs.get(AppConfig.myServentInfo) != null
							&& AppConfig.waitingForReply.size() != 1)
						AppConfig.waitingForReply.put(AppConfig.myServentInfo, worker.getMatrix());

					BufferedImage img = new BufferedImage(msg.getMatrix()[0].length, msg.getMatrix().length,
							BufferedImage.TYPE_INT_RGB);

					for (int i = 0; i < msg.getMatrix().length; i++) {
						for (int j = 0; j < msg.getMatrix()[0].length; j++) {
							boolean red = false;
							for (int[][] mat : AppConfig.waitingForReply.values()) {
								if (mat[i][j] == 1) {
									red = true;
									break;
								}
							}

							if (red) {
								img.setRGB(j, msg.getMatrix().length - 1 - i, Color.RED.getRGB());
							} else {
								img.setRGB(j, msg.getMatrix().length - 1 - i, Color.WHITE.getRGB());
							}

						}
					}

					File outputFile = new File("slika" + AppConfig.myServentInfo.getId() + ".png");
					try {
						ImageIO.write(img, "png", outputFile);
						System.out.println("SACUVANA SLIKA");
					} catch (IOException e) {
						e.printStackTrace();
					}

				}

			}
		} else {
			AppConfig.timestampedErrorPrint("TellResult handler got a message that is not TELL_RESULT");
		}
	}

}
