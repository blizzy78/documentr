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
package de.blizzy.documentr.web.markdown.macro;

import org.springframework.util.Assert;

import de.blizzy.documentr.access.DocumentrPermissionEvaluator;
import de.blizzy.documentr.page.IPageStore;

class MacroContext implements IMacroContext {
	private IPageStore pageStore;
	private DocumentrPermissionEvaluator permissionEvaluator;

	MacroContext(IPageStore pageStore, DocumentrPermissionEvaluator permissionEvaluator) {
		Assert.notNull(pageStore);
		Assert.notNull(permissionEvaluator);

		this.pageStore = pageStore;
		this.permissionEvaluator = permissionEvaluator;
	}

	@Override
	public IPageStore getPageStore() {
		return pageStore;
	}

	@Override
	public DocumentrPermissionEvaluator getPermissionEvaluator() {
		return permissionEvaluator;
	}
}
