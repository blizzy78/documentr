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

import static junit.framework.Assert.*;

import org.junit.Test;

public class YoutubeMacroTest {
	@Test
	public void getHtml() {
		YoutubeMacro macro = new YoutubeMacro();
		macro.setParameters("video123"); //$NON-NLS-1$
		assertEquals("<iframe width=\"560\" height=\"315\" src=\"http://www.youtube.com/embed/video123?" + //$NON-NLS-1$
				"rel=0\" frameborder=\"0\" allowfullscreen></iframe>", macro.getHtml()); //$NON-NLS-1$
	}
}
