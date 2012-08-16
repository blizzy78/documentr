/*
documentr - Edit, maintain, and present software documentation on the web.
Copyright (C) 2012 Maik Schreiber

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
package de.blizzy.documentr.search;

import java.util.List;

public class SearchHit {
	private String projectName;
	private String branchName;
	private String path;
	private String title;
	private String textHtml;
	private List<String> tags;

	SearchHit(String projectName, String branchName, String path, String title, String textHtml, List<String> tags) {
		this.projectName = projectName;
		this.branchName = branchName;
		this.path = path;
		this.title = title;
		this.textHtml = textHtml;
		this.tags = tags;
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
	
	public String getTextHtml() {
		return textHtml;
	}
	
	public List<String> getTags() {
		return tags;
	}
}
