package de.imc.mirror.msfam;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.zip.GZIPOutputStream;

import javax.servlet.http.HttpServletResponse;

public class StaticFile implements LookupResult {
	protected final long lastModified;
	protected final String mimeType;
	protected final int contentLength;
	protected final boolean acceptsDeflate;
	protected final File file;
	
	private static final int deflateThreshold = 4*1024;
	private static final int bufferSize = 4*1024;


	public StaticFile(long lastModified, String mimeType, int contentLength, boolean acceptsDeflate, File file) {
		this.lastModified = lastModified;
		this.mimeType = mimeType;
		this.contentLength = contentLength;
		this.acceptsDeflate = acceptsDeflate;
		this.file = file;
	}

	public long getLastModified() {
		return lastModified;
	}

	protected boolean willDeflate() {
		return acceptsDeflate && deflatable(mimeType) && contentLength >= deflateThreshold;
	}

	protected void setHeaders(HttpServletResponse resp) {
		resp.setStatus(HttpServletResponse.SC_OK);
		resp.setContentType(mimeType);
		if(contentLength >= 0 && !willDeflate())
			resp.setContentLength(contentLength);
	}

	public void respondGet(HttpServletResponse resp) throws IOException {
		setHeaders(resp);
		final OutputStream os;
		if(willDeflate()) {
			resp.setHeader("Content-Encoding", "gzip");
			os = new GZIPOutputStream(resp.getOutputStream(), bufferSize);
		} else
			os = resp.getOutputStream();
		transferStreams(new FileInputStream(file), os);
	}
	
	public void respondHead(HttpServletResponse resp) {
		if(willDeflate())
			throw new UnsupportedOperationException();
		setHeaders(resp);		
	}
	
	private boolean deflatable(String mimetype) {
		return mimetype.startsWith("text/")
			|| mimetype.equals("application/postscript")
			|| mimetype.startsWith("application/ms")
			|| mimetype.startsWith("application/vnd")
			|| mimetype.endsWith("xml");
	}
	
	private void transferStreams(InputStream is, OutputStream os) throws IOException {
		try {
			byte[] buf = new byte[bufferSize];
			int bytesRead;
			while ((bytesRead = is.read(buf)) != -1)
				os.write(buf, 0, bytesRead);
		} finally {
			is.close();
			os.close();
		}
	}
}