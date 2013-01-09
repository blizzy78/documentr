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
package de.blizzy.documentr.web.util;

import javax.servlet.http.HttpServletResponse;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/error")
public class ErrorController {
	@RequestMapping("/{statusCode:[0-9]+}/{messageKey:[a-zA-Z0-9\\.]+}")
	@PreAuthorize("permitAll")
	public String sendError(@PathVariable int statusCode, @PathVariable String messageKey, Model model) {
		model.addAttribute("statusCode", statusCode); //$NON-NLS-1$
		model.addAttribute("messageKey", messageKey); //$NON-NLS-1$
		return "/sendError"; //$NON-NLS-1$
	}

	public static String timeout() {
		return "forward:/error/" + HttpServletResponse.SC_SERVICE_UNAVAILABLE + "/timeout"; //$NON-NLS-1$ //$NON-NLS-2$
	}

	public static String notFound(String messageKey) {
		Assert.hasLength(messageKey);

		return "forward:/error/" + HttpServletResponse.SC_NOT_FOUND + "/" + messageKey; //$NON-NLS-1$ //$NON-NLS-2$
	}

	public static String notModified() {
		return "forward:/error/" + HttpServletResponse.SC_NOT_MODIFIED + "/dummy"; //$NON-NLS-1$ //$NON-NLS-2$
	}

	public static String forbidden() {
		return "forward:/error/" + HttpServletResponse.SC_FORBIDDEN + "/dummy"; //$NON-NLS-1$ //$NON-NLS-2$
	}
}
