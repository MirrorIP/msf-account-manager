package de.imc.mirror.msfam;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;

import javax.activation.MimetypesFileTypeMap;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.Namespace;
import org.dom4j.QName;
import org.jivesoftware.admin.AuthCheckFilter;
import org.jivesoftware.openfire.XMPPServer;
import org.jivesoftware.openfire.auth.AuthFactory;
import org.jivesoftware.openfire.auth.AuthToken;
import org.jivesoftware.openfire.auth.ConnectionException;
import org.jivesoftware.openfire.auth.InternalUnauthenticatedException;
import org.jivesoftware.openfire.auth.UnauthorizedException;
import org.jivesoftware.openfire.container.PluginManager;
import org.jivesoftware.openfire.user.User;
import org.jivesoftware.openfire.user.UserAlreadyExistsException;
import org.jivesoftware.openfire.user.UserNotFoundException;
import org.jivesoftware.util.XMLWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Servlet for the frontend of the MSF Account Management Tool. 
 * @author simon.schwantzer(at)im-c.de
 */
public class AccountManagerServlet extends HttpServlet {
	private static final Logger log = LoggerFactory.getLogger(AccountManagerServlet.class);
	private static final long serialVersionUID = 1L;
	
	/**
	 * Enumeration for user actions.
	 */
	public enum Action {
		LOGIN,
		LOGOUT,
		REGISTER,
		RESET,
		UPDATE_ACCOUNT,
		UPDATE_PROFILE,
		CHANGE_PASSWORD,
		RESET_PASSWORD,
		REGISTER_ACCOUNT,
		NONE;
		
		public static Action getAction(String actionRequest) {
			if ("login".equalsIgnoreCase(actionRequest)) {
				return LOGIN;
			} else if ("logout".equalsIgnoreCase(actionRequest)) {
				return LOGOUT;
			} if ("register".equalsIgnoreCase(actionRequest)) {
				return REGISTER;
			} else if ("reset".equalsIgnoreCase(actionRequest)) {
				return RESET;
			} else if ("update-account".equalsIgnoreCase(actionRequest)) {
				return UPDATE_ACCOUNT;
			} else if ("update-profile".equalsIgnoreCase(actionRequest)) {
				return UPDATE_PROFILE;
			} else if ("change-password".equalsIgnoreCase(actionRequest)) {
				return CHANGE_PASSWORD;
			} else if ("reset-password".equalsIgnoreCase(actionRequest)) {
				return RESET_PASSWORD;
			} else if ("register-account".equalsIgnoreCase(actionRequest)) {
				return REGISTER_ACCOUNT;
			} else {
				return NONE;
			}
		}
	}
	
	private static final String errorHTML = "<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 4.01 Transitional//EN\"><html><head><title>MSF Account Management Tool</title><meta http-equiv=\"content-type\" content=\"text/html; charset=UTF-8\"><link rel=\"stylesheet\" href=\"manage/static/css/style.css\" type=\"text/css\" media=\"screen\"/></head><body><div id=\"body\"><div id=\"header\"><h1>MSF Account Management Tool</h1></div><div id=\"errorMessagePanel\" class=\"panel errorMessage\"><table><colgroup><col width=\"100%\"></colgroup><tr><td id=\"errorMessageLabel\">%MESSAGE%</td></tr></table></div></div></body>";
	private AccountManagerPlugin amPlugin;
    private PluginManager pluginManager;
    private File pluginPath, staticFilePath, templateFilePath;

    @Override
    public void init(ServletConfig servletConfig) throws ServletException {
        super.init(servletConfig);
        
        pluginManager = XMPPServer.getInstance().getPluginManager();
		amPlugin = (AccountManagerPlugin) pluginManager.getPlugin("msfam");
		pluginPath = pluginManager.getPluginDirectory(amPlugin);
		
		templateFilePath = new File(new File(pluginPath, "web"), "templates");
		staticFilePath = new File(new File(pluginPath, "web"), "static");
        
		AuthCheckFilter.addExclude("msfam/manage*");
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
    	if (request.getPathInfo().startsWith("/msfam/manage/static/")) {
    		handleStaticRequest(request, response);
    		return;
    	}

        if (!amPlugin.isServiceEnabled()) {
        	showError("The Account Management Tool is currently not available!", null, response);
        	return;
        }
        
    	Action action = Action.getAction(request.getParameter("action"));
    	switch (action) {
    	case LOGIN:
            handleLoginAction(request, response);
    		break;
    	case LOGOUT:
    		handleLogoutAction(request, response);
    		break;
    	case UPDATE_ACCOUNT:
    		handleUpdateAccountAction(request, response);
    		break;
    	case CHANGE_PASSWORD:
    		handlePasswordChangeAction(request, response);
    		break;
    	case RESET_PASSWORD:
    		handlePasswordResetAction(request, response);
    		break;
    	case REGISTER_ACCOUNT:
    		handleAccountRegistrationAction(request, response);
    		break;
    	default:
    		// No action to perform.
            AuthToken authToken = (AuthToken) request.getSession().getAttribute("authToken");
    		if (authToken != null) {
    			showProfilePage(authToken, request, response);
    		} else {
    			readHTMLFile("welcome.html", null, response);
    		}
    	}
    }
    
