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
package de.blizzy.documentr.web.pagetree;

import static de.blizzy.documentr.DocumentrMatchers.*;
import static junit.framework.Assert.*;
import static org.mockito.Matchers.*;
import static org.mockito.Mockito.*;

import java.io.IOException;
import java.util.List;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;
import org.springframework.security.core.Authentication;

import com.google.common.base.Function;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import de.blizzy.documentr.Util;
import de.blizzy.documentr.access.DocumentrPermissionEvaluator;
import de.blizzy.documentr.access.Permission;
import de.blizzy.documentr.page.IPageStore;
import de.blizzy.documentr.page.Page;
import de.blizzy.documentr.repository.GlobalRepositoryManager;

public class PageTreeControllerTest {
	private GlobalRepositoryManager repoManager;
	private IPageStore pageStore;
	private PageTreeController pageTreeController;
	private Authentication authentication;
	private DocumentrPermissionEvaluator permissionEvaluator;

	@Before
	public void setUp() {
		repoManager = mock(GlobalRepositoryManager.class);
		
		pageStore = mock(IPageStore.class);
		
		permissionEvaluator = mock(DocumentrPermissionEvaluator.class);
		
		pageTreeController = new PageTreeController();
		pageTreeController.setGlobalRepositoryManager(repoManager);
		pageTreeController.setPermissionEvaluator(permissionEvaluator);
		pageTreeController.setPageStore(pageStore);
		
		authentication = mock(Authentication.class);
	}
	
	@Test
	@SuppressWarnings("boxing")
	public void getApplicationChildren() {
		when(repoManager.listProjects()).thenReturn(
				Lists.newArrayList("project1", "project2", "inaccessible")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		
		when(permissionEvaluator.hasProjectPermission(
					same(authentication), notEq("inaccessible"), same(Permission.VIEW))) //$NON-NLS-1$
				.thenReturn(true);
		when(permissionEvaluator.hasProjectPermission(
					same(authentication), eq("inaccessible"), same(Permission.VIEW))) //$NON-NLS-1$
				.thenReturn(false);
		
		List<ProjectTreeNode> result = pageTreeController.getApplicationChildren(authentication);
		Function<ProjectTreeNode, String> function = new Function<ProjectTreeNode, String>() {
			@Override
			public String apply(ProjectTreeNode node) {
				return node.getName();
			}
		};
		Set<String> projects = Sets.newHashSet(Lists.transform(result, function));
		assertEquals(Sets.newHashSet("project1", "project2"), projects); //$NON-NLS-1$ //$NON-NLS-2$
	}

	@Test
	@SuppressWarnings("boxing")
	public void getProjectChildren() throws IOException {
		when(repoManager.listProjectBranches("project")).thenReturn( //$NON-NLS-1$
				Lists.newArrayList("branch1", "branch2", "inaccessible")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		
		when(permissionEvaluator.hasBranchPermission(
					same(authentication), eq("project"), notEq("inaccessible"), same(Permission.VIEW))) //$NON-NLS-1$ //$NON-NLS-2$
				.thenReturn(true);
		when(permissionEvaluator.hasBranchPermission(
					same(authentication), eq("project"), eq("inaccessible"), same(Permission.VIEW))) //$NON-NLS-1$ //$NON-NLS-2$
				.thenReturn(false);
		
		List<BranchTreeNode> result = pageTreeController.getProjectChildren("project", authentication); //$NON-NLS-1$
		Function<BranchTreeNode, String> function = new Function<BranchTreeNode, String>() {
			@Override
			public String apply(BranchTreeNode node) {
				return node.getProjectName() + "/" + node.getName(); //$NON-NLS-1$
			}
		};
		Set<String> branches = Sets.newHashSet(Lists.transform(result, function));
		assertEquals(Sets.newHashSet("project/branch1", "project/branch2"), branches); //$NON-NLS-1$ //$NON-NLS-2$
	}
	
	@Test
	public void getBranchChildren() throws IOException {
		getBranchChildren(true);
		getBranchChildren(false);
	}
	
	@SuppressWarnings("boxing")
	private void getBranchChildren(boolean hasBranchPermission) throws IOException {
		when(pageStore.getPage("project", "branch", "home", false)) //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			.thenReturn(Page.fromText("title", "text")); //$NON-NLS-1$ //$NON-NLS-2$
		
		when(permissionEvaluator.hasBranchPermission(authentication, "project", "branch", Permission.EDIT_PAGE)) //$NON-NLS-1$ //$NON-NLS-2$
			.thenReturn(hasBranchPermission);
		
		List<PageTreeNode> result = pageTreeController.getBranchChildren(
				"project", "branch", Sets.newHashSet(Permission.EDIT_PAGE.name()), authentication); //$NON-NLS-1$ //$NON-NLS-2$
		Function<PageTreeNode, String> function = new Function<PageTreeNode, String>() {
			@Override
			public String apply(PageTreeNode node) {
				return node.getProjectName() + "/" + node.getBranchName() + "/" + //$NON-NLS-1$ //$NON-NLS-2$
						Util.toURLPagePath(node.getPath()) + "/" + node.getTitle() + "/" + //$NON-NLS-1$ //$NON-NLS-2$
						node.isHasBranchPermissions();
			}
		};
		Set<String> pages = Sets.newHashSet(Lists.transform(result, function));
		assertEquals(Sets.newHashSet("project/branch/home/title/" + hasBranchPermission), pages); //$NON-NLS-1$
	}

	@Test
	public void getPageChildren() throws IOException {
		getPageChildren(true);
		getPageChildren(false);
	}
	
	@SuppressWarnings("boxing")
	private void getPageChildren(boolean hasBranchPermission) throws IOException {
		when(pageStore.listChildPagePaths("project", "branch", "home/foo")) //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			.thenReturn(Lists.newArrayList("home/foo/page1", "home/foo/page2")); //$NON-NLS-1$ //$NON-NLS-2$
		
		when(pageStore.getPage("project", "branch", "home/foo/page1", false)) //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			.thenReturn(Page.fromText("title1", "text1")); //$NON-NLS-1$ //$NON-NLS-2$
		when(pageStore.getPage("project", "branch", "home/foo/page2", false)) //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			.thenReturn(Page.fromText("title2", "text2")); //$NON-NLS-1$ //$NON-NLS-2$

		when(permissionEvaluator.hasBranchPermission(authentication, "project", "branch", Permission.EDIT_PAGE)) //$NON-NLS-1$ //$NON-NLS-2$
			.thenReturn(hasBranchPermission);

		List<PageTreeNode> result = pageTreeController.getPageChildren(
				"project", "branch", "home/foo", Sets.newHashSet(Permission.EDIT_PAGE.name()), authentication); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		Function<PageTreeNode, String> function = new Function<PageTreeNode, String>() {
			@Override
			public String apply(PageTreeNode node) {
				return node.getProjectName() + "/" + node.getBranchName() + "/" + //$NON-NLS-1$ //$NON-NLS-2$
						Util.toURLPagePath(node.getPath()) + "/" + node.getTitle() + "/" + //$NON-NLS-1$ //$NON-NLS-2$
						node.isHasBranchPermissions();
			}
		};
		Set<String> pages = Sets.newHashSet(Lists.transform(result, function));
		assertEquals(Sets.newHashSet(
					"project/branch/home,foo,page1/title1/" + hasBranchPermission, //$NON-NLS-1$
					"project/branch/home,foo,page2/title2/" + hasBranchPermission), //$NON-NLS-1$
				pages);
	}
}
