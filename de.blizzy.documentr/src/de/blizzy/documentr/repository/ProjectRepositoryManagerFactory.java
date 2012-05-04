package de.blizzy.documentr.repository;

import java.io.File;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

@Component
public class ProjectRepositoryManagerFactory {
	@Autowired
	private LockManager lockManager;
	
	ProjectRepositoryManager getManager(File reposDir, String projectName) {
		Assert.notNull(reposDir);
		Assert.hasLength(projectName);
		
		File projectDir = new File(reposDir, projectName);
		return new ProjectRepositoryManager(projectName, projectDir, lockManager);
	}
	
	public void setLockManager(LockManager lockManager) {
		this.lockManager = lockManager;
	}
}
