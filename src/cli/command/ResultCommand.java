package cli.command;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map.Entry;

import javax.imageio.ImageIO;

import app.AppConfig;
import app.Job;
import app.ServentInfo;
import servent.message.GetResultMessage;
import servent.message.util.MessageUtil;
import worker.FractalWorker;

public class ResultCommand implements CLICommand {

	private FractalWorker worker;

	public ResultCommand(FractalWorker worker) {
		this.worker = worker;
	}

	@Override
	public String commandName() {
		return "result";
	}

	@Override
	public void execute(String arguments) {
		synchronized(AppConfig.lock) {
		if (arguments == null) {
			System.out.println("Result command expect at least one argument.");
			return;
		}
		if (AppConfig.queueMap == null) {
			System.out.println("There are 0 active jobs.");
			return;
		}
		
		String args[] = arguments.split(" ");
		if (args.length != 1 && args.length != 2) {
			AppConfig.timestampedStandardPrint(
					"Invalid arguments. Valid example: result job_name [job_id], arguments within [] are optional.");
			return;
		}

		if (args.length == 1) {
			//fali u slucaju da samo mi radimo ovaj
			if (AppConfig.finalJobs.get(AppConfig.myServentInfo) != null && AppConfig.finalJobs.get(AppConfig.myServentInfo).getName().equals(arguments)) {
				//onda smo mi
				generateImage();
				return;
			} 
			
			Job temp = new Job();
			temp.setName(arguments);
			
			if (!AppConfig.queueMap.containsKey(temp)) {
				System.out.println("KEY SET: ");
				System.out.println(AppConfig.queueMap.keySet());
				AppConfig.timestampedStandardPrint("Job " + arguments + " doesn't exist.");
				return;
			}
			
			AppConfig.waitingForReply.clear();
			for (Entry<ServentInfo, Job> entry : AppConfig.finalJobs.entrySet()) {
				if (entry.getKey().equals(AppConfig.myServentInfo))
					continue;
				if (entry.getValue() != null && entry.getValue().getName().startsWith(arguments)) {
					AppConfig.waitingForReply.put(entry.getKey(), null);
					GetResultMessage msg = new GetResultMessage(AppConfig.myServentInfo, entry.getKey());
					MessageUtil.sendMessage(msg);
				}
			}

			System.out.println("Poslato svima get results");

		} else {
			
			Job temp = new Job();
			temp.setName(args[0]);
			
			if (!AppConfig.queueMap.containsKey(temp)) {
				AppConfig.timestampedStandardPrint("Job " + args[0] + " doesn't exist.");
				return;
			}
			
			if (AppConfig.queueMap.get(temp).size() == 1) {
				System.out.println("FractalID [" + args[1] + "] of job " + args[0] + " doesn't exist.");
				return;
			}
			
			boolean test = false;
			for (Entry<ServentInfo, Job> entry: AppConfig.finalJobs.entrySet()) {
				if (entry.getValue() != null) {
					String[] nameData = entry.getValue().getName().split("_");
					if (nameData[0].equals(args[0]) &&  nameData[nameData.length - 1].equals(args[1])) {
						if (entry.getKey().equals(AppConfig.myServentInfo)) {
							//posao je kod nas
							generateImage();
							
						} else {
							//saljemo get result cvoru kod koga je posao
							AppConfig.waitingForReply.clear();
							AppConfig.waitingForReply.put(entry.getKey(), null);
							GetResultMessage msg = new GetResultMessage(AppConfig.myServentInfo, entry.getKey());
							MessageUtil.sendMessage(msg);
						}
						test = true;
						break;
					}
				}
			}
			
			if (!test)
				System.out.println("FractalID [" + args[1] + "] of job " + args[0] + " doesn't exist.");
			
		}

		// proveriti da li smo jedini... u primeru koji cu sad testirati NISMO
		}
	}
	
	private void generateImage() {
		int[][] matrix = worker.getMatrix();
		BufferedImage img = new BufferedImage(matrix[0].length, matrix.length, BufferedImage.TYPE_INT_RGB);
		
		for (int i = 0; i < matrix.length; i++) {
			for (int j = 0; j < matrix[0].length; j++) {
				if (matrix[i][j] == 1)
					img.setRGB(j, matrix.length - 1 - i, Color.RED.getRGB());
				else
					img.setRGB(j, matrix.length - 1 - i, Color.WHITE.getRGB());
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
