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
package de.blizzy.documentr.markdown.macro.impl;

import java.util.Collections;
import java.util.Locale;
import java.util.Set;

import de.blizzy.documentr.markdown.macro.IMacro;
import de.blizzy.documentr.markdown.macro.IMacroDescriptor;
import de.blizzy.documentr.markdown.macro.IMacroRunnable;
import de.blizzy.documentr.markdown.macro.MacroSetting;

// cannot be a @Component because it must not be picked up by MacroFactory
public class UnknownMacroMacro implements IMacro {
	@Override
	public IMacroDescriptor getDescriptor() {
		return new IMacroDescriptor() {
			@Override
			public String getMacroName() {
				// not used
				return null;
			}

			@Override
			public String getTitle(Locale locale) {
				// not used
				return null;
			}

			@Override
			public String getDescription(Locale locale) {
				// not used
				return null;
			}

			@Override
			public String getInsertText() {
				// not used
				return null;
			}

			@Override
			public boolean isCacheable() {
				return true;
			}

			@Override
			public Set<MacroSetting> getSettings() {
				return Collections.emptySet();
			}
		};
	}

	@Override
	public IMacroRunnable createRunnable() {
		return new UnknownMacroMacroRunnable();
	}
}
