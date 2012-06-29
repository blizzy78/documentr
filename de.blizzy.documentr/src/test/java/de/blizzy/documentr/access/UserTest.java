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
package de.blizzy.documentr.access;

import static org.junit.Assert.*;

import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.junit.Test;
import org.springframework.security.core.token.Sha512DigestUtils;

import com.google.common.collect.Sets;

public class UserTest {
	@Test
	public void getLoginName() {
		User user = new User("user", "password", "email", false); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		assertEquals("user", user.getLoginName()); //$NON-NLS-1$
	}

	@Test
	public void getPassword() {
		User user = new User("user", "password", "email", false); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		assertEquals("password", user.getPassword()); //$NON-NLS-1$
	}
	
	@Test
	public void getEmail() {
		User user = new User("user", "password", "email", false); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		assertEquals("email", user.getEmail()); //$NON-NLS-1$
	}
	
	@Test
	public void isDisabled() {
		User user = new User("user", "password", "email", false); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		assertFalse(user.isDisabled());
		user = new User("user", "password", "email", true); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		assertTrue(user.isDisabled());
	}
	
	@Test
	public void hashPassword() {
		String password = "password"; //$NON-NLS-1$
		String salt = "salt"; //$NON-NLS-1$
		assertEquals(Sha512DigestUtils.shaHex(password + salt), User.hashPassword(password, salt));
	}
	
	@Test
	public void getRandomSalt() {
		String salt = User.getRandomSalt();
		assertTrue(StringUtils.isNotBlank(salt));
	}
	
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
