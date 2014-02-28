<%@page import="de.imc.mirror.msfam.AccountManagerPlugin"%>
<%@page import="java.net.URLEncoder"%>
<%@page import="org.jivesoftware.util.JiveGlobals"%>
<%@page import="org.jivesoftware.util.ParamUtils"%>
<%@page import="org.jivesoftware.util.StringUtils"%>
<%@page import="org.jivesoftware.openfire.XMPPServer"%>
<%@page import="org.xmpp.packet.JID"%>
<%@ taglib uri="http://java.sun.com/jstl/fmt_rt" prefix="fmt" %>
<jsp:useBean id="webManager" class="org.jivesoftware.util.WebManager" />

<%
	// initialize openfire objects
	webManager.init(request, response, session, application, out);
	
	// parse parameters
	boolean save = ParamUtils.getBooleanParameter(request, "save");
	AccountManagerPlugin amPlugin = (AccountManagerPlugin) XMPPServer.getInstance().getPluginManager().getPlugin("msfam");
	boolean isServiceEnabled = amPlugin.isServiceEnabled();
	String senderName = amPlugin.getSenderName() != null ? amPlugin.getSenderName() : "";
	String senderEmail = amPlugin.getSenderEmail() != null ? amPlugin.getSenderEmail() : "";
	
	if (save) {
		amPlugin.setServiceEnabled(ParamUtils.getBooleanParameter(request, "enableService", false));
		amPlugin.setSenderName(ParamUtils.getParameter(request, "senderName"));
		amPlugin.setSenderEmail(ParamUtils.getParameter(request, "senderEmail"));
		response.sendRedirect("msfam-settings.jsp?saved=true");
	}
	
%>
<html>
<head>
	<title>Account Manager Settings</title>
	<meta name="pageID" content="msfam-settings"/>
</head>
<body>

<% if (ParamUtils.getBooleanParameter(request, "saved")) { %>
   
<div class="jive-success">
	<table cellpadding="0" cellspacing="0" border="0">
		<tbody>
			<tr>
				<td class="jive-icon"><img src="images/success-16x16.gif" width="16" height="16" border="0"></td>
				<td class="jive-icon-label">Saved successfully.</td>
			</tr>
		</tbody>
	</table>
</div>
   
<% } %>

<form action="msfam-settings.jsp" method="post">
<input type="hidden" name="save" value="true">
<div class="jive-contentBoxHeader">General Settings</div>
<div class="jive-contentBox">
	<table cellpadding="3" cellspacing="0" border="0" width="100%">
		<tbody>
			<tr>
				<td width="1%" align="center" nowrap><input type="checkbox" name="enableService" <%= isServiceEnabled ? "checked=\"checked\"" : "" %>></td>
				<td width="99%" align="left">Enable MSF Account Manager.</td>
			</tr>
		</tbody>
	</table>
</div>
<div class="jive-contentBoxHeader">E-Mail Settings</div>
<div class="jive-contentBox">
	<table cellpadding="3" cellspacing="0" border="0">
		<tbody>
			<tr>
				<td style="text-align: right;">Name of the sender:</td>
				<td><input style="width: 400px;" type="text" name="senderName" value="<%= StringUtils.escapeHTMLTags(senderName) %>"></td>
			</tr>
			<tr>
				<td style="text-align: right;">E-mail address of the sender:</td>
				<td><input style="width: 400px;" type="text" name="senderEmail" value="<%= StringUtils.escapeHTMLTags(senderEmail) %>"></td>
			</tr>
		</tbody>
	</table>
</div>
<input type="submit" value="Save"/>
</form>

</body>
</html>