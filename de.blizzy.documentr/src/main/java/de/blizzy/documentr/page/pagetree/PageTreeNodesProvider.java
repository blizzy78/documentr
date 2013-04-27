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
package de.blizzy.documentr.page.pagetree;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import com.google.common.collect.Lists;

import de.blizzy.documentr.DocumentrConstants;
import de.blizzy.documentr.access.DocumentrPermissionEvaluator;
import de.blizzy.documentr.access.Permission;
import de.blizzy.documentr.page.IPageStore;
import de.blizzy.documentr.page.Page;
import de.blizzy.documentr.repository.IGlobalRepositoryManager;
import de.blizzy.documentr.util.Util;

@Component
public class PageTreeNodesProvider {
	@Autowired
	private DocumentrPermissionEvaluator permissionEvaluator;
	@Autowired
	private IGlobalRepositoryManager globalRepositoryManager;
	@Autowired
	private IPageStore pageStore;

	public List<ProjectTreeNode> getApplicationChildren(Authentication authentication) {
		List<ProjectTreeNode> result = Lists.newArrayList();
		if (permissionEvaluator.hasAnyProjectPermission(authentication, Permission.VIEW)) {
			List<String> projects = globalRepositoryManager.listProjects();
			for (String project : projects) {
				if (permissionEvaluator.hasProjectPermission(authentication, project, Permission.VIEW)) {
					result.add(new ProjectTreeNode(project));
				}
			}
		}
		return result;
	}

	public List<BranchTreeNode> getProjectChildren(String name, Authentication authentication) throws IOException {
		List<BranchTreeNode> result = Lists.newArrayList();
		if (permissionEvaluator.hasAnyBranchPermission(authentication, name, Permission.VIEW)) {
			List<String> branches = globalRepositoryManager.listProjectBranches(name);
			for (String branch : branches) {
				if (permissionEvaluator.hasBranchPermission(authentication, name, branch, Permission.VIEW)) {
					result.add(new BranchTreeNode(name, branch));
				}
			}
		}
		return result;
	}

	public List<PageTreeNode> getBranchChildren(String projectName, String name, Set<String> checkBranchPermissions,
			Authentication authentication) throws IOException {

		if (permissionEvaluator.hasBranchPermission(authentication, projectName, name, Permission.VIEW) &&
			permissionEvaluator.hasPagePermission(authentication, projectName, name, DocumentrConstants.HOME_PAGE_NAME, Permission.VIEW)) {

			Page page = pageStore.getPage(projectName, name, DocumentrConstants.HOME_PAGE_NAME, false);
			PageTreeNode node = new PageTreeNode(projectName, name, DocumentrConstants.HOME_PAGE_NAME, page.getTitle());
			node.setHasBranchPermissions(hasBranchPermissions(authentication, projectName, name,
					checkBranchPermissions));
			return Collections.singletonList(node);
		} else {
			return Collections.emptyList();
		}
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

	public List<AbstractTreeNode> getPageChildren(String projectName, String branchName, String path, Set<String> checkBranchPermissions,
			boolean pages, boolean attachments, Authentication authentication) throws IOException {

		List<AbstractTreeNode> result = Lists.newArrayList();
		if (permissionEvaluator.hasBranchPermission(authentication, projectName, branchName, Permission.VIEW)) {
			if (pages) {
				List<String> childPagePaths = pageStore.listChildPagePaths(projectName, branchName, Util.toRealPagePath(path));
				for (String childPagePath : childPagePaths) {
					if (permissionEvaluator.hasPagePermission(authentication, projectName, branchName, childPagePath, Permission.VIEW)) {
						Page page = pageStore.getPage(projectName, branchName, childPagePath, false);
						PageTreeNode node = new PageTreeNode(projectName, branchName, childPagePath, page.getTitle());
						node.setHasBranchPermissions(hasBranchPermissions(authentication, projectName, branchName,
								checkBranchPermissions));
						result.add(node);
					}
				}
			}

			if (attachments) {
				List<String> pageAttachments = pageStore.listPageAttachments(projectName, branchName, Util.toRealPagePath(path));
				for (String attachment : pageAttachments) {
					result.add(new AttachmentTreeNode(projectName, branchName, path, attachment));
				}
			}
		}
		return result;
	}
}
