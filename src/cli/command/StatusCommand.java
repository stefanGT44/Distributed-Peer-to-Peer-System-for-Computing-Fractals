package cli.command;

import java.util.Map.Entry;

import app.AppConfig;
import app.Job;
import app.ServentInfo;
import servent.message.GetStatusMessage;
import servent.message.util.MessageUtil;
import worker.FractalWorker;

public class StatusCommand implements CLICommand {

	private FractalWorker worker;
	
	public StatusCommand(FractalWorker worker) {
		this.worker = worker;
	}
	
	@Override
	public String commandName() {
		return "status";
	}

	@Override
	public void execute(String args) {
		synchronized(AppConfig.lock) {
		if (AppConfig.queueMap == null) {
			System.out.println("There are 0 active jobs.");
			return;
		}
		
		if (args == null) {
			
			Job temp = new Job();
			
			if (AppConfig.queueMap.keySet().size() == 1 && AppConfig.finalJobs.get(AppConfig.myServentInfo) != null &&
				AppConfig.queueMap.containsKey(AppConfig.finalJobs.get(AppConfig.myServentInfo))) {
				//onda smo samo MI
				printStatus("null");
			}
			
			AppConfig.waitingStatusReplies.clear();
			for (Entry<ServentInfo, Job> entry: AppConfig.finalJobs.entrySet()) {
				if (AppConfig.myServentInfo.equals(entry.getKey()))
					continue;
				if (entry.getValue() != null) {
					AppConfig.waitingStatusReplies.put(entry.getKey(), null);
					GetStatusMessage msg = new GetStatusMessage(AppConfig.myServentInfo, entry.getKey());
					MessageUtil.sendMessage(msg);
				}
			}
			
			return;
		}
		
		String data[] = args.split(" ");
		
		if (data.length == 1) {
			
			Job j = new Job();
			j.setName(args);
			
			if (AppConfig.queueMap.containsKey(j)) {
				
				if (AppConfig.finalJobs.get(AppConfig.myServentInfo) != null && AppConfig.finalJobs.get(AppConfig.myServentInfo).getName().equals(args)) {
					//onda smo mi
					printStatus("1");
					return;
				}
				
				AppConfig.waitingStatusReplies.clear();
				for (Entry<ServentInfo, Job> entry : AppConfig.finalJobs.entrySet()) {
					if (AppConfig.myServentInfo.equals(entry.getKey()))
						continue;
					if (entry.getValue() != null && entry.getValue().getName().startsWith(args)) {
						AppConfig.waitingStatusReplies.put(entry.getKey(), null);
						GetStatusMessage msg = new GetStatusMessage(AppConfig.myServentInfo, entry.getKey());
						MessageUtil.sendMessage(msg);
					}
				}
				
			} else {
				
				System.out.println("Job " + args + " doesn't exist or isn't active.");
				
			}
			
		} else if (data.length == 2) {
			
			Job j = new Job();
			j.setName(data[0]);
			
			if (!AppConfig.queueMap.containsKey(j)) {
				System.out.println("Job " + data[0] + " doesn't exist");
				return;
			}
			
			if (AppConfig.queueMap.get(j).size() == 1) {
				System.out.println("FractalID [" + data[1] + "] of job " + data[0] + " doesn't exist.");
				return;
			}
			
			boolean test = false;
			for (Entry<ServentInfo, Job> entry: AppConfig.finalJobs.entrySet()) {
				if (entry.getValue() != null) {
					String[] nameData = entry.getValue().getName().split("_");
					if (nameData[0].equals(data[0]) &&  nameData[nameData.length - 1].equals(data[1])) {
						if (entry.getKey().equals(AppConfig.myServentInfo)) {
							//posao je kod nas
							printStatus("2");
							
						} else {
							//saljemo get result cvoru kod koga je posao
							AppConfig.waitingStatusReplies.clear();
							AppConfig.waitingStatusReplies.put(entry.getKey(), null);
							GetStatusMessage msg = new GetStatusMessage(AppConfig.myServentInfo, entry.getKey());
							MessageUtil.sendMessage(msg);
						}
						test = true;
						break;
					}
				}
			}
			
			if (!test)
				System.out.println("FractalID [" + data[1] + "] of job " + data[0] + " doesn't exist.");
			
		} else {
			System.out.println("Status command accepts only 2 arguments.");
		}
		}
	}
	
	private void printStatus(String type) {
		AppConfig.timestampedStandardPrint("STATUS: ");
		Job job = worker.getIterations();
		AppConfig.timestampedStandardPrint("[" +job.getName() + "]: " + worker.getIterations().getIterations() + " iterations");
	}

}
