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
package de.blizzy.documentr.markdown;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import de.blizzy.documentr.page.IPageStore;
import de.blizzy.documentr.page.Page;
import de.blizzy.documentr.page.PageTextData;
import de.blizzy.documentr.util.Util;

@Component
class PageRenderer implements IPageRenderer {
	private static final Logger log = LoggerFactory.getLogger(PageRenderer.class);
	
	@Autowired
	private IPageStore pageStore;
	@Autowired
	private MarkdownProcessor markdownProcessor;
	
	@Override
	public String getHtml(String projectName, String branchName, String path, Authentication authentication)
			throws IOException {

		if (log.isInfoEnabled()) {
			log.info("rendering page {}/{}/{} for user {}", //$NON-NLS-1$
					new Object[] { projectName, branchName, Util.toURLPagePath(path), authentication.getName() });
		}
		
		Page page = pageStore.getPage(projectName, branchName, path, true);
		String markdown = ((PageTextData) page.getData()).getText();
		return markdownProcessor.markdownToHTML(markdown, projectName, branchName, path, authentication);
	}

	@Override
	public String getHeaderHtml(String projectName, String branchName, String path, Authentication authentication)
			throws IOException {

		if (log.isInfoEnabled()) {
			log.info("rendering page {}/{}/{} header for user {}", //$NON-NLS-1$
					new Object[] { projectName, branchName, Util.toURLPagePath(path), authentication.getName() });
		}

		Page page = pageStore.getPage(projectName, branchName, path, true);
		String markdown = ((PageTextData) page.getData()).getText();
		return markdownProcessor.headerMarkdownToHTML(markdown, projectName, branchName, path, authentication);
	}
	
	void setPageStore(IPageStore pageStore) {
		this.pageStore = pageStore;
	}

	void setMarkdownProcessor(MarkdownProcessor markdownProcessor) {
		this.markdownProcessor = markdownProcessor;
	}
}
