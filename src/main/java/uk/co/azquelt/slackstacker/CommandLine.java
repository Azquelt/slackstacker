package uk.co.azquelt.slackstacker;

import java.io.File;
import java.util.Arrays;
import java.util.Iterator;

/**
 * Processes and validates the command line arguments
 */
public class CommandLine {
	
	private File configFile;
	
	private CommandLine() {}
	
	public static CommandLine processArgs(String[] args) throws InvalidArgumentException {
		CommandLine commandLine = new CommandLine();
		Iterator<String> i = Arrays.asList(args).iterator();
		
		while (i.hasNext()) {
			String arg = i.next();
			switch (arg) {
			case "-f":
				String file = i.next();
				validateFile("-f", file);
				commandLine.setConfigFile(file);
				break;
			default:
				throw new InvalidArgumentException("Unrecognised argument: " + arg);
			}
		}
		
		return commandLine;
	}
	
	public void setConfigFile(String file) {
		configFile = new File(file);
	}
	
	public File getConfigFile() {
		return configFile;
	}
	
	private static void validateFile(String arg, String file) throws InvalidArgumentException {
		if (file == null) {
			throw new InvalidArgumentException("Argument " + arg + " must be followed by a valid filename");
		}
		if (!new File(file).exists()) {
			throw new InvalidArgumentException("File " + file + " does not exist");
		}
	}

}
