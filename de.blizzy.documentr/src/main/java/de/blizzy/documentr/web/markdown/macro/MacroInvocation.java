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

public class MacroInvocation {
	private IMacro macro;
	private String macroName;
	private String params;
	private String startMarker;
	private String endMarker;

	public MacroInvocation(IMacro macro, String macroName, String params) {
		this.macro = macro;
		this.macroName = macroName;
		this.params = params;
		
		long random = (long) (Math.random() * Long.MAX_VALUE);
		String markerLabel = macroName + "_" + String.valueOf(random); //$NON-NLS-1$
		startMarker = "__" + markerLabel + "__"; //$NON-NLS-1$ //$NON-NLS-2$
		endMarker = "__/" + markerLabel + "__"; //$NON-NLS-1$ //$NON-NLS-2$
	}
	
	public IMacro getMacro() {
		return macro;
	}

	public String getMacroName() {
		return macroName;
	}
	
	public String getParameters() {
		return params;
	}
	
	public String getStartMarker() {
		return startMarker;
	}

	public String getEndMarker() {
		return endMarker;
	}
}
