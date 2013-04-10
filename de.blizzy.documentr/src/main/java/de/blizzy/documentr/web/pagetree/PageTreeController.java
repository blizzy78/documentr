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
package de.blizzy.documentr.web.pagetree;

import java.io.IOException;
import java.util.List;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import de.blizzy.documentr.DocumentrConstants;
import de.blizzy.documentr.access.DocumentrAnonymousAuthenticationFactory;
import de.blizzy.documentr.access.UserStore;
import de.blizzy.documentr.page.pagetree.AbstractTreeNode;
import de.blizzy.documentr.page.pagetree.BranchTreeNode;
import de.blizzy.documentr.page.pagetree.PageTreeNode;
import de.blizzy.documentr.page.pagetree.PageTreeNodesProvider;
import de.blizzy.documentr.page.pagetree.ProjectTreeNode;

@Controller
@RequestMapping("/pageTree")
public class PageTreeController {
	@Autowired
	private PageTreeNodesProvider nodesProvider;
	@Autowired
	private DocumentrAnonymousAuthenticationFactory authenticationFactory;

	@RequestMapping(value="/application/json", method=RequestMethod.POST)
	@ResponseBody
	@PreAuthorize("permitAll")
	public List<ProjectTreeNode> getApplicationChildren(Authentication authentication) throws IOException {
		return nodesProvider.getApplicationChildren(getAuthentication(authentication));
	}

	@RequestMapping(value="/project/{name:" + DocumentrConstants.PROJECT_NAME_PATTERN + "}/json",
			method=RequestMethod.POST, produces="application/json")
	@ResponseBody
	@PreAuthorize("permitAll")
	public List<BranchTreeNode> getProjectChildren(@PathVariable String name, Authentication authentication)
			throws IOException {

		return nodesProvider.getProjectChildren(name, getAuthentication(authentication));
	}

	@RequestMapping(value="/branch/{projectName:" + DocumentrConstants.PROJECT_NAME_PATTERN + "}/" +
			"{name:" + DocumentrConstants.BRANCH_NAME_PATTERN + "}/json",
			method=RequestMethod.POST, produces="application/json")
	@ResponseBody
	@PreAuthorize("permitAll")
	public List<PageTreeNode> getBranchChildren(@PathVariable String projectName, @PathVariable String name,
			@RequestParam(required=false) Set<String> checkBranchPermissions, Authentication authentication)
			throws IOException {

		return nodesProvider.getBranchChildren(projectName, name, checkBranchPermissions, authentication);
	}

	@RequestMapping(value="/page/{projectName:" + DocumentrConstants.PROJECT_NAME_PATTERN + "}/" +
			"{branchName:" + DocumentrConstants.BRANCH_NAME_PATTERN + "}/" +
			"{path:" + DocumentrConstants.PAGE_PATH_URL_PATTERN + "}/json",
			method=RequestMethod.POST, produces="application/json")
	@ResponseBody
	@PreAuthorize("permitAll")
	public List<AbstractTreeNode> getPageChildren(@PathVariable String projectName, @PathVariable String branchName,
			@PathVariable String path, @RequestParam(required=false) Set<String> checkBranchPermissions,
			@RequestParam(required=false) boolean pages, @RequestParam(required=false) boolean attachments,
			Authentication authentication) throws IOException {

		return nodesProvider.getPageChildren(projectName, branchName, path, checkBranchPermissions, pages, attachments,
				getAuthentication(authentication));
	}

	private Authentication getAuthentication(Authentication authentication) throws IOException {
		return (authentication != null) ? authentication : authenticationFactory.create(UserStore.ANONYMOUS_USER_LOGIN_NAME);
	}
}
