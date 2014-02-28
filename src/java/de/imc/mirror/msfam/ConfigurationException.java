package de.imc.mirror.msfam;

/**
 * Exception thrown when the server configuration does not allow sending emails. 
 * @author simon.schwantzer(at)im-c.de
 */
public class ConfigurationException extends Exception {
	private static final long serialVersionUID = 1L;

	public ConfigurationException(String message) {
		super(message);
	}
}
