package de.blizzy.documentr.repository;

import org.eclipse.jgit.lib.Repository;

public interface ILockedRepository {
	Repository r();
	void close();
}
