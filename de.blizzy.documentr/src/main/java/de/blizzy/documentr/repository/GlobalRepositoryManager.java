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

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import javax.annotation.PostConstruct;

import org.eclipse.jgit.api.errors.GitAPIException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.common.base.Function;
import com.google.common.collect.Lists;
import com.google.common.eventbus.EventBus;

import de.blizzy.documentr.Settings;
import de.blizzy.documentr.access.User;

@Component
public class GlobalRepositoryManager {
	@Autowired
	private Settings settings;
	@Autowired
	private ProjectRepositoryManagerFactory repositoryManagerFactory;
	@Autowired
	private EventBus eventBus;
	private File reposDir;

	@PostConstruct
	public void init() {
		reposDir = new File(settings.getDocumentrDataDir(), "repositories"); //$NON-NLS-1$
	}

	public ILockedRepository createProjectCentralRepository(String projectName, User user)
			throws IOException, GitAPIException {

		ProjectRepositoryManager repoManager = repositoryManagerFactory.getManager(reposDir, projectName);
		return repoManager.createCentralRepository(user);
	}

	public ILockedRepository createProjectCentralRepository(String projectName, boolean bare, User user)
			throws IOException, GitAPIException {

		ProjectRepositoryManager repoManager = repositoryManagerFactory.getManager(reposDir, projectName);
		return repoManager.createCentralRepository(bare, user);
	}

	public ILockedRepository getProjectCentralRepository(String projectName) throws IOException {
		ProjectRepositoryManager repoManager = repositoryManagerFactory.getManager(reposDir, projectName);
		return repoManager.getCentralRepository();
	}

	public ILockedRepository getProjectCentralRepository(String projectName, boolean bare) throws IOException {
		ProjectRepositoryManager repoManager = repositoryManagerFactory.getManager(reposDir, projectName);
		return repoManager.getCentralRepository(bare);
	}

	public ILockedRepository createProjectBranchRepository(String projectName, String branchName, String startingBranch)
			throws IOException, GitAPIException {

		ProjectRepositoryManager repoManager = repositoryManagerFactory.getManager(reposDir, projectName);
		ILockedRepository repo = repoManager.createBranchRepository(branchName, startingBranch);
		eventBus.post(new BranchCreatedEvent(projectName, branchName));
		return repo;
	}

	public ILockedRepository getProjectBranchRepository(String projectName, String branchName) throws IOException, GitAPIException {
		ProjectRepositoryManager repoManager = repositoryManagerFactory.getManager(reposDir, projectName);
		return repoManager.getBranchRepository(branchName);
	}

	public List<String> listProjectBranches(String projectName) throws IOException {
		ProjectRepositoryManager repoManager = repositoryManagerFactory.getManager(reposDir, projectName);
		return repoManager.listBranches();
	}

	public List<String> listProjects() {
		List<String> result = Collections.emptyList();
		if (reposDir.isDirectory()) {
			FileFilter filter = new FileFilter() {
				@Override
				public boolean accept(File dir) {
					return dir.isDirectory();
				}
			};
			File[] files = reposDir.listFiles(filter);
			Function<File, String> function = new Function<File, String>() {
				@Override
				public String apply(File dir) {
					return dir.getName();
				}
			};
			result = Lists.newArrayList(Lists.transform(Lists.newArrayList(files), function));
			for (Iterator<String> iter = result.iterator(); iter.hasNext();) {
				String project = iter.next();
				if (project.startsWith("_")) { //$NON-NLS-1$
					iter.remove();
				}
			}
			Collections.sort(result);
		}
		return result;
	}

	public void importSampleContents(String projectName) throws IOException, GitAPIException {
		ProjectRepositoryManager repoManager = repositoryManagerFactory.getManager(reposDir, projectName);
		repoManager.importSampleContents();
	}
}
