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
package de.blizzy.documentr.web.account;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import de.blizzy.documentr.access.User;
import de.blizzy.documentr.access.UserStore;

@Controller
@RequestMapping("/account")
public class AccountController {
	@Autowired
	private UserStore userStore;

	@RequestMapping(value="/myAccount", method=RequestMethod.GET)
	@PreAuthorize("isAuthenticated()")
	public String getMyAccount() {
		return "/account/index"; //$NON-NLS-1$
	}

	@RequestMapping(value="/openId", method=RequestMethod.GET)
	@PreAuthorize("isAuthenticated()")
	public String getMyOpenIds() {
		return "/account/openId"; //$NON-NLS-1$
	}

	@RequestMapping(value="/removeOpenId", method=RequestMethod.GET)
	@PreAuthorize("isAuthenticated()")
	public String removeOpenId(@RequestParam String openId, Authentication authentication) throws IOException {
		String loginName = authentication.getName();
		User user = userStore.getUser(loginName);
		user.removeOpenId(openId);
		userStore.saveUser(user, user);
		return "redirect:/account/openId"; //$NON-NLS-1$
	}
}
