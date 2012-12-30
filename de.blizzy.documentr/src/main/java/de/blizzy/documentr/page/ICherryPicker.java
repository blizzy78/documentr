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
import java.util.Locale;
import java.util.Set;
import java.util.SortedMap;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Caching;

import de.blizzy.documentr.access.User;

public interface ICherryPicker {
	@Caching(evict={
			@CacheEvict(value="page_html", allEntries=true),
			@CacheEvict(value="page_header_html", allEntries=true),
			@CacheEvict(value="page_view_restriction_role", allEntries=true),
			@CacheEvict(value="page_metadata", allEntries=true)
	})
	SortedMap<String, List<CommitCherryPickResult>> cherryPick(String projectName, String branchName, String path,
			List<String> commits, Set<String> targetBranches, Set<CommitCherryPickConflictResolve> conflictResolves,
			boolean dryRun, User user, Locale locale) throws IOException;

	List<String> getCommitsList(String projectName, String branchName, String path,
			String version1, String version2) throws IOException;
}
