package de.blizzy.documentr.repository;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import javax.annotation.PostConstruct;

import org.eclipse.jgit.api.errors.GitAPIException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.common.base.Function;
import com.google.common.collect.Lists;

import de.blizzy.documentr.Settings;

@Component
public class GlobalRepositoryManager {
	@Autowired
	private Settings settings;
	@Autowired
	private ProjectRepositoryManagerFactory repoManagerFactory;
	private File reposDir;
	
	@PostConstruct
	public void init() {
		reposDir = new File(settings.getDocumentrDataDir(), "repositories"); //$NON-NLS-1$
	}

	public ILockedRepository createProjectCentralRepository(String projectName) throws IOException, GitAPIException {
		ProjectRepositoryManager repoManager = repoManagerFactory.getManager(reposDir, projectName);
		return repoManager.createCentralRepository();
	}

	public ILockedRepository createProjectCentralRepository(String projectName, boolean bare) throws IOException, GitAPIException {
		ProjectRepositoryManager repoManager = repoManagerFactory.getManager(reposDir, projectName);
		return repoManager.createCentralRepository(bare);
	}
	
	public ILockedRepository getProjectCentralRepository(String projectName) throws IOException {
		ProjectRepositoryManager repoManager = repoManagerFactory.getManager(reposDir, projectName);
		return repoManager.getCentralRepository();
	}
	
	public ILockedRepository getProjectCentralRepository(String projectName, boolean bare) throws IOException {
		ProjectRepositoryManager repoManager = repoManagerFactory.getManager(reposDir, projectName);
		return repoManager.getCentralRepository(bare);
	}
	
	public ILockedRepository createProjectBranchRepository(String projectName, String branchName, String startingBranch)
			throws IOException, GitAPIException {
		
		ProjectRepositoryManager repoManager = repoManagerFactory.getManager(reposDir, projectName);
		return repoManager.createBranchRepository(branchName, startingBranch);
	}
	
	public ILockedRepository getProjectBranchRepository(String projectName, String branchName) throws IOException, GitAPIException {
		ProjectRepositoryManager repoManager = repoManagerFactory.getManager(reposDir, projectName);
		return repoManager.getBranchRepository(branchName);
	}

	public List<String> listProjectBranches(String projectName) throws IOException {
		ProjectRepositoryManager repoManager = repoManagerFactory.getManager(reposDir, projectName);
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
			result = new ArrayList<>(Lists.transform(Arrays.asList(files), function));
			Collections.sort(result);
		}
		return result;
	}
	
	public void setSettings(Settings settings) {
		this.settings = settings;
	}
	
	public void setRepositoryManagerFactory(ProjectRepositoryManagerFactory repoManagerFactory) {
		this.repoManagerFactory = repoManagerFactory;
	}
}
