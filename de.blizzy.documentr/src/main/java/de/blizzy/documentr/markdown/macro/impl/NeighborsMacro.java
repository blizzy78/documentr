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
package de.blizzy.documentr.markdown.macro.impl;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import lombok.extern.slf4j.Slf4j;

import org.apache.commons.lang3.StringUtils;
import org.springframework.security.core.Authentication;

import com.google.common.base.Stopwatch;

import de.blizzy.documentr.access.DocumentrPermissionEvaluator;
import de.blizzy.documentr.access.Permission;
import de.blizzy.documentr.markdown.HtmlSerializerContext;
import de.blizzy.documentr.markdown.macro.IMacroContext;
import de.blizzy.documentr.markdown.macro.IMacroRunnable;
import de.blizzy.documentr.markdown.macro.Macro;
import de.blizzy.documentr.page.IPageStore;
import de.blizzy.documentr.page.Page;
import de.blizzy.documentr.util.Util;

@Macro(name="neighbors", insertText="{{neighbors/}}", cacheable=false)
@Slf4j
public class NeighborsMacro implements IMacroRunnable {
	private HtmlSerializerContext htmlSerializerContext;
	private IPageStore pageStore;
	private DocumentrPermissionEvaluator permissionEvaluator;
	private String projectName;
	private String branchName;
	private String path;
	private int maxChildren;

	@Override
	public String getHtml(IMacroContext macroContext) {
		htmlSerializerContext = macroContext.getHtmlSerializerContext();
		path = htmlSerializerContext.getPagePath();
		if (path != null) {
			Map<String, String> params = Util.parseParameters(macroContext.getParameters());
			try {
				String childrenStr = StringUtils.defaultIfBlank(params.get("children"), "1"); //$NON-NLS-1$ //$NON-NLS-2$
				if (childrenStr.equals("all")) { //$NON-NLS-1$
					maxChildren = Integer.MAX_VALUE;
				} else {
					maxChildren = Integer.parseInt(childrenStr);
					maxChildren = Math.max(maxChildren, 1);
				}
			} catch (NumberFormatException e) {
				maxChildren = 1;
			}

			pageStore = macroContext.getPageStore();
			permissionEvaluator = macroContext.getPermissionEvaluator();
			projectName = htmlSerializerContext.getProjectName();
			branchName = htmlSerializerContext.getBranchName();

			if (log.isInfoEnabled()) {
				log.info("rendering neighbors for page: {}/{}/{}, user: {}", //$NON-NLS-1$
						projectName, branchName, Util.toUrlPagePath(path), htmlSerializerContext.getAuthentication().getName());
			}

			Stopwatch stopwatch = new Stopwatch().start();
			try {
				StringBuilder buf = new StringBuilder();
				buf.append("<ul class=\"well well-small nav nav-list neighbors pull-right\">") //$NON-NLS-1$
					.append(printParent(printLinkListItem(path, 1, maxChildren), path))
					.append("</ul>"); //$NON-NLS-1$
				return buf.toString();
			} catch (IOException e) {
				throw new RuntimeException(e);
			} finally {
				log.trace("rendering neighbors took {} ms", stopwatch.stop().elapsed(TimeUnit.MILLISECONDS)); //$NON-NLS-1$
			}
		} else {
			return null;
		}
	}

	private CharSequence printParent(CharSequence inner, String path) throws IOException {
		StringBuilder buf = new StringBuilder();
		Page page = pageStore.getPage(projectName, branchName, path, false);
		if (page.getParentPagePath() != null) {
			if (hasViewPermission(page.getParentPagePath())) {
				StringBuilder parentBuf = new StringBuilder();
				Page parentPage = pageStore.getPage(projectName, branchName, page.getParentPagePath(), false);
				String uri = htmlSerializerContext.getPageUri(page.getParentPagePath());
				parentBuf.append("<li><a href=\"").append(uri).append("\">") //$NON-NLS-1$ //$NON-NLS-2$
					.append(parentPage.getTitle())
					.append("</a>") //$NON-NLS-1$
					.append("<ul class=\"nav nav-list\">"); //$NON-NLS-1$

				if (path.equals(this.path)) {
					List<String> siblingPaths = pageStore.listChildPagePaths(
							projectName, branchName, page.getParentPagePath());
					for (String siblingPath : siblingPaths) {
						parentBuf.append(siblingPath.equals(path) ?
								inner :
								printLinkListItem(siblingPath, 1, 0));
					}
				} else {
					parentBuf.append(inner);
				}
				parentBuf.append("</ul>") //$NON-NLS-1$
					.append("</li>"); //$NON-NLS-1$

				buf.append(printParent(parentBuf, page.getParentPagePath()));
			}
		} else {
			buf.append(inner);
		}
		return buf;
	}

	private CharSequence printLinkListItem(String path, int childLevel, int maxChildren) throws IOException {
		StringBuilder buf = new StringBuilder();
		Page page = pageStore.getPage(projectName, branchName, path, false);
		String title = page.getTitle();
		String uri = htmlSerializerContext.getPageUri(path);
		boolean active = path.equals(this.path);
		if (active || hasViewPermission(path)) {
			buf.append("<li"); //$NON-NLS-1$
			if (active) {
				buf.append(" class=\"active\""); //$NON-NLS-1$
			}
			buf.append("><a href=\"").append(uri).append("\">") //$NON-NLS-1$ //$NON-NLS-2$
				.append(title)
				.append("</a>") //$NON-NLS-1$
				.append(printChildren(path, childLevel, maxChildren))
				.append("</li>"); //$NON-NLS-1$
		}
		return buf;
	}

	private CharSequence printChildren(String path, int childLevel, int maxChildren) throws IOException {
		if (childLevel <= maxChildren) {
			StringBuilder buf = new StringBuilder();
			List<String> childPaths = pageStore.listChildPagePaths(projectName, branchName, path);
			if (!childPaths.isEmpty()) {
				buf.append("<ul class=\"nav nav-list\">"); //$NON-NLS-1$
				for (String childPath : childPaths) {
					buf.append(printLinkListItem(childPath, childLevel + 1, maxChildren));
				}
				buf.append("</ul>"); //$NON-NLS-1$
			}
			return buf;
		} else {
			return StringUtils.EMPTY;
		}
	}

	private boolean hasViewPermission(String path) {
		Authentication authentication = htmlSerializerContext.getAuthentication();
		return permissionEvaluator.hasPagePermission(authentication, projectName, branchName, path, Permission.VIEW);
	}

	@Override
	public String cleanupHtml(String html) {
		return null;
	}
}
