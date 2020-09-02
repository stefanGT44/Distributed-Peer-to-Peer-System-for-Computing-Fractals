package app;

import java.awt.Point;
import java.io.Serializable;
import java.util.Arrays;

public class Job implements Serializable {

	private String name;
	private Job parent;
	private int N;
	private double P;
	private int W, H;
	private Point[] points;
	
	//trenutno se koristi samo prilikom REORGANIZACIJE, rasparcana matrica roditelja !!!!!
	private int[][] matrix;
	
	//koristi se samo za dohvatanje statusa
	private Long iterations;
	
	public Job(Job parent, String name) {
		this.name = name;
		this.parent = parent;
		this.N = parent.N;
		this.P = parent.P;
		this.W = parent.W;
		this.H = parent.H;
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (!(obj instanceof Job))
			return false;
		Job other = (Job) obj;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		return true;
	}

	public Job() {
	}
	
	public Long getIterations() {
		return iterations;
	}
	
	public void setIterations(Long iterations) {
		this.iterations = iterations;
	}
	
	public void setMatrix(int[][] matrix) {
		this.matrix = matrix;
	}
	
	public int[][] getMatrix() {
		return matrix;
	}
	
	public Job getParent() {
		return parent;
	}
	
	public void setParent(Job parent) {
		this.parent = parent;
	}
	
	public String getName() {
		return name;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	public int getN() {
		return N;
	}
	
	public void setN(int n) {
		N = n;
	}
	
	public double getP() {
		return P;
	}
	
	public void setP(double p) {
		P = p;
	}
	
	public int getW() {
		return W;
	}
	
	public void setW(int w) {
		W = w;
	}
	
	public int getH() {
		return H;
	}
	
	public void setH(int h) {
		H = h;
	}
	
	public Point[] getPoints() {
		return points;
	}
	
	public void setPoints(Point[] points) {
		this.points = points;
	}
	
	@Override
	public String toString() {
		return name + ", N = " + N + ", P = " + P + ", " + W + "x" + H + ", points: " + Arrays.asList(points);
	}
	
}
