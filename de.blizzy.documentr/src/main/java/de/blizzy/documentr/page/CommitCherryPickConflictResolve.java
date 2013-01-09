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

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.springframework.util.Assert;

public class CommitCherryPickConflictResolve {
	@Getter
	private String targetBranch;
	@Getter
	private String commit;
	@Getter
	private String text;

	public CommitCherryPickConflictResolve(String targetBranch, String commit, String text) {
		Assert.hasLength(targetBranch);
		Assert.hasLength(commit);
		Assert.notNull(text);

		this.targetBranch = targetBranch;
		this.commit = commit;
		this.text = text;
	}

	boolean isApplicable(String targetBranch, String commit) {
		Assert.hasLength(targetBranch);
		Assert.hasLength(commit);

		return targetBranch.equals(this.targetBranch) && commit.equals(this.commit);
	}

	@Override
	public boolean equals(Object o) {
		if (o == this) {
			return true;
		} else if ((o != null) && o.getClass().equals(getClass())) {
			CommitCherryPickConflictResolve other = (CommitCherryPickConflictResolve) o;
			return new EqualsBuilder()
				.append(targetBranch, other.targetBranch)
				.append(commit, other.commit)
				.append(text, other.text)
				.isEquals();
		}
		return false;
	}

	@Override
	public int hashCode() {
		return new HashCodeBuilder()
			.append(targetBranch)
			.append(commit)
			.append(text)
			.toHashCode();
	}
}
