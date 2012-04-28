package de.blizzy.documentr.repository;

import static org.junit.Assert.*;

import java.io.File;

import org.junit.Test;

public class ProjectRepositoryManagerFactoryTest {
	@Test
	public void getManager() {
		ProjectRepositoryManagerFactory factory = new ProjectRepositoryManagerFactory();
		ProjectRepositoryManager repoManager = factory.getManager(new File("."), "project"); //$NON-NLS-1$ //$NON-NLS-2$
		assertNotNull(repoManager);
	}
}
