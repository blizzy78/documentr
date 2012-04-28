package de.blizzy.documentr.repository;

import java.io.File;

import org.eclipse.jgit.lib.Repository;

public final class RepositoryUtil {
	private RepositoryUtil() {}
	
	public static void closeQuietly(Repository repo) {
		if (repo != null) {
			repo.close();
		}
	}
	
	static File getWorkingDir(Repository repo) {
		return repo.getDirectory().getParentFile();
	}
}
