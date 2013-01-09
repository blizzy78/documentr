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
package de.blizzy.documentr.repository;

import lombok.Getter;
import de.blizzy.documentr.util.NotFoundException;

public class RepositoryNotFoundException extends NotFoundException {
	@Getter
	private String projectName;
	@Getter
	private String branchName;
	@Getter
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
}
