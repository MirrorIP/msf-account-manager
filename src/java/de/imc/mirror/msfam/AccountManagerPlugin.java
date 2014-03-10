/**
 * $Revision: 1.0 $
 *
 * This software is published under the terms of the GNU Public License (GPL),
 * a copy of which is included in this distribution.
 * 
 * Inspired by the user service plugin from Justin Hunt.
 */

package de.imc.mirror.msfam;

import java.io.File;
import java.util.Random;

import org.dom4j.Element;
import org.jivesoftware.openfire.XMPPServer;
import org.jivesoftware.openfire.auth.AuthToken;
import org.jivesoftware.openfire.container.Plugin;
import org.jivesoftware.openfire.container.PluginManager;
import org.jivesoftware.openfire.user.User;
import org.jivesoftware.openfire.user.UserAlreadyExistsException;
import org.jivesoftware.openfire.user.UserManager;
import org.jivesoftware.openfire.user.UserNotFoundException;
import org.jivesoftware.openfire.vcard.VCardManager;
import org.jivesoftware.openfire.vcard.VCardProvider;
import org.jivesoftware.util.EmailService;
import org.jivesoftware.util.JiveGlobals;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AccountManagerPlugin implements Plugin {
	private static final Logger log = LoggerFactory.getLogger(AccountManagerPlugin.class);
	private static final char[] pwdChars = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789".toCharArray();
	private static final int pwdLength = 12;
	
    private boolean isServiceEnabled;
    private UserManager userManager;
    private VCardProvider vCardProvider;
    private String senderName;
    private String senderEmail;
    private String passwordResetMessageBody;

    @Override
    public void initializePlugin(PluginManager manager, File pluginDirectory) {
        isServiceEnabled = JiveGlobals.getBooleanProperty("plugin.msfam.enabled", false);
        senderName = JiveGlobals.getProperty("plugin.msfam.senderName");
        senderEmail = JiveGlobals.getProperty("plugin.msfam.senderEmail");
        passwordResetMessageBody = JiveGlobals.getProperty("plugin.msfam.passwordResetMessageBody", "<p>Your new password is: %PASSWORD%</p>");
        log.info("MSF Account Management Tool loaded. The service is currently " + (isServiceEnabled ? "enabled." : "disabled."));
        this.userManager = XMPPServer.getInstance().getUserManager();
        XMPPServer.getInstance().getVCardManager();
		this.vCardProvider = VCardManager.getProvider();
    }

    @Override
    public void destroyPlugin() {
    }
    
    /**
     * Checks if the service is activated.
     * @return <code>true</code> if the service is activated in the configuration, otherwise <code>false</code>. 
     */
    public boolean isServiceEnabled() {
        return isServiceEnabled;
    }

    /**
     * Sets the availability of the service and write the configuration.
     * @param enabled Set to <code>true</code> to enable service or to <code>false</code> to diable it.
     */
    public void setServiceEnabled(boolean enabled) {
        this.isServiceEnabled = enabled;
        JiveGlobals.setProperty("plugin.msfam.enabled",  enabled ? "true" : "false");
    }
    
    /**
     * Checks if server and plugin configuration allows sending out e-mails. 
     * @return <code>true</code> if sending e-mails is possible, otherwise <code>false</code>.
     */
    public boolean isEmailConfigured() {
    	if (!JiveGlobals.getBooleanProperty("mail.configured")) {
    		return false;
    	}
    	if (senderEmail == null || senderEmail.trim().isEmpty()) {
    		return false;
    	}
    	return true;
    }
    
    /**
     * Returns the name of the e-mail sender configured for the plugin.
     * @return Name of the sender. May be <code>null</code> or empty.
     */
    public String getSenderName() {
    	return senderName;
    }
    
    /**
     * Sets the name of the sender used when e-mails are sent. 
     * @param name Name of the sender. May be <code>null</code>.
     */
    public void setSenderName(String name) {
    	this.senderName = name;
    	JiveGlobals.setProperty("plugin.msfam.senderName", name);
    }
    
    /**
     * Returns the body of the password reset message.     
     * @return Message body as HTML string.
     */
    public String getPasswordResetMessageBody() {
		return passwordResetMessageBody;
	}

    /**
     * Sets the body for the password reset message. 
     * @param passwordResetMessageBody HTML string to use as message body. 
     */
	public void setPasswordResetMessageBody(String passwordResetMessageBody) {
		this.passwordResetMessageBody = passwordResetMessageBody;
		JiveGlobals.setProperty("plugin.msfam.passwordResetMessageBody",  passwordResetMessageBody);
	}

	/**
     * Returns the sender for e-mails send out.
     * @return E-mail address as string. May be <code>null</code> or empty.
     */
    public String getSenderEmail() {
    	return senderEmail;
    }
    
    /**
     * Sets the sender for e-mails send out.
     * @param email E-mail address as string. May be <code>null</code> or empty.
     */
    public void setSenderEmail(String email) {
    	this.senderEmail = email;
    	JiveGlobals.setProperty("plugin.msfam.senderEmail", email);
    }
    
    /**
     * Returns the user for the given identifier.
     * @param userId Identifier (node ID) if the user.
     * @return User object.
     * @throws UserNotFoundException No user is available for the given identifier.
     */
    public User getUser(String userId) throws UserNotFoundException {
    	return userManager.getUser(userId);
    }
    
    /**
     * Returns the user for the given authentication token.
     * @param token Authentication token.
     * @return User object.
     * @throws UserNotFoundException No user is available for the user id stored in the token.
     */
    public User getUser(AuthToken token) throws UserNotFoundException {
    	return userManager.getUser(token.getUsername());
    }
    
    /**
     * Creates a new XMPP user.
     * @param userId Identifier for the user.
     * @param userPwd Password for the account.
     * @param userName Full name of the user. Optional.
     * @param userEmail E-mail address of the user. Optional.
     * @return User object.
     * @throws UserAlreadyExistsException The given user id is already in use.
     */
    public User createUser(String userId, String userPwd, String userName, String userEmail) throws UserAlreadyExistsException {
    	return userManager.createUser(userId, userPwd, userName, userEmail);
    }
    
    /**
     * Returns the vCard information for the current user.
     * @param token Authentication token for the user.
     * @return XML element as defined in XEP-0054.
     */
    public Element getVCard(AuthToken token) {
    	return vCardProvider.loadVCard(token.getUsername());
    }
    
    /**
     * Sets the vCard information for the current user.
     * @param token Authentication token for the user.
     * @param vCardElement XML element as defined in XEP-0054.
     */
    public void setVCard(AuthToken token, Element vCardElement) {
		try {
	    	if (vCardProvider.loadVCard(token.getUsername()) != null) {
				vCardProvider.updateVCard(token.getUsername(), vCardElement);
	    	} else {
				vCardProvider.createVCard(token.getUsername(), vCardElement);
	    	}
		} catch (Exception e) {
			log.warn("Failed to write vCard: " + e.getMessage());
		}
    }
    
    /**
     * Replaces the password of the given user with a random one and sends it to the users e-mail address. 
     * @param user User to reset password of.
     * @throws ConfigurationException The server is not configured properly to send e-mails.
     * @throws NoRecipientException The user profile does not contain a valid e-mail address.
     */
    public void resetAndSendPassword(User user) throws ConfigurationException, NoRecipientException {
    	if (!isEmailConfigured()) {
    		throw new ConfigurationException("The server is not configured properly to send e-mails.");
    	}
    	String toEmail = user.getEmail();
    	if (toEmail == null || toEmail.trim().isEmpty()) {
    		throw new NoRecipientException("The given user has no email address.");
    	}
    	
    	String toName;
    	if (user.getName() != null && !user.getName().trim().isEmpty()) {
    		toName = user.getName();
    	} else {
    		toName = user.getEmail();
    	}
    	
    	String fromName = senderName != null && !senderName.trim().isEmpty() ? senderName : senderEmail;
    	
    	String subject = "MIRROR Spaces Framework: Password Reset";
    	String newPassword = generatePassword(pwdChars, pwdLength);
    	String htmlBody = generateMessageBody(newPassword);
    	
    	EmailService.getInstance().sendMessage(toName, toEmail, fromName, senderEmail, subject, null, htmlBody);
    	user.setPassword(newPassword);
    	log.info("Handled a password reset request for: " + user.getUID());
    }
    
    /**
     * Generates the final message body. 
     * @param newPassword
     * @return HTML body for the password reset e-mail.
     */
    private String generateMessageBody(String newPassword) {
    	return passwordResetMessageBody.replaceAll("%PASSWORD%", newPassword);
    }
    
    /**
     * Generates a random password.
     * @param chars Characters to use.
     * @param size Size of the generated password.
     * @return Password string.
     */
	private String generatePassword(char[] chars, int size) {
		StringBuilder sb = new StringBuilder();
		Random random = new Random();
		for (int i = 0; i < size; i++) {
		    char c = chars[random.nextInt(chars.length)];
		    sb.append(c);
		}
		String output = sb.toString();
		return output;
	}
}