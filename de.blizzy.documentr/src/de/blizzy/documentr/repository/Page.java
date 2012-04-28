package de.blizzy.documentr.repository;

import java.io.UnsupportedEncodingException;

public class Page {
	private String title;
	private byte[] data;
	private String contentType;

	public Page(String title, byte[] data, String contentType) {
		this.title = title;
		this.data = data;
		this.contentType = contentType;
	}

	public static Page fromText(String title, String text) {
		return fromText(title, text, "text/plain"); //$NON-NLS-1$
	}
	
	public static Page fromText(String title, String text, String contentType) {
		try {
			return new Page(title, text.getBytes("UTF-8"), contentType); //$NON-NLS-1$
		} catch (UnsupportedEncodingException e) {
			throw new RuntimeException(e);
		}
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
