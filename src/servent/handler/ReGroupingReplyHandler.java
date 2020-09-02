package servent.handler;

import java.awt.Point;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import app.AppConfig;
import app.Job;
import app.ServentInfo;
import servent.message.Message;
import servent.message.MessageType;
import servent.message.ReGroupingReplyMessage;
import servent.message.SubmitJobMessage;
import servent.message.util.MessageUtil;
import worker.FractalWorker;

public class ReGroupingReplyHandler implements MessageHandler {

	private Message clientMessage;
	private FractalWorker worker;

	public ReGroupingReplyHandler(Message clientMessage, FractalWorker worker) {
		this.clientMessage = clientMessage;
		this.worker = worker;
	}

	@Override
	public void run() {
		if (clientMessage.getMessageType() == MessageType.REGROUPING_REPLY) {

			synchronized (AppConfig.lock) {
				ReGroupingReplyMessage msg = (ReGroupingReplyMessage) clientMessage;
				AppConfig.waitingReGrouping.put(msg.getSenderInfo(), msg.getMatrix());
				AppConfig.ReGroupingHelp.put(msg.getMessageText(), msg.getMatrix());

				boolean test = true;
				for (Entry<ServentInfo, int[][]> entry : AppConfig.waitingReGrouping.entrySet()) {
					if (entry.getValue() == null) {
						test = false;
						break;
					}
				}

				if (test) {

					if (AppConfig.finalJobs.get(AppConfig.myServentInfo) != null) {
						int mat[][] = worker.getMatrix();
						AppConfig.waitingReGrouping.put(AppConfig.myServentInfo, mat);
						AppConfig.ReGroupingHelp.put(AppConfig.finalJobs.get(AppConfig.myServentInfo).getName(), mat);
					}

					System.out.println("SVI ODGOVORILI, RADIMO REORGANIZACIJU POSLA");

					Job job = null;
					Job original = null;
					for (Job j : AppConfig.jobs) {
						if (j.getName().equals(msg.getMessageText())) {
							job = j;
							original = j;
							break;
						}
					}

					// BROJ PARALELNIH POSLOVA/FRAKTALA
					int concurrentJobs = AppConfig.queueMap.keySet().size() + 1;

					System.out.println("Broj paralelnih poslova: " + concurrentJobs);

					// IZRACUNAVANJE KOJEM POSLU PRIPADA KOLIKI BROJ CVOROVA
					int div = AppConfig.activeServents.size() / concurrentJobs;
					int mod = AppConfig.activeServents.size() % concurrentJobs;

					int[] serventGroupSizes = new int[concurrentJobs];
					for (int i = 0; i < concurrentJobs; i++)
						serventGroupSizes[i] = div;

					for (int i = 0; i < mod; i++)
						serventGroupSizes[i % concurrentJobs] += 1;

					int counter = 0;
					Map<Job, List<Job>> newQueueMap = new HashMap<>();

					// SJEDINJAVANJE REZULTATA POSTOJECIH POSLOVA, I RASPARCAVANJE NA NOVE DELOVE

					Map<String, Integer> sizes = new HashMap<>();
					for (Entry<Job, List<Job>> entry : AppConfig.queueMap.entrySet()) {
						int activeNodes = serventGroupSizes[counter];

						System.out.println(
								"Broj cvorova dodeljen poslu[" + entry.getKey().getName() + "] = " + activeNodes);

						sizes.put(entry.getKey().getName(), activeNodes);
						int N = entry.getKey().getN();

						// SJEDINJAVANJE MATRICA
						int[][] combinedMatrix = combineMatrix(entry.getValue(), entry.getKey());

						List<Job> queue = new ArrayList<>();
						queue.add(entry.getKey());

						// PODELA POSLA CVOROVIMA UNUTAR GRUPE
						while (queue.size() + (N - 1) <= activeNodes) {
							job = queue.remove(0);
							job.setMatrix(null);
							queue.addAll(decompose(job));
						}

						// DELJENJE MATRICE NA NOVE DELOVE
						for (Job j : queue)
							j.setMatrix(decomposeMatrix(combinedMatrix, j));

						// PAMTIMO IZRACUNATU PODELU POSLA
						newQueueMap.put(entry.getKey(), queue);
						counter++;
					}

					// ZATIM SE IZRACUNAVA PODELA POSLA ZA NOVI POSAO/FRAKTAL
					int activeNodes = serventGroupSizes[counter];

					int N = original.getN();

					System.out.println("Broj cvorova dodeljen poslu[" + original.getName() + "] = " + activeNodes);

					sizes.put(original.getName(), activeNodes);

					List<Job> queue = new ArrayList<>();
					queue.add(original);

					while (queue.size() + (N - 1) <= activeNodes) {
						job = queue.remove(0);
						queue.addAll(decompose(job));
					}

					newQueueMap.put(original, queue);

					// DODELJIVANJE PODELJEN POSAO CVOROVIMA
					counter = 0;
					Map<ServentInfo, Job> finalJobs = new HashMap<>();
					int temp = 0;

					for (Entry<Job, List<Job>> entry : newQueueMap.entrySet()) {

						int i = 0;
						int activeServents = sizes.get(entry.getKey().getName());

						for (Job j : entry.getValue()) {
							finalJobs.put(AppConfig.activeServents.get(temp + (i++)), j);
						}

						while (i < activeServents) {
							finalJobs.put(AppConfig.activeServents.get(temp + (i++)), null);
						}

						temp += i;
					}

					AppConfig.printFinalJobs(finalJobs);

					AppConfig.finalJobs = finalJobs;
					AppConfig.queueMap = newQueueMap;
					AppConfig.groups = sizes;

					Map<ServentInfo, Job> finalJobsCopy = new HashMap<>(finalJobs);
					Map<Job, List<Job>> queueMapCopy = new HashMap<>(newQueueMap);
					Map<String, Integer> groupsCopy = new HashMap<>(sizes);

					// POSLATI SVIMA NOVI POSLOVI
					for (ServentInfo servent : AppConfig.activeServents) {
						if (servent.equals(AppConfig.myServentInfo))
							continue;
						SubmitJobMessage jobMessage = new SubmitJobMessage(AppConfig.myServentInfo, servent,
								finalJobsCopy, queueMapCopy, groupsCopy);
						MessageUtil.sendMessage(jobMessage);
					}

					if (finalJobs.get(AppConfig.myServentInfo) != null)
						AppConfig.jobQueue.add(finalJobs.get(AppConfig.myServentInfo));

				}

			}
		} else {
			AppConfig
					.timestampedErrorPrint("ReGroupingReplyHandler handler got a message that is not REGROUPING_REPLY");
		}

	}

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

