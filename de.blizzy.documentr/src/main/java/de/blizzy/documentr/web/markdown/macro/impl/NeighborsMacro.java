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
package de.blizzy.documentr.web.markdown.macro.impl;

import java.io.IOException;
import java.util.List;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.springframework.security.core.Authentication;

import de.blizzy.documentr.access.DocumentrPermissionEvaluator;
import de.blizzy.documentr.access.Permission;
import de.blizzy.documentr.page.IPageStore;
import de.blizzy.documentr.page.Page;
import de.blizzy.documentr.web.markdown.HtmlSerializerContext;
import de.blizzy.documentr.web.markdown.macro.AbstractMacro;
import de.blizzy.documentr.web.markdown.macro.MacroDescriptor;

public class NeighborsMacro extends AbstractMacro {
	public static final MacroDescriptor DESCRIPTOR = new MacroDescriptor("neighbors", //$NON-NLS-1$
			NeighborsMacro.class, "{{neighbors/}}"); //$NON-NLS-1$

	@SuppressWarnings("nls")
	private static final Pattern CLEANUP_RE = Pattern.compile(
			"(<li class=\"span3\"><a class=\"thumbnail\" (?:[^>]+)>" +
			"<img (?:[^>]+)/></a></li>)</ul>(?:[ \t]|<br/>)*" +
			"<ul class=\"thumbnails\">(<li class=\"span3\">" +
			"<a class=\"thumbnail\" (?:[^>]+)>)",
			Pattern.DOTALL);
	private static final String CLEANUP_REPLACE_WITH = "$1$2"; //$NON-NLS-1$

	@Override
	public String getHtml(String body) {
		HtmlSerializerContext context = getHtmlSerializerContext();
		if (context.getPagePath() != null) {
			try {
				StringBuilder buf = new StringBuilder();
				buf.append("<ul class=\"well well-small nav nav-list pull-right\">"); //$NON-NLS-1$
				buf.append(printParent(
						printLinkListItem(context.getPagePath(), context.getPagePath()),
						context.getPagePath(), context.getPagePath()));
				buf.append("</ul>"); //$NON-NLS-1$
				return buf.toString();
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		} else {
			return StringUtils.EMPTY;
		}
	}
	
	private StringBuilder printParent(CharSequence inner, String path, String currentPagePath) throws IOException {
		StringBuilder buf = new StringBuilder();
		HtmlSerializerContext context = getHtmlSerializerContext();
		IPageStore pageStore = getMacroContext().getPageStore();
		Page page = pageStore.getPage(context.getProjectName(), context.getBranchName(), path, false);
		if (page.getParentPagePath() != null) {
			Authentication authentication = context.getAuthentication();
			DocumentrPermissionEvaluator permissionEvaluator = getMacroContext().getPermissionEvaluator();
			if (permissionEvaluator.hasPagePermission(authentication, context.getProjectName(),
					context.getBranchName(), page.getParentPagePath(), Permission.VIEW)) {
				
				StringBuilder parentBuf = new StringBuilder();
				Page parentPage = pageStore.getPage(context.getProjectName(), context.getBranchName(),
					page.getParentPagePath(), false);
				String uri = context.getPageURI(page.getParentPagePath());
				parentBuf.append("<li><a href=\"").append(uri).append("\">") //$NON-NLS-1$ //$NON-NLS-2$
					.append(parentPage.getTitle())
					.append("</a>") //$NON-NLS-1$
					.append("<ul class=\"nav nav-list\">"); //$NON-NLS-1$
				
				if (path.equals(currentPagePath)) {
					List<String> siblingPaths = pageStore.listChildPagePaths(
							context.getProjectName(), context.getBranchName(), page.getParentPagePath());
					for (String siblingPath : siblingPaths) {
						parentBuf.append(siblingPath.equals(path) ?
								inner :
								printLinkListItem(siblingPath, currentPagePath));
					}
				} else {
					parentBuf.append(inner);
				}
				parentBuf.append("</ul>") //$NON-NLS-1$
					.append("</li>"); //$NON-NLS-1$
				
				buf.append(printParent(parentBuf, page.getParentPagePath(), currentPagePath));
			}
		} else {
			buf.append(inner);
		}
		return buf;
	}

	private StringBuilder printLinkListItem(String path, String currentPagePath) throws IOException {
		StringBuilder buf = new StringBuilder();
		HtmlSerializerContext context = getHtmlSerializerContext();
		IPageStore pageStore = getMacroContext().getPageStore();
		Page page = pageStore.getPage(context.getProjectName(), context.getBranchName(), path, false);
		String uri = context.getPageURI(path);
		if (path.equals(currentPagePath)) {
			buf.append("<li class=\"active\"><a href=\"").append(uri).append("\">") //$NON-NLS-1$ //$NON-NLS-2$
				.append(page.getTitle())
				.append("</a>") //$NON-NLS-1$
				.append(printChildren(path, currentPagePath))
				.append("</li>"); //$NON-NLS-1$
		} else {
			Authentication authentication = context.getAuthentication();
			DocumentrPermissionEvaluator permissionEvaluator = getMacroContext().getPermissionEvaluator();
			if (permissionEvaluator.hasPagePermission(authentication, context.getProjectName(),
					context.getBranchName(), path, Permission.VIEW)) {

				buf.append("<li><a href=\"").append(uri).append("\">") //$NON-NLS-1$ //$NON-NLS-2$
					.append(page.getTitle())
					.append("</a></li>"); //$NON-NLS-1$
			}
		}
		return buf;
	}

	private StringBuilder printChildren(String path, String currentPagePath) throws IOException {
		StringBuilder buf = new StringBuilder();
		HtmlSerializerContext context = getHtmlSerializerContext();
		IPageStore pageStore = getMacroContext().getPageStore();
		List<String> childPaths = pageStore.listChildPagePaths(context.getProjectName(), context.getBranchName(), path);
		if (!childPaths.isEmpty()) {
			buf.append("<ul class=\"nav nav-list\">"); //$NON-NLS-1$
			for (String childPath : childPaths) {
				buf.append(printLinkListItem(childPath, currentPagePath));
			}
			buf.append("</ul>"); //$NON-NLS-1$
		}
		return buf;
	}

	@Override
	public String cleanupHTML(String html) {
		return CLEANUP_RE.matcher(html).replaceAll(CLEANUP_REPLACE_WITH);
	}
	
	@Override
	public boolean isCacheable() {
		return false;
	}
}
