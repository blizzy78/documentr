package de.blizzy.documentr.repository;

import de.blizzy.documentr.NotFoundException;

public class RepositoryNotFoundException extends NotFoundException {
	private String projectName;
	private String branchName;
	private boolean central;

	private RepositoryNotFoundException(String projectName, String branchName, boolean central) {
		this.projectName = projectName;
		this.branchName = branchName;
		this.central = central;
	}

	RepositoryNotFoundException(String projectName, String branchName) {
		this(projectName, branchName, false);
	}
	
	static RepositoryNotFoundException forCentralRepository(String projectName) {
		return new RepositoryNotFoundException(projectName, null, true);
	}
	
	public String getProjectName() {
		return projectName;
	}
	
	public String getBranchName() {
		return branchName;
	}
	
	public boolean isCentralRepository() {
		return central;
	}
}
