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
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import de.blizzy.documentr.DocumentrConstants;
import de.blizzy.documentr.access.User;
import de.blizzy.documentr.access.UserStore;
import de.blizzy.documentr.subscription.SubscriptionStore;
import de.blizzy.documentr.util.Util;

@Controller
@RequestMapping("/subscription")
public class SubscriptionController {
	@Autowired
	private SubscriptionStore subscriptionStore;
	@Autowired
	private UserStore userStore;

	@RequestMapping(value="/subscribe/{projectName:" + DocumentrConstants.PROJECT_NAME_PATTERN + "}/" +
			"{branchName:" + DocumentrConstants.BRANCH_NAME_PATTERN + "}/" +
			"{path:" + DocumentrConstants.PAGE_PATH_URL_PATTERN + "}/json",
			method=RequestMethod.GET)
	@PreAuthorize("isAuthenticated()")
	@ResponseBody
	public void subscribe(@PathVariable String projectName, @PathVariable String branchName, @PathVariable String path,
			Authentication authentication) throws IOException {

		User user = userStore.getUser(authentication.getName());
		subscriptionStore.subscribe(projectName, branchName, Util.toRealPagePath(path), user);
	}

	@RequestMapping(value="/unsubscribe/{projectName:" + DocumentrConstants.PROJECT_NAME_PATTERN + "}/" +
			"{branchName:" + DocumentrConstants.BRANCH_NAME_PATTERN + "}/" +
			"{path:" + DocumentrConstants.PAGE_PATH_URL_PATTERN + "}/json",
			method=RequestMethod.GET)
	@PreAuthorize("isAuthenticated()")
	@ResponseBody
	public void unsubscribe(@PathVariable String projectName, @PathVariable String branchName, @PathVariable String path,
			Authentication authentication) throws IOException {

		User user = userStore.getUser(authentication.getName());
		subscriptionStore.unsubscribe(projectName, branchName, Util.toRealPagePath(path), user);
	}
}
