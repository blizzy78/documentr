package de.blizzy.documentr.web.page;

public class PageForm {
	private String projectName;
	private String branchName;
	private String path;
	private String title;
	private String text;

	PageForm(String projectName, String branchName, String path, String title, String text) {
		this.projectName = projectName;
		this.branchName = branchName;
		this.path = path;
		this.title = title;
		this.text = text;
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

	public String getTitle() {
		return title;
	}
	
	public String getText() {
		return text;
	}
}
