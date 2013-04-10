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
package de.blizzy.documentr.data;

import java.util.Map;

import org.springframework.security.core.Authentication;

import de.blizzy.documentr.access.DocumentrPermissionEvaluator;
import de.blizzy.documentr.access.UserStore;
import de.blizzy.documentr.page.IPageStore;

public class NullDataHandler implements IDataHandler {
	@Override
	public Object getData(String request, Map<String, String[]> parameterMap, Authentication authentication) {
		return null;
	}

	@Override
	public void setPermissionEvaluator(DocumentrPermissionEvaluator permissionEvaluator) {
	}

	@Override
	public void setUserStore(UserStore userStore) {
	}

	@Override
	public void setPageStore(IPageStore pageStore) {
	}
}
