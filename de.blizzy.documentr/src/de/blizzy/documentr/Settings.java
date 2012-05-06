/*
documentr - Edit, maintain, and present software documentation on the web.
Copyright (C) 2012 Maik Schreiber

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program.  If not, see <http://www.gnu.org/licenses/>.
*/
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
