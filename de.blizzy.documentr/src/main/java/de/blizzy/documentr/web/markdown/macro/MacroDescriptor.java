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

public class MacroDescriptor {
	private String name;
	private String titleKey;
	private String descriptionKey;
	private Class<? extends IMacro> macroClass;
	private String insertText;

	public MacroDescriptor(String name, String titleKey, String descriptionKey, Class<? extends IMacro> macroClass,
			String insertText) {
		
		this.name = name;
		this.titleKey = titleKey;
		this.descriptionKey = descriptionKey;
		this.macroClass = macroClass;
		this.insertText = insertText;
	}

	public String getName() {
		return name;
	}
	
	public String getTitleKey() {
		return titleKey;
	}
	
	public String getDescriptionKey() {
		return descriptionKey;
	}
	
	public Class<? extends IMacro> getMacroClass() {
		return macroClass;
	}
	
	public String getInsertText() {
		return insertText;
	}
}
