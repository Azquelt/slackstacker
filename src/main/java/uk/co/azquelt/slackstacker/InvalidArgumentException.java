package uk.co.azquelt.slackstacker;

/**
 * Thrown when a command line argument is invalid
 */
public class InvalidArgumentException extends Exception {
	
	private static final long serialVersionUID = 1L;

	public InvalidArgumentException(String message) {
		super(message);
	}

}
