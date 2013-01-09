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
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.eclipse.jgit.lib.PersonIdent;
import org.eclipse.jgit.revwalk.RevCommit;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

public final class PageUtil {
	private static Map<String, Long> projectEditTimes =
			Collections.synchronizedMap(Maps.<String, Long>newHashMap());

	private PageUtil() {}

	public static List<String> getPagePathHierarchy(String projectName, String branchName, String pagePath,
			IPageStore pageStore) throws IOException {

		List<String> result = Lists.newArrayList(pagePath);
		for (Page page = pageStore.getPage(projectName, branchName, pagePath, false);
			page.getParentPagePath() != null;
			page = pageStore.getPage(projectName, branchName, page.getParentPagePath(), false)) {

			result.add(0, page.getParentPagePath());
		}
		return result;
	}

	static void updateProjectEditTime(String projectName) {
		projectEditTimes.put(projectName, System.currentTimeMillis());
	}

	public static long getProjectEditTime(String projectName) {
		Long time = projectEditTimes.get(projectName);
		return (time != null) ? time : -1;
	}

	public static PageVersion toPageVersion(RevCommit commit) {
		PersonIdent committer = commit.getAuthorIdent();
		String lastEditedBy = null;
		if (committer != null) {
			lastEditedBy = committer.getName();
		}
		// TODO: would love to use authored time
		Date lastEdited = new Date(TimeUnit.MILLISECONDS.convert(commit.getCommitTime(), TimeUnit.SECONDS));
		String commitName = commit.getName();
		return new PageVersion(commitName, lastEditedBy, lastEdited);
	}
}
