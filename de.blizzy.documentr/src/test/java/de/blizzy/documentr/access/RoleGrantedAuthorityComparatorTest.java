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

import java.util.Collections;
import java.util.List;

import org.junit.Test;

import com.google.common.collect.Lists;

public class RoleGrantedAuthorityComparatorTest {
	@Test
	public void compare() {
		RoleGrantedAuthority rga1 = new RoleGrantedAuthority(
				new GrantedAuthorityTarget("project", GrantedAuthorityTarget.Type.PROJECT), "role1"); //$NON-NLS-1$ //$NON-NLS-2$
		RoleGrantedAuthority rga2 = new RoleGrantedAuthority(
				new GrantedAuthorityTarget("project/branch", GrantedAuthorityTarget.Type.BRANCH), "role2"); //$NON-NLS-1$ //$NON-NLS-2$
		RoleGrantedAuthority rga3 = new RoleGrantedAuthority(
				new GrantedAuthorityTarget("project/branch2", GrantedAuthorityTarget.Type.BRANCH), "role3"); //$NON-NLS-1$ //$NON-NLS-2$
		RoleGrantedAuthority rga4 = new RoleGrantedAuthority(
				new GrantedAuthorityTarget("project/branch2", GrantedAuthorityTarget.Type.BRANCH), "role4"); //$NON-NLS-1$ //$NON-NLS-2$
		List<RoleGrantedAuthority> expected = Lists.newArrayList(rga1, rga2, rga3, rga4);

		List<RoleGrantedAuthority> rgas = Lists.newArrayList(expected);

		// make sure the list is random
		while (rgas.equals(expected)) {
			Collections.shuffle(rgas);
		}

		RoleGrantedAuthorityComparator comparator = new RoleGrantedAuthorityComparator();
		Collections.sort(rgas, comparator);
		assertEquals(expected, rgas);
	}
}
