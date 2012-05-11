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
package de.blizzy.documentr.web.markdown.macro;

import java.io.IOException;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

import de.blizzy.documentr.pagestore.Page;
import de.blizzy.documentr.pagestore.PageStore;
import de.blizzy.documentr.web.markdown.HtmlSerializerContext;

class NeighborsMacro extends AbstractMacro {
	@Override
	public String getHtml() {
		HtmlSerializerContext context = getHtmlSerializerContext();
		if (context.getPagePath() != null) {
			try {
				StringBuilder buf = new StringBuilder();
				buf.append("<div class=\"children-box\"><ul class=\"children\">"); //$NON-NLS-1$
				buf.append(printParent(
						printLinkListItem(context.getPagePath(), context.getPagePath()),
						context.getPagePath(), context.getPagePath()));
				buf.append("</ul></div>"); //$NON-NLS-1$
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
		PageStore pageStore = getPageStore();
		HtmlSerializerContext context = getHtmlSerializerContext();
		Page page = pageStore.getPage(context.getProjectName(), context.getBranchName(), path);
		if (page.getParentPagePath() != null) {
			StringBuilder parentBuf = new StringBuilder();
			Page parentPage = pageStore.getPage(context.getProjectName(), context.getBranchName(), page.getParentPagePath());
			String uri = context.getPageURI(page.getParentPagePath());
			parentBuf.append("<li><a href=\"").append(uri).append("\">") //$NON-NLS-1$ //$NON-NLS-2$
				.append(parentPage.getTitle())
				.append("</a>") //$NON-NLS-1$
				.append("<ul>"); //$NON-NLS-1$
			
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
			buf.append(printParent(parentBuf, page.getParentPagePath(), currentPagePath));
		} else {
			buf.append(inner);
		}
		return buf;
	}

	private StringBuilder printLinkListItem(String path, String currentPagePath) throws IOException {
		StringBuilder buf = new StringBuilder();
		PageStore pageStore = getPageStore();
		HtmlSerializerContext context = getHtmlSerializerContext();
		Page page = pageStore.getPage(context.getProjectName(), context.getBranchName(), path);
		if (path.equals(currentPagePath)) {
			buf.append("<li class=\"active\">").append(page.getTitle()); //$NON-NLS-1$
			buf.append(printChildren(path, currentPagePath));
			buf.append("</li>"); //$NON-NLS-1$
		} else {
			String uri = context.getPageURI(path);
			buf.append("<li><a href=\"").append(uri).append("\">") //$NON-NLS-1$ //$NON-NLS-2$
				.append(page.getTitle())
				.append("</a></li>"); //$NON-NLS-1$
		}
		return buf;
	}

	private StringBuilder printChildren(String path, String currentPagePath) throws IOException {
		StringBuilder buf = new StringBuilder();
		PageStore pageStore = getPageStore();
		HtmlSerializerContext context = getHtmlSerializerContext();
		List<String> childPaths = pageStore.listChildPagePaths(context.getProjectName(), context.getBranchName(), path);
		if (!childPaths.isEmpty()) {
			buf.append("<ul>"); //$NON-NLS-1$
			for (String childPath : childPaths) {
				buf.append(printLinkListItem(childPath, currentPagePath));
			}
			buf.append("</ul>"); //$NON-NLS-1$
		}
		return buf;
	}
}