    /**
     * Tries to authenticate a login request. If successful, a authentication token is added to the session.
     */
    private void handleLoginAction(HttpServletRequest request, HttpServletResponse response) throws IOException {
    	String userId = request.getParameter("userIdInput");
    	String userPwd = request.getParameter("userPwdInput");
    	    	
    	AuthToken token;
    	try {
			token = AuthFactory.authenticate(userId, userPwd);
			request.getSession().setAttribute("authToken", token);
			showProfilePage(token, request, response);
		} catch (UnauthorizedException e) {
			showError("Failed to log in.", "manage", response);
		} catch (ConnectionException e) {
			log.warn("Connection exception during authentication: " + e.getMessage());
			showError("Internal error.", "manage", response);
		} catch (InternalUnauthenticatedException e) {
			log.warn("An internal authentication exception occured: " + e.getMessage());
			showError("Internal error.", "manage", response);
		}
    }
    
    /**
     * Deletes the token from the session and loads the welcome page.
     */
    private void handleLogoutAction(HttpServletRequest request, HttpServletResponse response) throws IOException {
    	request.getSession().removeAttribute("authToken");
    	readHTMLFile("welcome.html", null, response);
    }
    
    /**
     * Handles a request to change the user password. Returns profile page if successful, otherwise an error page.
     */
    private void handlePasswordChangeAction(HttpServletRequest request, HttpServletResponse response) throws IOException {
    	AuthToken token = (AuthToken) request.getSession().getAttribute("authToken");
    	if (token == null) {
    		showError("Session is invalid.", "manage", response);
    		return;
    	}
    	String oldPwd = request.getParameter("oldPwd");
    	String newPwd = request.getParameter("newPwd");
    	User user;
		try {
			user = amPlugin.getUser(token);
	    	if (oldPwd.equals(AuthFactory.getPassword(user.getUsername()))) {
	    		user.setPassword(newPwd);
	    		showProfilePage(token, request, response);
	    	} else {
	    		showError("The password could not be verified.", "manage", response);
	    		return;
	    	}
		} catch (UserNotFoundException e) {
			showError("Account error.", "manage", response);
			return;
		}
    }
    
    /**
     * Handles an account update. Returns profile page if successful, otherwise an error page. 
     */
    private void handleUpdateAccountAction(HttpServletRequest request, HttpServletResponse response) throws IOException {
    	AuthToken token = (AuthToken) request.getSession().getAttribute("authToken");
    	if (token == null) {
    		showError("Session is invalid.", "manage", response);
    		return;
    	}
		String userEmail = request.getParameter("userEmail");
		String userName = request.getParameter("userName");
    	boolean isUserNamePublic = request.getParameter("isUserNamePublic") != null;
    	
    	try {
			User user = amPlugin.getUser(token);
			Element vCardElement = amPlugin.getVCard(token);
			if (vCardElement == null) {
				vCardElement = DocumentHelper.createElement(new QName("vCard", Namespace.get("vcard-temp")));
			} else {
				fixRecursiveVCardElements(vCardElement); // hotfix
			}
			Element fnElement = vCardElement.element("FN");
			if (userEmail != null && !userEmail.trim().isEmpty()) {
				user.setEmail(userEmail);
			} else {
				user.setEmail(null);
			}
			if (userName != null && !userName.trim().isEmpty()) {
				user.setName(userName);
				if (isUserNamePublic) {
					if (fnElement == null) {
						fnElement = vCardElement.addElement("FN");
					}
					fnElement.setText(userName);
				} else {
					if (fnElement != null) {
						vCardElement.remove(fnElement);
					}
				}
			} else {
				user.setName(null);
				if (fnElement != null) {
					vCardElement.remove(fnElement);
				}
			}
			amPlugin.setVCard(token, vCardElement);
			
	    	showProfilePage(token, request, response);
		} catch (UserNotFoundException e) {
			log.warn("Authorized user was not found: " + e.getMessage());
			showError("Session is invalid.", "manage", response);
			e.printStackTrace();
		}
    }
    
