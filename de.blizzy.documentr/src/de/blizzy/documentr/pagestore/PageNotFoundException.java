package de.blizzy.documentr.pagestore;

import de.blizzy.documentr.NotFoundException;

public class PageNotFoundException extends NotFoundException {
	private String projectName;
	private String branchName;
	private String path;

	PageNotFoundException(String projectName, String branchName, String path) {
		this.projectName = projectName;
		this.branchName = branchName;
		this.path = path;
	}
	
	public String getProjectName() {
		return projectName;
	}
	
	public String getBranchName() {
		return branchName;
	}
	
	public String getPath() {
		return path;
	}
}
