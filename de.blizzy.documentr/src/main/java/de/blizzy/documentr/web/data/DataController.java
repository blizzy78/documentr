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
package de.blizzy.documentr.web.data;

import java.io.IOException;
import java.util.Collections;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.context.request.WebRequest;

import de.blizzy.documentr.DocumentrConstants;
import de.blizzy.documentr.access.DocumentrAnonymousAuthenticationFactory;
import de.blizzy.documentr.access.UserStore;
import de.blizzy.documentr.markdown.macro.MacroFactory;

@Controller
@RequestMapping("/data")
public class DataController {
	@Autowired
	private MacroFactory macroFactory;
	@Autowired
	private DocumentrAnonymousAuthenticationFactory authenticationFactory;

	@RequestMapping(value="/macro/{macroName:" + DocumentrConstants.MACRO_NAME_PATTERN + "}/{request:.*}",
			method={ RequestMethod.GET, RequestMethod.POST })
	@PreAuthorize("permitAll")
	@ResponseBody
	public Object getData(@PathVariable String macroName, @PathVariable String request, Authentication authentication,
			WebRequest webRequest) throws IOException {

		if (authentication == null) {
			authentication = authenticationFactory.create(UserStore.ANONYMOUS_USER_LOGIN_NAME);
		}
		Object result = macroFactory.getData(macroName, request, webRequest.getParameterMap(), authentication);
		if (result == null) {
			result = Collections.emptyMap();
		}
		return result;
	}
}
