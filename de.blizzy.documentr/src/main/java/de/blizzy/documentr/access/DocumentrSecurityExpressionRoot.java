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
package de.blizzy.documentr.access;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.StringUtils;
import org.springframework.security.access.expression.SecurityExpressionRoot;
import org.springframework.security.core.Authentication;

import de.blizzy.documentr.access.GrantedAuthorityTarget.Type;
import de.blizzy.documentr.repository.GlobalRepositoryManager;

public class DocumentrSecurityExpressionRoot extends SecurityExpressionRoot {
	public HttpServletRequest request;
	
	private Object target;
	private GlobalRepositoryManager repoManager;

	public DocumentrSecurityExpressionRoot(Authentication authentication, GlobalRepositoryManager repoManager) {
		super(authentication);
		this.repoManager = repoManager;
	}

	public boolean hasApplicationPermission(String permission) {
		return hasPermission("application", Type.APPLICATION.name(), permission); //$NON-NLS-1$
	}

	public boolean hasProjectPermission(String projectName, String permission) {
		return hasPermission(projectName, Type.PROJECT.name(), permission);
	}
	
	public boolean hasAnyProjectPermission(String permission) {
		return hasPermission(GrantedAuthorityTarget.ANY, Type.PROJECT.name(), permission);
	}
	
	public boolean hasBranchPermission(String projectName, String branchName, String permission) {
		return hasPermission(projectName + "/" + branchName, Type.BRANCH.name(), permission); //$NON-NLS-1$
	}
	
	public boolean hasAnyBranchPermission(String projectName, String permission) {
		return hasPermission(projectName + "/" + GrantedAuthorityTarget.ANY, Type.BRANCH.name(), permission); //$NON-NLS-1$
	}
	
	public boolean hasPagePermission(String projectName, String branchName, String path, String permission) {
		return hasPermission(projectName + "/" + branchName + "/" + path, Type.PAGE.name(), permission); //$NON-NLS-1$ //$NON-NLS-2$
	}
	
	public boolean projectExists(String projectName) {
		return repoManager.listProjects().contains(StringUtils.defaultString(projectName));
	}

	void setThis(Object target) {
		this.target = target;
	}
	
	public Object getThis() {
		return target;
	}
	
	public void setRequest(HttpServletRequest request) {
		this.request = request;
	}
	
	public HttpServletRequest getRequest() {
		return request;
	}
}