    /**
     * Handler for account registration. It is implemented as AJAX call and returns an XML indicating success or errors.   
     */
    private void handleAccountRegistrationAction(HttpServletRequest request, HttpServletResponse response) throws IOException {
    	String userId = request.getParameter("userId");
    	String userPwd = request.getParameter("userPwd");
    	String userName = request.getParameter("userName");
    	String userEmail = request.getParameter("userEmail");
    	boolean isUserNamePublic = request.getParameter("isUserNamePublic") != null;
    	Element resultElement = DocumentHelper.createElement("result");
    	
    	AuthToken token = null;
    	try {
			amPlugin.createUser(userId, userPwd, userName, userEmail);
			token = AuthFactory.authenticate(userId, userPwd);
			if (isUserNamePublic && userName != null && !userName.trim().isEmpty()) {
				Element vCardElement = DocumentHelper.createElement(new QName("vCard", Namespace.get("vcard-temp")));
				Element fnElement = vCardElement.addElement("FN");
				fnElement.setText(userName);
				amPlugin.setVCard(token, vCardElement);
			}
			resultElement.addAttribute("type", "result");
			resultElement.addAttribute("code", "200");
		} catch (UserAlreadyExistsException e) {
			resultElement.addAttribute("type", "error");
			resultElement.addAttribute("code", "400");
			resultElement.setText("The selected user id is already in use. Please choose another one.");
		} catch (UnauthorizedException e) {
			resultElement.addAttribute("type", "error");
			resultElement.addAttribute("code", "500");
			resultElement.setText("The registration failed by an internal error.");
		} catch (ConnectionException e) {
			resultElement.addAttribute("type", "error");
			resultElement.addAttribute("code", "500");
			resultElement.setText("The registration failed by an internal error.");
		} catch (InternalUnauthenticatedException e) {
			resultElement.addAttribute("type", "error");
			resultElement.addAttribute("code", "500");
			resultElement.setText("The registration failed by an internal error.");
		} catch (IllegalArgumentException e) {
			resultElement.addAttribute("type", "error");
			resultElement.addAttribute("code", "400");
			resultElement.setText("The given data is not valid.");
		}
    	
		if (token != null) {
			request.getSession().setAttribute("authToken", token);
		}
		XMLWriter writer = new XMLWriter(response.getWriter());
		writer.write(resultElement);
		writer.flush();
		writer.close();
    	
    }

    /**
     * Handles a passwort reset action. It is implemented as AJAX call and returns the HTML to display.
     */
    private void handlePasswordResetAction(HttpServletRequest request, HttpServletResponse response) throws IOException {
    	String userId = request.getParameter("userId");
    	String responseHtml;
    	if (userId == null || userId.trim().isEmpty()) {
    		responseHtml = "<div class=\"warnLabel\">A user id is required in order to reset the password.</div>";
    	} else try {
			User user = amPlugin.getUser(userId);
			amPlugin.resetAndSendPassword(user);
			responseHtml = "<div class=\"infoLabel\">An e-mail containing a new password has been sent to the given e-mail address.</div>";
		} catch (UserNotFoundException e) {
			responseHtml = "<div class=\"warnLabel\">Invalid user ID.</div>";
		} catch (ConfigurationException e) {
			responseHtml = "<div class=\"warnLabel\">A passwort reset is currently not possible.</div>";
		} catch (NoRecipientException e) {
			responseHtml = "<div class=\"warnLabel\">No e-mail address available for the given account. The request cannot be processed.</div>";
		}
    	
    	PrintWriter writer = response.getWriter();
    	writer.println(responseHtml);
    	writer.flush();
    	writer.close();
    }
    
    /**
     * Shows the profile page of the authenticated user.
     * @param token Authentication token provided with the request.
     */
    private void showProfilePage(AuthToken token, HttpServletRequest request, HttpServletResponse response) throws IOException {
    	Map<String, String> fieldValues = new HashMap<String, String>();
    	try {
			User user = amPlugin.getUser(token);
			fieldValues.put("ACCOUNT_USER_ID", user.getUID());
			fieldValues.put("ACCOUNT_USER_NAME", user.getName() != null ? user.getName() : "");
			fieldValues.put("ACCOUNT_USER_EMAIL", user.getEmail() != null ? user.getEmail() : "");
			Element vCardElement =  amPlugin.getVCard(token);
			fieldValues.put("ACCOUNT_IS_USER_NAME_VISIBLE", "");
			if (vCardElement != null) {
				Element fullNameElement = vCardElement.element("FN");
				if (fullNameElement != null) {
					fieldValues.put("ACCOUNT_IS_USER_NAME_VISIBLE", "checked=\"checked\"");				
				}
			}
			
			readHTMLFile("profile.html", fieldValues, response);
		} catch (UserNotFoundException e) {
			request.getSession().removeAttribute("authToken");
			showError("Session is invalid.", "manage", response);
		}
    }
    
