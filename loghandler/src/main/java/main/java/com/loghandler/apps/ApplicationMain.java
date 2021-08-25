package main.java.com.loghandler.apps;

import static main.java.com.loghandler.utils.LogHandlerConstants.INPUT_FILE_NAME;

import java.util.Scanner;
import java.util.logging.Logger;

import main.java.com.loghandler.component.EventHandler;
import main.java.com.loghandler.utils.LogHandlerException;


public class ApplicationMain {
	private static final Logger LOGGER = Logger.getLogger("ApplicationMain");
	
	public static void main(String[] args) {
		LOGGER.info("Starting the log Handler,");
		LOGGER.info("Please enter the path to logfile.txt");
		try(Scanner scanner = new Scanner(System.in)) {
			String inputFilePath = scanner.nextLine();
			(new EventHandler()).prepareEventListMap(inputFilePath+"/"+INPUT_FILE_NAME);
		} catch (LogHandlerException e) {
			LOGGER.severe("Exception occured while executing the log handler " + e.getMessage());
		}
		LOGGER.info("Log Handler Application stopping now");
	}

} // class end

