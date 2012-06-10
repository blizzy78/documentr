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

import org.apache.commons.lang3.StringUtils;

import de.blizzy.documentr.web.markdown.macro.AbstractMacro;

public class LabelMacro extends AbstractMacro {
	@Override
	public String getHtml() {
		String type = StringUtils.substringBefore(getParameters(), " ").trim(); //$NON-NLS-1$
		String text = StringUtils.substringAfter(getParameters(), " ").trim(); //$NON-NLS-1$
		return "<span class=\"label label-" + type + "\">" + text + "</span>"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
	}
}
