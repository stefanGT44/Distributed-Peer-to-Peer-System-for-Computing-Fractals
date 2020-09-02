package app;

import cli.CLIParser;
import servent.SimpleServentListener;
import worker.FractalWorker;

/**
 * Describes the procedure for starting a single Servent
 *
 * @author stefanGT44
 */
public class ServentMain {

	/**
	 * Command line arguments are:
	 * 0 - path to servent list file
	 * 1 - this servent's id
	 */
	public static void main(String[] args) {
		if (args.length != 2) {
			AppConfig.timestampedErrorPrint("Please provide servent list file and id of this servent.");
		}
		
		int serventId = -1;
		int portNumber = -1;
		
		String serventListFile = args[0];
		
		try {
			serventId = Integer.parseInt(args[1]);
		} catch (NumberFormatException e) {
			AppConfig.timestampedErrorPrint("Second argument should be an int. Exiting...");
			System.exit(0);
		}
		
		AppConfig.readConfig(serventListFile, serventId);
		
		try {
			portNumber = AppConfig.myServentInfo.getListenerPort();
			
			if (portNumber < 1000 || portNumber > 2000) {
				throw new NumberFormatException();
			}
		} catch (NumberFormatException e) {
			AppConfig.timestampedErrorPrint("Port number should be in range 1000-2000. Exiting...");
			System.exit(0);
		}
		
		AppConfig.timestampedStandardPrint("Starting servent " + AppConfig.myServentInfo);
		
		SimpleServentListener simpleListener = new SimpleServentListener();
		Thread listenerThread = new Thread(simpleListener);
		listenerThread.start();
		
		FractalWorker worker = new FractalWorker();
		Thread workerThread = new Thread(worker);
		workerThread.start();
		
		CLIParser cliParser = new CLIParser(simpleListener, worker);
		Thread cliThread = new Thread(cliParser);
		cliThread.start();
		
		simpleListener.setParser(cliParser);
		simpleListener.setFractalWorker(worker);
		
		ServentInitializer serventInitializer = new ServentInitializer();
		Thread initializerThread = new Thread(serventInitializer);
		initializerThread.start();
		
	}
}
