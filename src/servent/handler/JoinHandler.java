package servent.handler;

import java.awt.Point;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import app.AppConfig;
import app.Job;
import app.ServentInfo;
import servent.message.Message;
import servent.message.MessageType;
import servent.message.ReorganizeMessage;
import servent.message.SubmitJobMessage;
import servent.message.WelcomeMessage;
import servent.message.util.MessageUtil;
import worker.FractalWorker;

public class JoinHandler implements MessageHandler {

	private Message clientMessage;
	private FractalWorker worker;
	
	public JoinHandler(Message clientMessage, FractalWorker worker) {
		this.clientMessage = clientMessage;
		this.worker = worker;
	}
	
	@Override
	public void run() {
		if (clientMessage.getMessageType() == MessageType.JOIN) {
			AppConfig.timestampedStandardPrint("Join request from: " + clientMessage.getSenderInfo().getId());
			
			synchronized(AppConfig.lock) {
				AppConfig.activeServents.add(clientMessage.getSenderInfo());
				//MOZDA KONFLIKT ZA LEAVING, TREBA PROVERITI ------------------
				
				if (AppConfig.queueMap != null)
					reOrganize(clientMessage.getSenderInfo());
				
				List<ServentInfo> activeServents = new ArrayList<>();
				activeServents.addAll(AppConfig.activeServents);
				if (AppConfig.LEAVING) 
					activeServents.remove(AppConfig.myServentInfo);
				
				
				Map<ServentInfo, Job> copyFinalJobs = null;
				Map<Job, List<Job>> queueMapCopy = null;
				
				if (AppConfig.queueMap != null) {
					copyFinalJobs = new HashMap<>(AppConfig.finalJobs);
					queueMapCopy = new HashMap<>(AppConfig.queueMap);
				}
				
				Map<String, Integer> groupsCopy = null;
				if (AppConfig.groups != null)
					groupsCopy = new HashMap<>(AppConfig.groups);
				
				WelcomeMessage msg = new WelcomeMessage(AppConfig.myServentInfo, clientMessage.getSenderInfo(), activeServents, copyFinalJobs, queueMapCopy, groupsCopy);
				MessageUtil.sendMessage(msg);
			}
			
		} else {
			AppConfig.timestampedErrorPrint("Join handler got a message that is not JOIN");
		}

	}
	
