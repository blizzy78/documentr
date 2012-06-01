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
package de.blizzy.documentr.pagestore;

import java.io.IOException;
import java.util.List;

import org.springframework.cache.annotation.CacheEvict;

import de.blizzy.documentr.access.User;

public interface IPageStore {
	@CacheEvict(value="pageHTML", key="#projectName + '/' + #branchName + '/' + #path")
	void savePage(String projectName, String branchName, String path, Page page,
			User user) throws IOException;

	@CacheEvict(value="pageHTML", key="#projectName + '/' + #branchName + '/' + #pagePath")
	void saveAttachment(String projectName, String branchName, String pagePath, String name,
			Page attachment, User user) throws IOException;

	Page getPage(String projectName, String branchName, String path, boolean loadData) throws IOException;

	Page getAttachment(String projectName, String branchName, String pagePath, String name) throws IOException;

	List<String> listPagePaths(String projectName, String branchName) throws IOException;

	List<String> listPageAttachments(String projectName, String branchName, String pagePath) throws IOException;

	boolean isPageSharedWithOtherBranches(String projectName, String branchName, String path) throws IOException;

	List<String> getBranchesPageIsSharedWith(String projectName, String branchName, String path) throws IOException;

	List<String> listChildPagePaths(String projectName, String branchName, String path) throws IOException;

	@CacheEvict(value="pageHTML", key="#projectName + '/' + #branchName + '/' + #path")
	void deletePage(String projectName, String branchName, String path, User user) throws IOException;
	
	PageMetadata getPageMetadata(String projectName, String branchName, String path) throws IOException;
}
