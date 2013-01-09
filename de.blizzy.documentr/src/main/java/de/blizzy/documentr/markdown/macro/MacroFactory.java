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
package de.blizzy.documentr.markdown.macro;

import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.ListableBeanFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.Lifecycle;
import org.springframework.stereotype.Component;

import com.google.common.base.Function;
import com.google.common.collect.Collections2;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

@Component
@Slf4j
public class MacroFactory implements Lifecycle {
	@Autowired
	private ListableBeanFactory beanFactory;
	@Autowired
	private GroovyMacroScanner groovyMacroScanner;
	private Map<String, IMacro> macros = Maps.newHashMap();
	private AtomicBoolean running = new AtomicBoolean();

	// cannot use @PostConstruct here because it is possible that not all beans in the context
	// have already been constructed, thus causing us to not pick up some macros
	@Override
	public void start() {
		rescanAllMacros();
		running.set(true);
	}

	@Override
	public void stop() {
		running.set(false);
	}

	@Override
	public boolean isRunning() {
		return running.get();
	}

	private void waitForRunning() {
		while (!running.get()) {
			log.trace("wait for running"); //$NON-NLS-1$
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				// ignore
			}
		}
	}

	private void rescanAllMacros() {
		macros.clear();
		registerMacrosFromClasses();
		registerGroovyMacros();
	}

	private void registerMacrosFromClasses() {
		log.info("registering macros from classes"); //$NON-NLS-1$
		Collection<IMacro> macros = beanFactory.getBeansOfType(IMacro.class).values();
		for (IMacro macro : macros) {
			registerMacro(macro);
		}
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
		waitForRunning();
		return macros.get(macroName);
	}

	public Set<IMacroDescriptor> getDescriptors() {
		waitForRunning();
		Function<IMacro, IMacroDescriptor> function = new Function<IMacro, IMacroDescriptor>() {
			@Override
			public IMacroDescriptor apply(IMacro macro) {
				return macro.getDescriptor();
			}
		};
		return Sets.newHashSet(Collections2.transform(macros.values(), function));
	}

	public List<String> listGroovyMacros() {
		return groovyMacroScanner.listMacros();
	}

	public String getGroovyMacroCode(String name) throws IOException {
		return groovyMacroScanner.getMacroCode(name);
	}

	public List<CompilationMessage> verifyGroovyMacro(String code) {
		return groovyMacroScanner.verifyMacro(code);
	}

	public void saveGroovyMacro(String name, String code) throws IOException {
		groovyMacroScanner.saveMacro(name, code);
		rescanAllMacros();
	}

	public void deleteGroovyMacro(String name) throws IOException {
		groovyMacroScanner.deleteMacro(name);
		rescanAllMacros();
	}
}
