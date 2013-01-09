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
import java.util.Collections;
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

import com.google.common.collect.Lists;

import de.blizzy.documentr.DocumentrConstants;
import de.blizzy.documentr.access.DocumentrPermissionEvaluator;
import de.blizzy.documentr.access.Permission;
import de.blizzy.documentr.page.IPageStore;
import de.blizzy.documentr.page.Page;
import de.blizzy.documentr.repository.GlobalRepositoryManager;
import de.blizzy.documentr.util.Util;

@Controller
@RequestMapping("/pageTree")
public class PageTreeController {
	@Autowired
	private GlobalRepositoryManager globalRepositoryManager;
	@Autowired
	private IPageStore pageStore;
	@Autowired
	private DocumentrPermissionEvaluator permissionEvaluator;

	@RequestMapping(value="/application/json", method=RequestMethod.POST)
	@ResponseBody
	@PreAuthorize("hasAnyProjectPermission(VIEW)")
	public List<ProjectTreeNode> getApplicationChildren(Authentication authentication) {
		List<ProjectTreeNode> result = Lists.newArrayList();
		List<String> projects = globalRepositoryManager.listProjects();
		for (String project : projects) {
			if (permissionEvaluator.hasProjectPermission(authentication, project, Permission.VIEW)) {
				result.add(new ProjectTreeNode(project));
			}
		}
		return result;
	}

	@RequestMapping(value="/project/{name:" + DocumentrConstants.PROJECT_NAME_PATTERN + "}/json",
			method=RequestMethod.POST, produces="application/json")
	@ResponseBody
	@PreAuthorize("hasAnyBranchPermission(#name, VIEW)")
	public List<BranchTreeNode> getProjectChildren(@PathVariable String name, Authentication authentication)
			throws IOException {

		List<String> branches = globalRepositoryManager.listProjectBranches(name);
		List<BranchTreeNode> result = Lists.newArrayList();
		for (String branch : branches) {
			if (permissionEvaluator.hasBranchPermission(authentication, name, branch, Permission.VIEW)) {
				result.add(new BranchTreeNode(name, branch));
			}
		}
		return result;
	}

	@RequestMapping(value="/branch/{projectName:" + DocumentrConstants.PROJECT_NAME_PATTERN + "}/" +
			"{name:" + DocumentrConstants.BRANCH_NAME_PATTERN + "}/json",
			method=RequestMethod.POST, produces="application/json")
	@ResponseBody
	@PreAuthorize("hasBranchPermission(#projectName, #name, VIEW)")
	public List<PageTreeNode> getBranchChildren(@PathVariable String projectName, @PathVariable String name,
			@RequestParam(required=false) Set<String> checkBranchPermissions, Authentication authentication)
			throws IOException {

		Page page = pageStore.getPage(projectName, name, DocumentrConstants.HOME_PAGE_NAME, false);
		PageTreeNode node = new PageTreeNode(projectName, name, DocumentrConstants.HOME_PAGE_NAME, page.getTitle());
		node.setHasBranchPermissions(hasBranchPermissions(authentication, projectName, name,
				checkBranchPermissions));
		return Collections.singletonList(node);
	}

	@RequestMapping(value="/page/{projectName:" + DocumentrConstants.PROJECT_NAME_PATTERN + "}/" +
			"{branchName:" + DocumentrConstants.BRANCH_NAME_PATTERN + "}/" +
			"{path:" + DocumentrConstants.PAGE_PATH_URL_PATTERN + "}/json",
			method=RequestMethod.POST, produces="application/json")
	@ResponseBody
	@PreAuthorize("hasBranchPermission(#projectName, #branchName, VIEW)")
	public List<AbstractTreeNode> getPageChildren(@PathVariable String projectName, @PathVariable String branchName,
			@PathVariable String path, @RequestParam(required=false) Set<String> checkBranchPermissions,
			@RequestParam(required=false) boolean pages, @RequestParam(required=false) boolean attachments,
			Authentication authentication) throws IOException {

		List<AbstractTreeNode> result = Lists.newArrayList();

		if (pages) {
			List<String> childPagePaths = pageStore.listChildPagePaths(projectName, branchName, Util.toRealPagePath(path));
			for (String childPagePath : childPagePaths) {
				Page page = pageStore.getPage(projectName, branchName, childPagePath, false);
				PageTreeNode node = new PageTreeNode(projectName, branchName, childPagePath, page.getTitle());
				node.setHasBranchPermissions(hasBranchPermissions(authentication, projectName, branchName,
						checkBranchPermissions));
				result.add(node);
			}
		}

		if (attachments) {
			List<String> pageAttachments = pageStore.listPageAttachments(projectName, branchName, Util.toRealPagePath(path));
			for (String attachment : pageAttachments) {
				result.add(new AttachmentTreeNode(projectName, branchName, path, attachment));
			}
		}

		return result;
	}

	private boolean hasBranchPermissions(Authentication authentication, String projectName,
			String branchName, Set<String> checkBranchPermissions) {

		boolean result = false;
		if (checkBranchPermissions != null) {
			result = true;
			for (String permission : checkBranchPermissions) {
				Permission p = Permission.valueOf(permission);
				if (!permissionEvaluator.hasBranchPermission(authentication, projectName, branchName, p)) {
					result = false;
					break;
				}
			}
		}
		return result;
	}
}