    /**
     * Displays an error page.
     * @param error Error message to show.
     * @param returnUrl URL to call one the error message is dismissed.
     */
    private void showError(String error, String returnUrl, HttpServletResponse response) throws IOException {
    	Map<String, String> fieldValues = new HashMap<String, String>();
    	fieldValues.put("MESSAGE", error);
    	fieldValues.put("URL", returnUrl != null ? returnUrl : "");
    	readHTMLFile("error.html", fieldValues, response);
    }
    
    /**
     * Delivers static files from /src/web/static.
     */
    private void handleStaticRequest(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
    	String filePath = request.getPathInfo().substring(21);
    	lookup(new File(staticFilePath, filePath), acceptsDeflate(request)).respondGet(response);
    }
    
    /**
     * Removes invalid vCard sub elements created by MIRROR Space Manager.
     * @param vCardElement
     */
    private void fixRecursiveVCardElements(Element vCardElement) {
    	Element subElement = vCardElement.element("vCard");
    	if (subElement != null) {
    		vCardElement.remove(subElement);
    	}
    }
    
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        doGet(request, response);
    }
    
    @Override
	protected void doHead(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
    	if (request.getPathInfo().startsWith("/msfam/manage/static/")) {
    		String filePath = request.getPathInfo().substring(21);
    		
    		try {
    			lookup(new File(staticFilePath, filePath), acceptsDeflate(request)).respondHead(response);
    		} catch (UnsupportedOperationException e) {
    			super.doHead(request, response);
    		}
    		handleStaticRequest(request, response);
    		return;
    	} else {
    		super.doHead(request, response);
    	}
	}

    @Override
    public void destroy() {
        super.destroy();
        AuthCheckFilter.removeExclude("msfam/manage*");
    }
	
    /**
     * Lookup a static file.
     * @param file File requested.
     * @param acceptsDeflate If <code>true</code> the file is deflated is possible, otherwise it is not.
     * @return Static file object or lookup error. 
     */
	protected LookupResult lookup(File file, boolean acceptsDeflate) {
		if (!file.exists()) {
			return new LookupError(HttpServletResponse.SC_NOT_FOUND, "Not found");
		} else if (!file.isFile()) {
			return new LookupError(HttpServletResponse.SC_FORBIDDEN, "Forbidden");
		}
		String mimeType = new MimetypesFileTypeMap().getContentType(file);
		if (mimeType == null) {
			mimeType = "application/octet-stream";
		}
		
		return new StaticFile(file.lastModified(), mimeType, (int) file.length(), acceptsDeflate, file);
	}
	
	/**
	 * Checks if the request accepts a deflating of the response. 
	 * @param req Http servlet request.
	 * @return <code>true</code> if the request accepts gzip encoding, otherwise <code>false</code>.
	 */
	private boolean acceptsDeflate(HttpServletRequest req) {
		final String ae = req.getHeader("Accept-Encoding");
		return ae != null && ae.contains("gzip");
	}
	
	/**
	 * Reads the content from an external (HTML) file, replaces the given fields and sends the result as request response. 
	 * @param fileName File name of the template to read. 
	 * @param fieldValues Map of values to replace. 
	 * @param response Servlet response to write result to.
	 */
	private void readHTMLFile(String fileName, Map<String, String> fieldValues, HttpServletResponse response) throws IOException {
		String s;
		try {
			FileReader fileReader = new FileReader(new File(templateFilePath, fileName));
			BufferedReader bufferedReader = new BufferedReader(fileReader);
			StringBuffer stringBuffer = new StringBuffer();
			while ((s = bufferedReader.readLine()) != null ){
				stringBuffer.append(s);
			}
			s = stringBuffer.toString();
			bufferedReader.close();
			fileReader.close();
			if (fieldValues != null) {
				for (String field : fieldValues.keySet()) {
					s = s.replaceAll("%" + field + "%", fieldValues.get(field));
				}
			}
		} catch (Exception e) {
			log.warn("Failed to read HTML template: " + e.getMessage());
			s = errorHTML.replaceAll("%MESSAGE%", "An internal error occurred.");
		}
		
		PrintWriter writer = response.getWriter();
		writer.println(s);
		writer.flush();
		writer.close();
	}
	
}