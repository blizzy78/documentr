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

import org.apache.commons.lang3.StringEscapeUtils;

import de.blizzy.documentr.web.markdown.macro.AbstractMacro;
import de.blizzy.documentr.web.markdown.macro.MacroDescriptor;

public class ColorMacro extends AbstractMacro {
	public static final MacroDescriptor DESCRIPTOR = new MacroDescriptor("color", //$NON-NLS-1$
			"macro.color.title", "macro.color.description", ColorMacro.class, //$NON-NLS-1$ //$NON-NLS-2$
			"{{color [COLOR]}}[CONTENTS]{{/color}}"); //$NON-NLS-1$

	@Override
	public String getHtml(String body) {
		String color = getParameters();
		return "<span style=\"color: " + StringEscapeUtils.escapeHtml4(color) + ";\">" + //$NON-NLS-1$ //$NON-NLS-2$
				body + "</span>"; //$NON-NLS-1$
	}
}
