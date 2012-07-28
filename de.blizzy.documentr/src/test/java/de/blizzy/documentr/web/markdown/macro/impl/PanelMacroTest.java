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
package de.blizzy.documentr.web.markdown.macro.impl;

import static org.junit.Assert.*;

import org.junit.Test;

public class PanelMacroTest {
	@Test
	public void getHtml() {
		PanelMacro macro = new PanelMacro();
		macro.setParameters("3"); //$NON-NLS-1$
		assertEquals("<div class=\"span3\">body</div>", macro.getHtml("body")); //$NON-NLS-1$ //$NON-NLS-2$

		macro = new PanelMacro();
		macro.setParameters("3 border"); //$NON-NLS-1$
		assertEquals("<div class=\"span3\"><div class=\"span12 panel-border\">body</div></div>", //$NON-NLS-1$
				macro.getHtml("body")); //$NON-NLS-1$
	}
}
