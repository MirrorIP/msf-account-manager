package de.imc.mirror.msfam;

import java.io.IOException;

import javax.servlet.http.HttpServletResponse;

public class LookupError implements LookupResult {
	protected final int statusCode;
	protected final String message;

	public LookupError(int statusCode, String message) {
		this.statusCode = statusCode;
		this.message = message;
	}

	public long getLastModified() {
		return -1;
	}

	public void respondGet(HttpServletResponse resp) throws IOException {
		resp.sendError(statusCode,message);
	}

	public void respondHead(HttpServletResponse resp) {
		throw new UnsupportedOperationException();
	}
}