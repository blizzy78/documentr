package de.blizzy.documentr.pagestore;

import java.io.UnsupportedEncodingException;

public class Page {
	private String title;
	private byte[] data;
	private String contentType;

	private Page(String title, byte[] data, String contentType) {
		this.title = title;
		this.data = data;
		this.contentType = contentType;
	}

	public static Page fromText(String title, String text) {
		try {
			return new Page(title, text.getBytes("UTF-8"), "text/plain"); //$NON-NLS-1$ //$NON-NLS-2$
		} catch (UnsupportedEncodingException e) {
			throw new RuntimeException(e);
		}
	}
	
	public static Page fromData(byte[] data, String contentType) {
		return new Page(null, data, contentType);
	}

	public String getTitle() {
		return title;
	}
	
	public byte[] getData() {
		return data;
	}
	
	public String getText() {
		try {
			return new String(data, "UTF-8"); //$NON-NLS-1$
		} catch (UnsupportedEncodingException e) {
			throw new RuntimeException(e);
		}
	}

	public String getContentType() {
		return contentType;
	}
}
