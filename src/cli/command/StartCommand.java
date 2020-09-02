package cli.command;

import java.awt.Point;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import app.AppConfig;
import app.Job;
import app.ServentInfo;
import servent.message.ReGroupingMessage;
import servent.message.SubmitJobMessage;
import servent.message.util.MessageUtil;

public class StartCommand implements CLICommand {

	@Override
	public String commandName() {
		return "start";
	}

	@Override
	public void execute(String args) {
		synchronized(AppConfig.lock) {
		if (!AppConfig.initialised) {
			AppConfig.timestampedStandardPrint("Servent not initialised yet...");
			return;
		}
		
		if (AppConfig.LEAVING) {
			AppConfig.timestampedStandardPrint("Unable to perform command while shutting down...");
			return;
		}
		
		Job original = null;
		Job job = null;
		
		for (Job j: AppConfig.jobs) {
			if (j.getName().equals(args)) {
				job = j;
				original = j;
				break;
			}
		}
		
		if (job != null) {
			if (AppConfig.queueMap != null && AppConfig.queueMap.containsKey(job)) {
				System.out.println("Job " + job.getName() + " already started.");
				return;
			}
			
			System.out.println("Podela posla za: " + job.getName());
			
			System.out.println("MAPA: " + AppConfig.queueMap);
			
			if (AppConfig.queueMap != null && AppConfig.queueMap.keySet().size() > 0) {
				AppConfig.waitingReGrouping.clear();
				System.out.println("ReGrouping required, sending for results...");
				for (Entry<ServentInfo, Job> entry: AppConfig.finalJobs.entrySet()) {
					if (entry.getKey().equals(AppConfig.myServentInfo) || entry.getValue() == null)
						continue;
					ReGroupingMessage msg = new ReGroupingMessage(AppConfig.myServentInfo, entry.getKey(), job.getName());
					AppConfig.waitingReGrouping.put(entry.getKey(), null);
					MessageUtil.sendMessage(msg);
				}
				return;
			}
			
			List<Job> queue = new ArrayList<>();
			queue.add(job);
			
			int activeNodes = AppConfig.activeServents.size();
			int N = job.getN();
			
			//podela posla
			while (queue.size() + (N -1) <= activeNodes) {
				job = queue.remove(0);
				queue.addAll(decompose(job));
			}
			
			System.out.println("Broj uposlenih cvorova: " + queue.size());
			
			Map<ServentInfo, Job> finalJobs = new HashMap<>();
			int counter = 0;
			for (ServentInfo servent: AppConfig.activeServents) {
				if (queue.size() > counter)
					finalJobs.put(servent, queue.get(counter++));
				else
					finalJobs.put(servent, null);
			}
			
			System.out.println(finalJobs);
			
			Map<Job, List<Job>> queueMap = new HashMap<>();
			queueMap.put(original, queue);
			
			AppConfig.finalJobs = finalJobs;
			AppConfig.queueMap = queueMap;
			
			Map<ServentInfo, Job> finalJobsCopy = new HashMap<>(finalJobs);
			Map<Job, List<Job>> queueMapCopy = new HashMap<>(queueMap);
			
			for (Entry<ServentInfo, Job> entry: finalJobs.entrySet()) {
				if (entry.getKey().equals(AppConfig.myServentInfo)) {
					if (entry.getValue() != null)
						AppConfig.jobQueue.add(entry.getValue());
				} else {
					//if (entry.getValue() != null) {
					SubmitJobMessage jobMessage = new SubmitJobMessage(AppConfig.myServentInfo, entry.getKey(), finalJobsCopy, queueMapCopy, null);
					MessageUtil.sendMessage(jobMessage);
					//}
				}
			}
			
			System.out.println("Poslati poslovi svima...");
			
		} else {
			System.out.println("Navedeni posao " + args + " ne postoji trenutno, OVO TREBA PROMENITI!");
		}
		}
	}
	
	private List<Job> decompose(Job job) {
		List<Job> list = new ArrayList<>();
		for (int i = 0; i < job.getN(); i++) {
			
			Point[] points = new Point[job.getN()];
			points[i] = job.getPoints()[i]; 
			
			for (int j = 0; j < job.getN(); j++) {
				if (j == i) continue;
				points[j] = getPoint(points[i], job.getPoints()[j], job.getP());
			}
			
			Job job1 = null;
			if (job.getParent() == null)
				job1 = new Job(job, job.getName() + "_" + i);
			else
				job1 = new Job(job, job.getName() + i);
			
			job1.setPoints(points);
			list.add(job1);
		}
		return list;
	}
	
	//C = od A prema B za P% udaljena
	private Point getPoint(Point a, Point b, double P) {
		return new Point((int)((1 - P) * a.x + P * b.x), (int)((1 - P) * a.y + P * b.y));
	}

}
