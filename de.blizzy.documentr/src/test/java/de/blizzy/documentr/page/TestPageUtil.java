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

import java.lang.reflect.Field;
import java.util.Map;

import org.powermock.reflect.Whitebox;

public final class TestPageUtil {
	private TestPageUtil() {}

	public static void clearProjectEditTimes() {
		try {
			Field field = Whitebox.getField(PageUtil.class, "projectEditTimes"); //$NON-NLS-1$
			@SuppressWarnings("unchecked")
			Map<String, Long> projectEditTimes = (Map<String, Long>) field.get(null);
			projectEditTimes.clear();
		} catch (IllegalAccessException e) {
			throw new RuntimeException(e);
		}
	}

	public static void setParentPagePath(Page page, String parentPagePath) {
		page.setParentPagePath(parentPagePath);
	}
}
