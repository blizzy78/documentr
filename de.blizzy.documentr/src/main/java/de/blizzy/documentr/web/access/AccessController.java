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
package de.blizzy.documentr.web.access;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.commons.lang3.StringUtils;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.WebAttributes;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@Controller
@RequestMapping("/access")
public class AccessController {
	@RequestMapping(value="/login", method=RequestMethod.GET)
	@PreAuthorize("permitAll")
	public String login() {
		return "/login"; //$NON-NLS-1$
	}

	@RequestMapping(value="/login/error", method=RequestMethod.GET)
	@PreAuthorize("permitAll")
	public String loginError(HttpSession session, Model model) {
		AuthenticationException exception =
				(AuthenticationException) session.getAttribute(WebAttributes.AUTHENTICATION_EXCEPTION);
		String msg = getMessage(exception);
		if (StringUtils.isNotBlank(msg)) {
			model.addAttribute("errorMessage", msg); //$NON-NLS-1$
		}
		return "/login"; //$NON-NLS-1$
	}

	@RequestMapping(value="/login/forbidden", method=RequestMethod.GET)
	@PreAuthorize("permitAll")
	public String loginForbidden(HttpServletRequest request, Model model) {
		AccessDeniedException exception =
				(AccessDeniedException) request.getAttribute(WebAttributes.ACCESS_DENIED_403);
		String msg = getMessage(exception);
		if (StringUtils.isNotBlank(msg)) {
			model.addAttribute("errorMessage", msg); //$NON-NLS-1$
		}
		return "/login"; //$NON-NLS-1$
	}

	private String getMessage(Exception exception) {
		String msg = null;
		if (exception != null) {
			msg = exception.getLocalizedMessage();
			if (StringUtils.isBlank(msg)) {
				msg = exception.getMessage();
			}
		}
		return msg;
	}
}
