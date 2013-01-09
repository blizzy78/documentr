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

import java.util.Comparator;

import de.blizzy.documentr.access.GrantedAuthorityTarget.Type;

class RoleGrantedAuthorityComparator implements Comparator<RoleGrantedAuthority> {
	@Override
	public int compare(RoleGrantedAuthority rga1, RoleGrantedAuthority rga2) {
		GrantedAuthorityTarget target1 = rga1.getTarget();
		GrantedAuthorityTarget target2 = rga2.getTarget();
		Type type1 = target1.getType();
		Type type2 = target2.getType();
		int result = ((Integer) type1.ordinal()).compareTo(type2.ordinal());
		if (result != 0) {
			return result;
		}

		String targetId1 = target1.getTargetId();
		String targetId2 = target2.getTargetId();
		result = targetId1.compareToIgnoreCase(targetId2);
		if (result != 0) {
			return result;
		}

		return rga1.getRoleName().compareToIgnoreCase(rga2.getRoleName());
	}
}
