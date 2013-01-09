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
package de.blizzy.documentr.page;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;

import de.blizzy.documentr.access.User;

public interface IPageStore {
	@Caching(evict={
			@CacheEvict(value="page_html", key="#projectName + '/' + #branchName + '/' + #path"),
			@CacheEvict(value="page_header_html", key="#projectName + '/' + #branchName + '/' + #path"),
			@CacheEvict(value="page_view_restriction_role", key="#projectName + '/' + #branchName + '/' + #path"),
			@CacheEvict(value="page_metadata", key="#projectName + '/' + #branchName + '/' + #path")
	})
	MergeConflict savePage(String projectName, String branchName, String path, Page page, String baseCommit,
			User user) throws IOException;

	@Caching(evict={
			@CacheEvict(value="page_html", key="#projectName + '/' + #branchName + '/' + #pagePath"),
			@CacheEvict(value="page_header_html", key="#projectName + '/' + #branchName + '/' + #pagePath"),
			@CacheEvict(value="page_metadata", key="#projectName + '/' + #branchName + '/' + #pagePath")
	})
	void saveAttachment(String projectName, String branchName, String pagePath, String name,
			Page attachment, User user) throws IOException;

	Page getPage(String projectName, String branchName, String path, boolean loadData) throws IOException;

	Page getPage(String projectName, String branchName, String path, String commit, boolean loadData) throws IOException;

	Page getAttachment(String projectName, String branchName, String pagePath, String name) throws IOException;

	List<String> listPageAttachments(String projectName, String branchName, String pagePath) throws IOException;

	boolean isPageSharedWithOtherBranches(String projectName, String branchName, String path) throws IOException;

	List<String> getBranchesPageIsSharedWith(String projectName, String branchName, String path) throws IOException;

	List<String> listChildPagePaths(String projectName, String branchName, String path) throws IOException;

	@Caching(evict={
			@CacheEvict(value="page_html", key="#projectName + '/' + #branchName + '/' + #path"),
			@CacheEvict(value="page_header_html", key="#projectName + '/' + #branchName + '/' + #path"),
			@CacheEvict(value="page_view_restriction_role", key="#projectName + '/' + #branchName + '/' + #path"),
			@CacheEvict(value="page_metadata", key="#projectName + '/' + #branchName + '/' + #path")
	})
	void deletePage(String projectName, String branchName, String path, User user) throws IOException;

	@Cacheable(value="page_metadata", key="#projectName + '/' + #branchName + '/' + #path")
	PageMetadata getPageMetadata(String projectName, String branchName, String path) throws IOException;

	PageMetadata getAttachmentMetadata(String projectName, String branchName, String pagePath, String name)
			throws IOException;

	@Caching(evict={
			@CacheEvict(value="page_html", allEntries=true),
			@CacheEvict(value="page_header_html", allEntries=true),
			@CacheEvict(value="page_view_restriction_role", allEntries=true),
			@CacheEvict(value="page_metadata", allEntries=true)
	})
	void relocatePage(String projectName, String branchName, String path, String newParentPagePath,
			User user) throws IOException;

	Map<String, String> getMarkdown(String projectName, String branchName, String path, Set<String> versions)
			throws IOException;

	List<PageVersion> listPageVersions(String projectName, String branchName, String path) throws IOException;

	@Caching(evict={
			@CacheEvict(value="page_html", key="#projectName + '/' + #branchName + '/' + #pagePath"),
			@CacheEvict(value="page_header_html", key="#projectName + '/' + #branchName + '/' + #pagePath"),
			@CacheEvict(value="page_metadata", key="#projectName + '/' + #branchName + '/' + #pagePath")
	})
	void deleteAttachment(String projectName, String branchName, String pagePath, String name, User user)
			throws IOException;

	@Cacheable(value="page_view_restriction_role", key="#projectName + '/' + #branchName + '/' + #path")
	String getViewRestrictionRole(String projectName, String branchName, String path) throws IOException;

	@Caching(evict={
			@CacheEvict(value="page_html", key="#projectName + '/' + #branchName + '/' + #path"),
			@CacheEvict(value="page_header_html", key="#projectName + '/' + #branchName + '/' + #path"),
			@CacheEvict(value="page_metadata", key="#projectName + '/' + #branchName + '/' + #path")
	})
	void restorePageVersion(String projectName, String branchName, String path, String version, User user)
			throws IOException;

	List<String> listAllPagePaths(String projectName, String branchName) throws IOException;
}
