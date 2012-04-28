package de.blizzy.documentr;

import java.io.File;

import javax.annotation.PostConstruct;
import javax.servlet.ServletContext;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

@Component
public class Settings {
	@Autowired
	private ServletContext servletContext;
	private File dataDir;
	
	@PostConstruct
	public void init() {
		String dataDirParam = servletContext.getInitParameter("documentr.dataDir"); //$NON-NLS-1$
		Assert.hasLength(dataDirParam);
		dataDir = new File(dataDirParam);
	}
	
	public File getDocumentrDataDir() {
		return dataDir;
	}
	
	public void setDocumentrDataDir(File dataDir) {
		this.dataDir = dataDir;
	}
}
