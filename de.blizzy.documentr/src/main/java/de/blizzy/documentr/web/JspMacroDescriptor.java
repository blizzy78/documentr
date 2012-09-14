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
package de.blizzy.documentr.web;

import java.util.Locale;

import de.blizzy.documentr.markdown.macro.IMacroDescriptor;

public class JspMacroDescriptor {
	private String insertText;
	private String title;
	private String description;

	JspMacroDescriptor(IMacroDescriptor descriptor, Locale locale) {
		insertText = descriptor.getInsertText();
		title = descriptor.getTitle(locale);
		description = descriptor.getDescription(locale);
	}
	
	public String getInsertText() {
		return insertText;
	}
	
	public String getTitle() {
		return title;
	}
	
	public String getDescription() {
		return description;
	}
}