	//ako queue != null -> 
	private void reOrganize(ServentInfo newServent) {
		//nakon sto je dodat kod nas
		
		List<Job> queue = null;
		Job mainJob = null;
		int activeNodes = - 1;
		Entry<String, Integer> minJob = null;
		
		if (AppConfig.groups != null) {
			
			System.out.println("GROUPS nisu NULL");
			
			int counter = 0;
			for (Entry<String, Integer> entry: AppConfig.groups.entrySet()) {
				if (counter++ == 0)
					minJob = entry;
				else if (entry.getValue() < minJob.getValue())
					minJob = entry;
			}
			
			System.out.println("NASO MINJOB: " + minJob.getKey() + " = " + minJob.getValue());
			
			for (Entry<Job, List<Job>> entry: AppConfig.queueMap.entrySet()) {
				if (entry.getKey().getName().equals(minJob.getKey())) {
					mainJob = entry.getKey();
					queue = entry.getValue();
					activeNodes = minJob.getValue() + 1;
					break;
				}
			}
			
			System.out.println("INICIJALIZOVANI PARAMETRI");
			
			AppConfig.groups.put(minJob.getKey(), minJob.getValue() + 1);
			
		} else {
			
			System.out.println("SAMO JEDAN PARALELNI POSAO");
			
			// U ovom slucaju queueMap ima samo JEDAN element, uzimamo ga na ovaj nacin
			for (Entry<Job, List<Job>> entry: AppConfig.queueMap.entrySet()) {
				mainJob = entry.getKey();
				queue = entry.getValue();
			}
			activeNodes = AppConfig.activeServents.size();
		}
		
		System.out.println("PROSLA NOVA PROVERA");
		
		System.out.println(queue);
		
		Job job = queue.get(0);
		
		System.out.println(job);
		
		int N = job.getN();
		List<Job> newJobs = null;
		
		System.out.println("INICIJALIZOVANI PARAMETRI");
		
		System.out.println("[" + job.getName() + "] queueSize = " + queue.size() + " activeNodesInGroup = " + activeNodes);
		
		if (job.equals(AppConfig.finalJobs.get(AppConfig.myServentInfo))) {
			AppConfig.timestampedStandardPrint("Ja vrsim reorganizaciju");
			//ONDA MI VRSTIMO RASPODELU I SALJEMO SVIMA SUBMIT JOB/UPDATE
			
			if (queue.size() + (N -1) <= activeNodes) {
				AppConfig.timestampedStandardPrint("Treba preraspodela");
				//ONDA TREBA REORGANIZACIJA
				
				job = queue.remove(0);
				newJobs = decompose(job);
				queue.addAll(newJobs);
				
				//TRAZIMO SLOBODNE CVOROVE
				List<ServentInfo> idleNodes = new ArrayList<>();
				for (Entry<ServentInfo, Job> entry: AppConfig.finalJobs.entrySet()) {
					if (entry.getValue() == null)
						idleNodes.add(entry.getKey());
				}
				
				for (ServentInfo node: AppConfig.activeServents) {
					if (node.equals(AppConfig.myServentInfo) || newServent.equals(node)) continue;
					if (!AppConfig.finalJobs.containsKey(node))
						idleNodes.add(node);
				}
				
				//NOVIM POSLOVIMA DAJEMO DELOVE MATRICE KOJA BA TRENUTNOM CVORU DO SAD RACUNATA
				for (Job j: newJobs) {
					j.setMatrix(decomposeMatrix(worker.getMatrix(), j));
				}
				
				System.out.println("Novi poslovi: ");
				System.out.println(newJobs);
				
				//NOVE POSLOVE DODELJUJEMO IDLE CVOROVIMA
				int counter = 0;
				for (ServentInfo node: idleNodes) {
					AppConfig.finalJobs.put(node, newJobs.get(counter++));
				}
				
				//DODELJUJEMO SEBI I NOVOM CVORU JEDAN OD NOVIH POSLOVA
				AppConfig.finalJobs.put(newServent, newJobs.get(counter++));
				AppConfig.finalJobs.put(AppConfig.myServentInfo, newJobs.get(counter));
				
				Map<ServentInfo, Job> copyFinalJobs = new HashMap<>(AppConfig.finalJobs);
				Map<Job, List<Job>> queueMapCopy = new HashMap<>(AppConfig.queueMap);
				
				AppConfig.printFinalJobs(copyFinalJobs);
				
				Map<String, Integer> groupsCopy = null;
				if (AppConfig.groups != null)
					groupsCopy = new HashMap<>(AppConfig.groups);
				
				//SALJEMO SVIMA SUBMIT - U HANDLERU AKO JE POSAO ISTI, SAMO UPDATE FINALJOBS I QUEUE, AKO NIJE ONDA KRECE DA RACUNA
				for (Entry<ServentInfo, Job> entry: AppConfig.finalJobs.entrySet()) {
					if (entry.getKey().equals(AppConfig.myServentInfo))
						continue;
					SubmitJobMessage submitMsg = new SubmitJobMessage(AppConfig.myServentInfo, entry.getKey(), copyFinalJobs, queueMapCopy, groupsCopy);
					MessageUtil.sendMessage(submitMsg);
				}
				
				AppConfig.timestampedStandardPrint("Poslat svima submit");
				
				//ODMAH KRECEMO DA RACUNAMO NOVI POSAO
				try {
					AppConfig.jobQueue.put(newJobs.get(counter));
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				
			} else {
				//NE TREBA REORGANIZACIJA
				
				AppConfig.timestampedStandardPrint("ActiveNodes = " + activeNodes + ", N = " + N + ",no need to reOrganize jobs...");
			}
			
		} else {
			//SALJEMO CVORU CIJI JE POSAO PRVI U QUEUE
			
			//NE RADIMO MI, ALI TREBA RASPODELA -> UPDATE GROUPS
			
			//TRAZIMO CVOR CRIJI JE POSAO PRVI U QUEUE
			ServentInfo nodeToContact = null;
			for (Entry<ServentInfo, Job> entry: AppConfig.finalJobs.entrySet()) {
				if (job.equals(entry.getValue())) {
					nodeToContact = entry.getKey();
					break;
				}
			}
			
			AppConfig.timestampedStandardPrint("Reorganizaciju vrsi cvor: " + nodeToContact);
			
			//NJEMU SALJEMO RE-ORGANIZE MESSAGE
			ReorganizeMessage msg = new ReorganizeMessage(newServent, nodeToContact);
			MessageUtil.sendMessage(msg);
		}
		
		
		/*if (queue.size() + (N -1) <= activeNodes) {
			AppConfig.timestampedStandardPrint("Treba preraspodela");
			//ONDA TREBA REORGANIZACIJA
			if (job.equals(AppConfig.finalJobs.get(AppConfig.myServentInfo))) {
				AppConfig.timestampedStandardPrint("Ja vrsim reorganizaciju");
				//ONDA MI VRSTIMO RASPODELU I SALJEMO SVIMA SUBMIT JOB/UPDATE
				
				if (AppConfig.groups != null)
					AppConfig.groups.put(minJob.getKey(), minJob.getValue() + 1);
				
				job = queue.remove(0);
				newJobs = decompose(job);
				queue.addAll(newJobs);
				
				//TRAZIMO SLOBODNE CVOROVE
				List<ServentInfo> idleNodes = new ArrayList<>();
				for (Entry<ServentInfo, Job> entry: AppConfig.finalJobs.entrySet()) {
					if (entry.getValue() == null)
						idleNodes.add(entry.getKey());
				}
				
				for (ServentInfo node: AppConfig.activeServents) {
					if (node.equals(AppConfig.myServentInfo) || newServent.equals(node)) continue;
					if (!AppConfig.finalJobs.containsKey(node))
						idleNodes.add(node);
				}
				
				//NOVIM POSLOVIMA DAJEMO DELOVE MATRICE KOJA BA TRENUTNOM CVORU DO SAD RACUNATA
				for (Job j: newJobs) {
					j.setMatrix(decomposeMatrix(worker.getMatrix(), j));
				}
				
				System.out.println("Novi poslovi: ");
				System.out.println(newJobs);
				
				//NOVE POSLOVE DODELJUJEMO IDLE CVOROVIMA
				int counter = 0;
				for (ServentInfo node: idleNodes) {
					AppConfig.finalJobs.put(node, newJobs.get(counter++));
				}
				
				//DODELJUJEMO SEBI I NOVOM CVORU JEDAN OD NOVIH POSLOVA
				AppConfig.finalJobs.put(newServent, newJobs.get(counter++));
				AppConfig.finalJobs.put(AppConfig.myServentInfo, newJobs.get(counter));
				
				Map<ServentInfo, Job> copyFinalJobs = new HashMap<>(AppConfig.finalJobs);
				Map<Job, List<Job>> queueMapCopy = new HashMap<>(AppConfig.queueMap);
				
				Map<String, Integer> groupsCopy = null;
				if (AppConfig.groups != null)
					groupsCopy = new HashMap<>(AppConfig.groups);
				
				//SALJEMO SVIMA SUBMIT - U HANDLERU AKO JE POSAO ISTI, SAMO UPDATE FINALJOBS I QUEUE, AKO NIJE ONDA KRECE DA RACUNA
				for (Entry<ServentInfo, Job> entry: AppConfig.finalJobs.entrySet()) {
					if (entry.getKey().equals(AppConfig.myServentInfo))
						continue;
					SubmitJobMessage submitMsg = new SubmitJobMessage(AppConfig.myServentInfo, entry.getKey(), copyFinalJobs, queueMapCopy, groupsCopy);
					MessageUtil.sendMessage(submitMsg);
				}
				
				AppConfig.timestampedStandardPrint("Poslat svima submit");
				
				//ODMAH KRECEMO DA RACUNAMO NOVI POSAO
				try {
					AppConfig.jobQueue.put(newJobs.get(counter));
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			} else {
				//SALJEMO CVORU CIJI JE POSAO PRVI U QUEUE
				
				//TRAZIMO CVOR CRIJI JE POSAO PRVI U QUEUE
				ServentInfo nodeToContact = null;
				for (Entry<ServentInfo, Job> entry: AppConfig.finalJobs.entrySet()) {
					if (job.equals(entry.getValue())) {
						nodeToContact = entry.getKey();
						break;
					}
				}
				
				AppConfig.timestampedStandardPrint("Reorganizaciju vrsi cvor: " + nodeToContact);
				
				//NJEMU SALJEMO RE-ORGANIZE MESSAGE
				ReorganizeMessage msg = new ReorganizeMessage(newServent, nodeToContact);
				MessageUtil.sendMessage(msg);
			}
			
			
		} else {
			//NE TREBA REORGANIZACIJA
			
			if (AppConfig.groups != null)
				AppConfig.groups.put(minJob.getKey(), minJob.getValue() + 1);	
			
			AppConfig.timestampedStandardPrint("ActiveNodes = " + activeNodes + ", N = " + N + ",no need to reOrganize jobs...");
		}*/
	}
	
	//bounds[0] = bottom
	//bounds[1] = left
	//bounds[2] = top
	//bounds[3] = right
	private int[][] decomposeMatrix(int[][] matrix, Job job) {
		Point[] bounds = getJobBounds(job);
		int[][] newMatrix = new int[job.getH()][job.getW()];
		for (int i = bounds[0].y; i < bounds[2].y; i++) {
			for (int j = bounds[1].x; j < bounds[3].x; j++) {
				newMatrix[i][j] = matrix[i][j];
			}
		}
		return newMatrix;
	}
	
	//find smallest rectangle that contains job polygon
	private Point[] getJobBounds(Job job) {
		Point bottom = job.getPoints()[0];
		Point left = job.getPoints()[0];
		Point top = job.getPoints()[0];
		Point right = job.getPoints()[0];
		for (Point p: job.getPoints()) {
			if (p.y < bottom.y)
				bottom = p;
			if (p.y > top.y)
				top = p;
			if (p.x < left.x)
				left = p;
			if (p.x > right.x)
				right = p;
		}
		Point[] points = {bottom, left, top, right};
		return points;
	}
	
	//find smaller polygon
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
