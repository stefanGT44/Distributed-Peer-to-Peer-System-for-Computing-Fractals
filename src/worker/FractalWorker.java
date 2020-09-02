package worker;

import java.awt.Point;
import java.util.Random;

import app.AppConfig;
import app.Cancellable;
import app.Job;

public class FractalWorker implements Runnable, Cancellable {

	private volatile boolean working = true;
	private int[][] matrix;
	private long iterations;
	private Job job;

	public FractalWorker() {
	}

	@Override
	public void run() {
		try {
			while (working) {
				
				job = AppConfig.jobQueue.take();
				System.out.println("Got JOB!");
				
				if (job.getName().equals("poison"))
					return;
				
				if (job.getName().equals("stop"))
					continue;
				
				if (job.getMatrix() == null) {
					matrix = new int[job.getH()][job.getW()];
					System.out.println("MATRICA JESTE NULL");
				}
				else {
					System.out.println("MATRICA NIJE NULL");
					matrix = job.getMatrix();
				}
				
				Random r = new Random(System.currentTimeMillis());
				int N = job.getN();
				double P = 1 - job.getP();
				//Point point = getPoint(job.getPoints()[0], job.getPoints()[1], P);
				Point point = job.getPoints()[0];
				
				Job tempJob = null;
				
				System.out.println("Starting WORK!");
				int previous = -1;
				while (working && (tempJob = AppConfig.jobQueue.peek()) == null) {
					
					int rand = r.nextInt(N);
					
					//while (N > 3 && rand == previous)
						//rand = r.nextInt(N);
					
					Point teme = job.getPoints()[rand];
					point = getPoint(point, teme, P);
					synchronized(this) {
						matrix[point.y][point.x] = 1;
					}
					
					previous = rand;
					iterations++;
					
					Thread.sleep(5);
					
				}
				
				if (!working) return;
				

			}
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	
	public synchronized int[][] getMatrix() {
		if (matrix == null) return null;
		int[][] toReturn = new int[job.getH()][job.getW()];
		for (int i = 0; i < job.getH(); i++)
			for (int j = 0; j < job.getW(); j++)
				toReturn[i][j] = matrix[i][j];
		return toReturn;
	}
	
	public Job getIterations() {
		Job j = new Job();
		j.setName(job.getName());
		j.setIterations(iterations);
		return j;
	}

	private Point getPoint(Point a, Point b, double P) {
		return new Point((int) ((1 - P) * a.x + P * b.x), (int) ((1 - P) * a.y + P * b.y));
	}

	@Override
	public void stop() {
		working = false;
	}

}
