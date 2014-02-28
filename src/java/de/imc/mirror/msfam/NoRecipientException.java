package de.imc.mirror.msfam;

/**
 * Exception thrown when a user has no email address to send the mail to. 
 * @author simon.schwantzer(at)im-c.de
 */
public class NoRecipientException extends Exception {
	private static final long serialVersionUID = 1L;

	public NoRecipientException(String message) {
		super(message);
	}
}
