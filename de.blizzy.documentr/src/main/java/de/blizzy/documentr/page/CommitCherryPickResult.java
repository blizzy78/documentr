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
package de.blizzy.documentr.page;

import lombok.Getter;

import org.springframework.util.Assert;

public class CommitCherryPickResult {
	public static enum Status {
		OK, CONFLICT, UNKNOWN;
	}

	@Getter
	private PageVersion pageVersion;
	@Getter
	private Status status;
	@Getter
	private String conflictText;

	public CommitCherryPickResult(PageVersion pageVersion, Status status) {
		Assert.notNull(pageVersion);
		Assert.notNull(status);

		this.pageVersion = pageVersion;
		this.status = status;
	}

	public CommitCherryPickResult(PageVersion pageVersion, String conflictText) {
		Assert.notNull(pageVersion);
		Assert.notNull(conflictText);

		this.pageVersion = pageVersion;
		this.conflictText = conflictText;
		status = Status.CONFLICT;
	}
}
