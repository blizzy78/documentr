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
package de.blizzy.documentr.markdown.macro;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.common.base.Function;
import com.google.common.collect.Collections2;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

@Component
public class MacroFactory {
	@Autowired
	private Collection<IMacro> contextMacros;
	private Map<String, IMacro> macros = Maps.newHashMap();

	@PostConstruct
	public void init() {
		for (IMacro macro : contextMacros) {
			IMacroDescriptor descriptor = macro.getDescriptor();
			macros.put(descriptor.getMacroName(), macro);
		}
		contextMacros = null;
	}
	
	public IMacro get(String macroName) {
		return macros.get(macroName);
	}
	
	public Set<IMacroDescriptor> getDescriptors() {
		Function<IMacro, IMacroDescriptor> function = new Function<IMacro, IMacroDescriptor>() {
			@Override
			public IMacroDescriptor apply(IMacro macro) {
				return macro.getDescriptor();
			}
		};
		return Sets.newHashSet(Collections2.transform(macros.values(), function));
	}

	void setContextMacros(Collection<IMacro> contextMacros) {
		this.contextMacros = contextMacros;
	}
}
