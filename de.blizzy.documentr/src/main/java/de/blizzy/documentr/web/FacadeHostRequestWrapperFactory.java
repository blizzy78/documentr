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
package de.blizzy.documentr.web;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import de.blizzy.documentr.Settings;

@Component
public class FacadeHostRequestWrapperFactory {
	@Autowired
	private Settings settings;
	
	public HttpServletRequest create(HttpServletRequest request) {
		return new FacadeHostRequestWrapper(request, settings.getHost(), settings.getPort());
	}
	
	void setSettings(Settings settings) {
		this.settings = settings;
	}
}
