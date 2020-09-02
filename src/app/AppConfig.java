package app;

import java.awt.Point;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.UnknownHostException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * This class contains all the global application configuration stuff.
 * @author stefanGT44
 *
 */
public class AppConfig {

	/**
	 * Convenience access for this servent's information
	 */
	
	public static List<ServentInfo> activeServents = new ArrayList<>();
	public static Object lock = new Object();
	public static Object leaveLock = new Object();
	
	public static ServentInfo myServentInfo;
	public static ServentInfo bootstrapInfo;
	
	public static volatile boolean LEAVING = false;
	public static volatile boolean LEAVE_CONFIRMED = false;
	
	public static int WEAK_LIMIT;
	public static int STRONG_LIMIT;
	public static int SERVENT_COUNT;
	
	public static List<Job> jobs = new ArrayList<>();
	
	
	public static LinkedBlockingQueue<Job> jobQueue = new LinkedBlockingQueue<>();
	
	
	public static Map<ServentInfo, Job> finalJobs;
	public static Map<Job, List<Job>> queueMap;
	public static Map<String, Integer> groups;
	
	//ovo sad mora da bude mapa queue-ova, za svaki zadati posao
	//public static List<Job> queue;
	
	public static Map<String, int[][]> ReGroupingHelp = Collections.synchronizedMap(new HashMap<>());
	public static Map<ServentInfo, int[][]> waitingReGrouping = Collections.synchronizedMap(new HashMap<>());
	
	public static Object replyLock = new Object();
	public static Map<ServentInfo, int[][]> waitingForReply = Collections.synchronizedMap(new HashMap<>());
	
	public static Map<ServentInfo, Job> waitingStatusReplies = Collections.synchronizedMap(new HashMap<>());
	
	public static boolean initialised = false;
	
	/**
	 * Print a message to stdout with a timestamp
	 * @param message message to print
	 */
	public static void timestampedStandardPrint(String message) {
		DateFormat timeFormat = new SimpleDateFormat("HH:mm:ss");
		Date now = new Date();
		
		System.out.println(timeFormat.format(now) + " - " + message);
	}
	
	/**
	 * Print a message to stderr with a timestamp
	 * @param message message to print
	 */
	public static void timestampedErrorPrint(String message) {
		DateFormat timeFormat = new SimpleDateFormat("HH:mm:ss");
		Date now = new Date();
		
		System.err.println(timeFormat.format(now) + " - " + message);
	}
	
	public static void printJobs(List<Job> jobs) {
		for (Job job: jobs)
			AppConfig.timestampedStandardPrint("Job: " + job.getName());
	}
	
	public static void printFinalJobs(Map<ServentInfo, Job> map) {
		for (Entry<ServentInfo, Job> entry: map.entrySet()) {
			if (entry.getValue() != null)
				System.out.println("servent " + entry.getKey().getId() + ": job = " + entry.getValue().getName());
			else
				System.out.println("servent " + entry.getKey().getId() + ": job = null");
		}
	}
	
	/**
	 * Reads a config file. Should be called once at start of app.
	 * The config file should be of the following format:
	 * <br/>
	 * <code><br/>
	 * servent_count=3 			- number of servents in the system <br/>
	 * chord_size=64			- maximum value for Chord keys <br/>
	 * bs.port=2000				- bootstrap server listener port <br/>
	 * servent0.port=1100 		- listener ports for each servent <br/>
	 * servent1.port=1200 <br/>
	 * servent2.port=1300 <br/>
	 * 
	 * </code>
	 * <br/>
	 * So in this case, we would have three servents, listening on ports:
	 * 1100, 1200, and 1300. A bootstrap server listening on port 2000, and Chord system with
	 * max 64 keys and 64 nodes.<br/>
	 * 
	 * @param configName name of configuration file
	 * @param serventId id of the servent, as used in the configuration file
	 */
	public static void readConfig(String configName, int serventId){
		Properties properties = new Properties();
		try {
			properties.load(new FileInputStream(new File(configName)));
			
		} catch (IOException e) {
			timestampedErrorPrint("Couldn't open properties file. Exiting...");
			System.exit(0);
		}
		
		try {
			String[] bootstrap = properties.getProperty("bootstrap").split(",");
			bootstrapInfo = new ServentInfo(bootstrap[0], Short.parseShort(bootstrap[1]), -1);
			
		} catch (NumberFormatException e) {
			timestampedErrorPrint("Problem reading bootstrap info. Exiting...");
			System.exit(0);
		}
		
		try {
			WEAK_LIMIT = Integer.parseInt(properties.getProperty("weaklimit"));
		} catch (NumberFormatException e) {
			timestampedErrorPrint("Problem reading weaklimit. Exiting...");
			System.exit(0);
		}
		
		try {
			STRONG_LIMIT = Integer.parseInt(properties.getProperty("stronglimit"));
		} catch (NumberFormatException e) {
			timestampedErrorPrint("Problem reading stronglimit. Exiting...");
			System.exit(0);
		}
		
		try {
			SERVENT_COUNT = Integer.parseInt(properties.getProperty("servent_count"));
		} catch (NumberFormatException e) {
			timestampedErrorPrint("Problem reading servent_count. Exiting...");
			System.exit(0);
		}
		
		try {
			
			int jobCount = Integer.parseInt(properties.getProperty("job_count"));
			
			for (int i = 0; i < jobCount; i++) {
				
				Job job = new Job();
				job.setName(properties.getProperty("job"+i+"_name"));
				job.setN(Integer.parseInt(properties.getProperty("job"+i+"_N")));
				job.setP(Double.parseDouble(properties.getProperty("job"+i+"_P")));
				String[] WH = properties.getProperty("job"+i+"_WH").split("x");
				job.setW(Integer.parseInt(WH[0]));
				job.setH(Integer.parseInt(WH[1]));
				String[] pointsStr = properties.getProperty("job"+i+"_points").split(";");
				
				Point[] points = new Point[job.getN()];
				for (int j = 0; j < points.length; j++) {
					points[j] = new Point(Integer.parseInt(pointsStr[j].split(",")[0]), Integer.parseInt(pointsStr[j].split(",")[1]));
				}
				job.setPoints(points);
				
				jobs.add(job);
			}
			
		} catch (NumberFormatException e) {
			timestampedErrorPrint("Problem reading jobs...");
			System.exit(0);
		}
		
		String portProperty = "servent"+serventId;
		
		try {
			myServentInfo = new ServentInfo(Short.parseShort(properties.getProperty(portProperty)), -1);
		} catch (NumberFormatException e) {
			timestampedErrorPrint("Problem reading " + portProperty + ". Exiting...");
			System.exit(0);
		} catch (UnknownHostException e) {
			timestampedErrorPrint("Problem getting localIpAddress");
		}
	}
	
}
