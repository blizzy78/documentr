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
package de.blizzy.documentr.access;

import lombok.Getter;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.springframework.util.Assert;

/**
 * <p>Represents an OpenID. An OpenID consists of a delegate ID and a real ID provided by an OpenID provider.
 * Delegate and real IDs can be equal.</p>
 *
 * <p>No two users must have OpenIDs that have the same real ID, but delegate IDs may be shared. For example,
 * &quot;yahoo.com&quot; is the common delegate ID for users using Yahoo's OpenID services.</p>
 */
public class OpenId {
	@Getter
	private String delegateId;
	@Getter
	private String realId;

	/**
	 * Constructs a new OpenID.
	 *
	 * @param delegateId the delegate ID
	 * @param realId the real ID
	 */
	public OpenId(String delegateId, String realId) {
		Assert.hasLength(delegateId);
		Assert.hasLength(realId);

		this.delegateId = delegateId;
		this.realId = realId;
	}

	@Override
	public boolean equals(Object o) {
		if (o == this) {
			return true;
		} else if ((o != null) && o.getClass().equals(getClass())) {
			OpenId other = (OpenId) o;
			return new EqualsBuilder()
				.append(delegateId, other.delegateId)
				.append(realId, other.realId)
				.isEquals();
		}
		return false;
	}

	@Override
	public int hashCode() {
		return new HashCodeBuilder().append(delegateId).append(realId).toHashCode();
	}
}
