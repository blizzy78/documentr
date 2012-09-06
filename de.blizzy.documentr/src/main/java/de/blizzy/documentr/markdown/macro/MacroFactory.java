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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.common.base.Function;
import com.google.common.collect.Collections2;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

@Component
public class MacroFactory {
	private static final Logger log = LoggerFactory.getLogger(MacroFactory.class);
	
	@Autowired
	private Collection<IMacro> contextMacros;
	@Autowired
	private GroovyMacroScanner groovyMacroScanner;
	private Map<String, IMacro> macros = Maps.newHashMap();

	@PostConstruct
	public void init() {
		registerMacrosFromClasses();
		registerGroovyMacros();
	}

	private void registerMacrosFromClasses() {
		log.info("registering macros from classes"); //$NON-NLS-1$
		for (IMacro macro : contextMacros) {
			registerMacro(macro);
		}
		contextMacros = null;
	}

	private void registerGroovyMacros() {
		Set<IMacro> macros = groovyMacroScanner.findGroovyMacros();
		for (IMacro macro : macros) {
			registerMacro(macro);
		}
	}

	private void registerMacro(IMacro macro) {
		IMacroDescriptor descriptor = macro.getDescriptor();
		String macroName = descriptor.getMacroName();
		log.debug("registering macro: {}", macroName); //$NON-NLS-1$
		if (!macros.containsKey(macroName)) {
			macros.put(macroName, macro);
		} else {
			log.warn("duplicate macro: {}", macroName); //$NON-NLS-1$
		}
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