	// find smallest rectangle that contains job polygon
	private Point[] getJobBounds(Job job) {
		Point bottom = job.getPoints()[0];
		Point left = job.getPoints()[0];
		Point top = job.getPoints()[0];
		Point right = job.getPoints()[0];
		for (Point p : job.getPoints()) {
			if (p.y < bottom.y)
				bottom = p;
			if (p.y > top.y)
				top = p;
			if (p.x < left.x)
				left = p;
			if (p.x > right.x)
				right = p;
		}
		Point[] points = { bottom, left, top, right };
		return points;
	}

	private int[][] combineMatrix(List<Job> queue, Job mainJob) {
		int[][] matrix = new int[mainJob.getH()][mainJob.getW()];

		for (Entry<ServentInfo, Job> entry : AppConfig.finalJobs.entrySet()) {

			if (entry.getValue() != null && entry.getValue().getName().startsWith(mainJob.getName())) {

				int[][] mat = AppConfig.waitingReGrouping.get(entry.getKey());

				for (int i = 0; i < matrix.length; i++) {
					for (int j = 0; j < matrix[0].length; j++) {

						if (mat[i][j] == 1) {
							matrix[i][j] = 1;
						}

					}
				}

			}
		}

		return matrix;
	}

	private List<Job> decompose(Job job) {
		List<Job> list = new ArrayList<>();
		for (int i = 0; i < job.getN(); i++) {

			Point[] points = new Point[job.getN()];
			points[i] = job.getPoints()[i];

			for (int j = 0; j < job.getN(); j++) {
				if (j == i)
					continue;
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

	// C = od A prema B za P% udaljena
	private Point getPoint(Point a, Point b, double P) {
		return new Point((int) ((1 - P) * a.x + P * b.x), (int) ((1 - P) * a.y + P * b.y));
	}

}
