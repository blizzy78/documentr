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
package de.blizzy.documentr.access;

import javax.servlet.http.HttpServletRequest;

import lombok.Getter;
import lombok.Setter;

import org.apache.commons.lang3.StringUtils;
import org.springframework.security.access.PermissionEvaluator;
import org.springframework.security.access.expression.SecurityExpressionRoot;
import org.springframework.security.access.expression.method.MethodSecurityExpressionOperations;
import org.springframework.security.authentication.AuthenticationTrustResolverImpl;
import org.springframework.security.core.Authentication;

import de.blizzy.documentr.repository.GlobalRepositoryManager;
import de.blizzy.documentr.util.Util;

/** documentr's {@link SecurityExpressionRoot} for use in JSP and security annotations. */
public class DocumentrSecurityExpressionRoot extends SecurityExpressionRoot implements MethodSecurityExpressionOperations {
	public static final Permission ADMIN = Permission.ADMIN;
	public static final Permission VIEW = Permission.VIEW;
	public static final Permission EDIT_PROJECT = Permission.EDIT_PROJECT;
	public static final Permission EDIT_BRANCH = Permission.EDIT_BRANCH;
	public static final Permission EDIT_PAGE = Permission.EDIT_PAGE;

	@Getter
	@Setter
	public HttpServletRequest request;

	private GlobalRepositoryManager repoManager;
	private Object target;
	@Getter
	@Setter
	private Object returnObject;
	@Getter
	@Setter
	private Object filterObject;
	private DocumentrPermissionEvaluator permissionEvaluator;

	public DocumentrSecurityExpressionRoot(Authentication authentication, GlobalRepositoryManager repoManager) {
		super(authentication);

		this.repoManager = repoManager;

		setTrustResolver(new AuthenticationTrustResolverImpl());
	}

	public boolean hasApplicationPermission(Permission permission) {
		return permissionEvaluator.hasApplicationPermission(authentication, permission);
	}

	public boolean hasProjectPermission(String projectName, Permission permission) {
		return permissionEvaluator.hasProjectPermission(authentication, projectName, permission);
	}

	public boolean hasAnyProjectPermission(Permission permission) {
		return permissionEvaluator.hasAnyProjectPermission(authentication, permission);
	}

	public boolean hasBranchPermission(String projectName, String branchName, Permission permission) {
		return permissionEvaluator.hasBranchPermission(authentication, projectName, branchName, permission);
	}

	public boolean hasAnyBranchPermission(String projectName, Permission permission) {
		return permissionEvaluator.hasAnyBranchPermission(authentication, projectName, permission);
	}

	public boolean hasPagePermission(String projectName, String branchName, String path, Permission permission) {
		path = Util.toRealPagePath(path);
		return permissionEvaluator.hasPagePermission(authentication, projectName, branchName, path, permission);
	}

	public boolean hasPagePermissionInOtherBranches(String projectName, String branchName, String path, Permission permission) {
		path = Util.toRealPagePath(path);
		return permissionEvaluator.hasPagePermissionInOtherBranches(authentication, projectName, branchName, path, permission);
	}

	public boolean isAdmin(String loginName) {
		return permissionEvaluator.isAdmin(loginName);
	}

	public boolean isLastAdminRole(String roleName) {
		return permissionEvaluator.isLastAdminRole(roleName);
	}

	public boolean projectExists(String projectName) {
		return repoManager.listProjects().contains(StringUtils.defaultString(projectName));
	}

	void setThis(Object target) {
		this.target = target;
	}

	@Override
	public Object getThis() {
		return target;
	}

	@Override
	public void setPermissionEvaluator(PermissionEvaluator permissionEvaluator) {
		super.setPermissionEvaluator(permissionEvaluator);
		this.permissionEvaluator = (DocumentrPermissionEvaluator) permissionEvaluator;
	}
}
