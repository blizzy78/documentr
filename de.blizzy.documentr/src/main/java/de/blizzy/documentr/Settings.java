/*
documentr - Edit, maintain, and present software documentation on the web.
Copyright (C) 2012-2013 Maik Schreiber

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

import lombok.Getter;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

/** Holds information about settings for the application. */
@Component
public class Settings {
	@Autowired
	private ServletContext servletContext;
	@Getter
	private File documentrDataDir;

	@PostConstruct
	public void init() {
		String dataDirParam = getInitParam("documentr.dataDir"); //$NON-NLS-1$
		Assert.hasLength(dataDirParam);
		documentrDataDir = new File(dataDirParam);
	}

	private String getInitParam(String param) {
		String value = servletContext.getInitParameter(param);
		if (StringUtils.isBlank(value)) {
			value = System.getProperty(param);
		}
		return value;
	}
}
