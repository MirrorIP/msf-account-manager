package de.imc.mirror.msfam;

import java.io.IOException;

import javax.servlet.http.HttpServletResponse;

public interface LookupResult {
	public void respondGet(HttpServletResponse resp) throws IOException;
	public void respondHead(HttpServletResponse resp);
	public long getLastModified();
}