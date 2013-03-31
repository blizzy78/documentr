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
package de.blizzy.documentr.repository;

import java.io.IOException;
import java.util.List;

import org.eclipse.jgit.api.errors.GitAPIException;

import de.blizzy.documentr.access.User;

public interface IGlobalRepositoryManager {
	ILockedRepository createProjectCentralRepository(String projectName, User user) throws IOException, GitAPIException;

	ILockedRepository createProjectCentralRepository(String projectName, boolean bare, User user) throws IOException, GitAPIException;

	ILockedRepository getProjectCentralRepository(String projectName) throws IOException;

	ILockedRepository getProjectCentralRepository(String projectName, boolean bare) throws IOException;

	ILockedRepository createProjectBranchRepository(String projectName, String branchName, String startingBranch)
			throws IOException, GitAPIException;

	ILockedRepository getProjectBranchRepository(String projectName, String branchName) throws IOException, GitAPIException;

	List<String> listProjectBranches(String projectName) throws IOException;

	List<String> listProjects();

	void importSampleContents(String projectName) throws IOException, GitAPIException;

	void renameProject(String projectName, String newProjectName, User user) throws IOException, GitAPIException;

	void deleteProject(String projectName, User user);
}
