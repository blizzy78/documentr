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

import static org.junit.Assert.*;

import java.util.Set;

import org.junit.Test;

import com.google.common.collect.Sets;

public class UserTest {
	@Test
	public void getOpenIds() {
		User user = new User("user", "password", "email", false); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		Set<OpenId> openIds = Sets.newHashSet(
				new OpenId("openId1", "realId1"), //$NON-NLS-1$ //$NON-NLS-2$
				new OpenId("openId2", "realId2")); //$NON-NLS-1$ //$NON-NLS-2$
		for (OpenId openId : openIds) {
			user.addOpenId(openId);
		}

		assertEquals(openIds, user.getOpenIds());
	}

	@Test
	public void addOpenId() {
		User user = new User("user", "password", "email", false); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		Set<OpenId> openIds = Sets.newHashSet(
				new OpenId("openId1", "realId1"), //$NON-NLS-1$ //$NON-NLS-2$
				new OpenId("openId2", "realId2")); //$NON-NLS-1$ //$NON-NLS-2$
		for (OpenId openId : openIds) {
			user.addOpenId(openId);
		}

		OpenId newOpenId = new OpenId("openId3", "realId3"); //$NON-NLS-1$ //$NON-NLS-2$
		user.addOpenId(newOpenId);
		openIds.add(newOpenId);

		assertEquals(openIds, user.getOpenIds());
	}

	@Test
	public void removeOpenId() {
		User user = new User("user", "password", "email", false); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		OpenId openId2 = new OpenId("openId2", "realId2"); //$NON-NLS-1$ //$NON-NLS-2$
		Set<OpenId> openIds = Sets.newHashSet(
				new OpenId("openId1", "realId1"), //$NON-NLS-1$ //$NON-NLS-2$
				openId2);
		for (OpenId openId : openIds) {
			user.addOpenId(openId);
		}

		user.removeOpenId(openId2.getDelegateId());
		openIds.remove(openId2);
		assertEquals(openIds, user.getOpenIds());
	}
}
