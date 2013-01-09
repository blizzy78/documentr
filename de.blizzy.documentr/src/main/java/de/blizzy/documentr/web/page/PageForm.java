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
package de.blizzy.documentr.web.page;

import javax.validation.constraints.NotNull;

import lombok.Getter;
import lombok.Setter;

import org.apache.commons.lang3.ArrayUtils;
import org.hibernate.validator.constraints.NotBlank;

import de.blizzy.documentr.validation.annotation.BranchNameNotBlacklisted;
import de.blizzy.documentr.validation.annotation.ProjectExists;
import de.blizzy.documentr.validation.annotation.ProjectNameNotBlacklisted;
import de.blizzy.documentr.validation.annotation.RoleExists;
import de.blizzy.documentr.validation.annotation.ValidBranchName;
import de.blizzy.documentr.validation.annotation.ValidPagePath;
import de.blizzy.documentr.validation.annotation.ValidProjectName;

public class PageForm {
	@NotNull(message="{project.name.blank}")
	@NotBlank(message="{project.name.blank}")
	@ValidProjectName
	@ProjectNameNotBlacklisted
	@ProjectExists
	@Getter
	private String projectName;
	@NotNull(message="{branch.name.blank}")
	@NotBlank(message="{branch.name.blank}")
	@ValidBranchName
	@BranchNameNotBlacklisted
	@Getter
	private String branchName;
	@ValidPagePath
	@Getter
	private String path;
	@ValidPagePath
	@Getter
	private String parentPagePath;
	@NotNull(message="{page.title.blank}")
	@NotBlank(message="{page.title.blank}")
	@Getter
	private String title;
	@Getter
	@Setter
	private String text;
	@RoleExists
	@Getter
	private String viewRestrictionRole;
	@Getter
	@Setter
	private String commit;
	private String[] tags;
	@Getter
	@Setter
	private Integer parentPageSplitRangeStart;
	@Getter
	@Setter
	private Integer parentPageSplitRangeEnd;

	PageForm(String projectName, String branchName, String path, String parentPagePath, String title, String text,
			String viewRestrictionRole, String commit, String[] tags) {

		this.projectName = projectName;
		this.branchName = branchName;
		this.path = path;
		this.parentPagePath = parentPagePath;
		this.title = title;
		this.text = text;
		this.viewRestrictionRole = viewRestrictionRole;
		this.commit = commit;
		this.tags = ArrayUtils.clone(tags);
	}

	public String[] getTags() {
		return ArrayUtils.clone(tags);
	}
}
