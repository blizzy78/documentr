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
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import lombok.extern.slf4j.Slf4j;

import org.apache.commons.lang3.StringUtils;
import org.springframework.context.MessageSource;
import org.springframework.security.core.Authentication;

import com.google.common.base.Stopwatch;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;

import de.blizzy.documentr.access.DocumentrPermissionEvaluator;
import de.blizzy.documentr.access.Permission;
import de.blizzy.documentr.markdown.HtmlSerializerContext;
import de.blizzy.documentr.markdown.macro.IMacroContext;
import de.blizzy.documentr.markdown.macro.IMacroRunnable;
import de.blizzy.documentr.markdown.macro.Macro;
import de.blizzy.documentr.page.IPageStore;
import de.blizzy.documentr.page.Page;
import de.blizzy.documentr.util.Util;

@Macro(name="neighbors", insertText="{{neighbors/}}", cacheable=false, dataHandler=NeighborsMacroDataHandler.class)
@Slf4j
public class NeighborsMacro implements IMacroRunnable {
	private HtmlSerializerContext htmlSerializerContext;
	private IPageStore pageStore;
	private DocumentrPermissionEvaluator permissionEvaluator;
	private Locale locale;
	private MessageSource messageSource;
	private String projectName;
	private String branchName;
	private String path;
	private int maxChildren;
	private boolean reorderAllowed;
	private LoadingCache<String, Page> pageCache = CacheBuilder.newBuilder().build(new CacheLoader<String, Page>() {
		@Override
		public Page load(String path) throws IOException {
			return pageStore.getPage(projectName, branchName, path, false);
		}
	});

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
			locale = macroContext.getLocale();
			messageSource = macroContext.getMessageSource();
			projectName = htmlSerializerContext.getProjectName();
			branchName = htmlSerializerContext.getBranchName();

			Authentication authentication = htmlSerializerContext.getAuthentication();
			reorderAllowed = (locale != null) && (messageSource != null) &&
					permissionEvaluator.hasBranchPermission(authentication, projectName, branchName, Permission.EDIT_PAGE);

			if (log.isInfoEnabled()) {
				log.info("rendering neighbors for page: {}/{}/{}, user: {}", //$NON-NLS-1$
						projectName, branchName, Util.toUrlPagePath(path), authentication.getName());
			}

			Stopwatch stopwatch = new Stopwatch().start();
			try {
				StringBuilder buf = new StringBuilder();
				Page page = getPage(path);
				buf.append("<span class=\"well well-small neighbors pull-right\"><ul class=\"nav nav-list\">") //$NON-NLS-1$
					.append(printParent(printLinkListItem(page, 1, maxChildren), path));
				if ((locale != null) && (messageSource != null)) {
					String hoverInfoText = messageSource.getMessage("neighborsItemsHoverExpandHelp", null, locale); //$NON-NLS-1$
					buf.append("<div class=\"hover-help\">").append(hoverInfoText).append("</div>"); //$NON-NLS-1$ //$NON-NLS-2$
				}
				buf.append("</ul></span>"); //$NON-NLS-1$
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
		Page page = getPage(path);
		if (page.getParentPagePath() != null) {
			if (hasViewPermission(page.getParentPagePath())) {
				StringBuilder parentBuf = new StringBuilder();
				Page parentPage = getPage(page.getParentPagePath());
				String uri = htmlSerializerContext.getPageUri(page.getParentPagePath());
				parentBuf.append("<li><a href=\"").append(uri).append("\">") //$NON-NLS-1$ //$NON-NLS-2$
					.append(parentPage.getTitle())
					.append("</a>") //$NON-NLS-1$
					.append("<ul class=\"nav nav-list\">"); //$NON-NLS-1$

				if (path.equals(this.path)) {
					List<Page> siblingPages = pageStore.listChildPagesOrdered(
							projectName, branchName, page.getParentPagePath(), locale);
					for (Page siblingPage : siblingPages) {
						parentBuf.append(siblingPage.getPath().equals(path) ?
								inner :
								printLinkListItem(siblingPage, 1, 0));
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

	private Page getPage(String path) throws IOException {
		try {
			return pageCache.get(path);
		} catch (ExecutionException e) {
			Throwable cause = e.getCause();
			if (cause instanceof IOException) {
				throw (IOException) cause;
			} else {
				throw new RuntimeException(cause);
			}
		}
	}

	private CharSequence printLinkListItem(Page page, int childLevel, int maxChildren) throws IOException {
		StringBuilder buf = new StringBuilder();
		String pagePath = page.getPath();
		if (hasViewPermission(pagePath)) {
			buf.append("<li data-path=\"").append(pagePath).append("\""); //$NON-NLS-1$ //$NON-NLS-2$
			if (reorderAllowed) {
				if (page.getOrderIndex() >= 0) {
					buf.append(" data-manual-order=\"true\""); //$NON-NLS-1$
				}
			}
			boolean active = pagePath.equals(path);
			if (active) {
				buf.append(" class=\"active\""); //$NON-NLS-1$
			}
			String title = page.getTitle();
			String uri = htmlSerializerContext.getPageUri(pagePath);
			buf.append("><a href=\"").append(uri).append("\">") //$NON-NLS-1$ //$NON-NLS-2$
				.append(title);
			if (active && reorderAllowed) {
				String buttonTitle = messageSource.getMessage("button.arrangePages", null, locale); //$NON-NLS-1$
				buf.append("<span class=\"buttons pull-right\"><i class=\"icon-move icon-white\" title=\"") //$NON-NLS-1$
					.append(buttonTitle)
					.append("\" onclick=\"startNeighborsArrange(); return false;\"></i></span>"); //$NON-NLS-1$
			}
			buf.append("</a>"); //$NON-NLS-1$
			buf.append(printChildren(pagePath, childLevel, maxChildren))
				.append("</li>"); //$NON-NLS-1$
		}
		return buf;
	}

	private CharSequence printChildren(String path, int childLevel, int maxChildren) throws IOException {
		if (childLevel <= maxChildren) {
			StringBuilder buf = new StringBuilder();
			List<Page> childPages = pageStore.listChildPagesOrdered(projectName, branchName, path, locale);
			if (!childPages.isEmpty()) {
				buf.append("<ul class=\"nav nav-list\">"); //$NON-NLS-1$
				for (Page childPage : childPages) {
					buf.append(printLinkListItem(childPage, childLevel + 1, maxChildren));
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
