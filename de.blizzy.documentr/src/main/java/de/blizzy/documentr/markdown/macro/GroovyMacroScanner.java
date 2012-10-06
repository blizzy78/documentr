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

import groovy.lang.GroovyClassLoader;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.util.Set;

import javax.annotation.PostConstruct;

import lombok.extern.slf4j.Slf4j;

import org.apache.commons.io.FileUtils;
import org.codehaus.groovy.control.CompilerConfiguration;
import org.codehaus.groovy.control.customizers.ImportCustomizer;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.common.collect.Sets;

import de.blizzy.documentr.Settings;

@Component
@Slf4j
class GroovyMacroScanner {
	@SuppressWarnings("nls")
	private static final String[] DEFAULT_IMPORTS = new String[] {
			"de.blizzy.documentr.access",
			"de.blizzy.documentr.markdown",
			"de.blizzy.documentr.markdown.macro",
			"de.blizzy.documentr.page",
			"de.blizzy.documentr.system",
			"org.apache.commons.lang3"
	};

	static final String MACROS_DIR_NAME = "macros"; //$NON-NLS-1$
	
	@Autowired
	private Settings settings;
	@Autowired
	private BeanFactory beanFactory;

	@PostConstruct
	public void init() throws IOException {
		File macrosDir = new File(settings.getDocumentrDataDir(), MACROS_DIR_NAME);
		if (!macrosDir.exists()) {
			FileUtils.forceMkdir(macrosDir);
		}
	}

	Set<IMacro> findGroovyMacros() {
		File macrosDir = new File(settings.getDocumentrDataDir(), MACROS_DIR_NAME);
		log.info("registering macros from Groovy scripts in folder {}", macrosDir.getAbsolutePath()); //$NON-NLS-1$
		FileFilter filter = new FileFilter() {
			@Override
			public boolean accept(File file) {
				return file.isFile() && file.getName().endsWith(".groovy"); //$NON-NLS-1$
			}
		};

		GroovyClassLoader classLoader = getGroovyClassLoader();
		Set<IMacro> macros = Sets.newHashSet();
		for (File file : macrosDir.listFiles(filter)) {
			IMacro macro = getMacro(file, classLoader);
			if (macro != null) {
				macros.add(macro);
			}
		}
		return macros;
	}

	private GroovyClassLoader getGroovyClassLoader() {
		ImportCustomizer importCustomizer = new ImportCustomizer();
		importCustomizer.addStarImports(DEFAULT_IMPORTS);
		CompilerConfiguration compilerConfiguration = new CompilerConfiguration();
		compilerConfiguration.addCompilationCustomizers(importCustomizer);
		ClassLoader contextClassLoader = Thread.currentThread().getContextClassLoader();
		GroovyClassLoader classLoader = new GroovyClassLoader(contextClassLoader, compilerConfiguration);
		return classLoader;
	}

	private IMacro getMacro(File file, GroovyClassLoader classLoader) {
		IMacro macro = null;
		try {
			Class<?> clazz = classLoader.parseClass(file);
			if (IMacro.class.isAssignableFrom(clazz)) {
				macro = (IMacro) clazz.newInstance();
			} else {
				Macro annotation = clazz.getAnnotation(Macro.class);
				if (annotation != null) {
					if (ISimpleMacro.class.isAssignableFrom(clazz)) {
						ISimpleMacro simpleMacro = (ISimpleMacro) clazz.newInstance();
						macro = new SimpleMacroMacro(simpleMacro, annotation, beanFactory);
					} else if (IMacroRunnable.class.isAssignableFrom(clazz)) {
						@SuppressWarnings("unchecked")
						Class<? extends IMacroRunnable> c = (Class<? extends IMacroRunnable>) clazz;
						macro = new MacroRunnableMacro(c, annotation, beanFactory);
					} else {
						log.warn("class {} not supported: {}", clazz.getName(), file.getName()); //$NON-NLS-1$
					}
				} else {
					log.warn("class {} not supported, no @Macro annotation found: {}", clazz.getName(), file.getName()); //$NON-NLS-1$
				}
			}
		} catch (IOException e) {
			log.warn("error loading Groovy macro: " + file.getName(), e); //$NON-NLS-1$
		} catch (InstantiationException e) {
			log.warn("error loading Groovy macro: " + file.getName(), e); //$NON-NLS-1$
		} catch (IllegalAccessException e) {
			log.warn("error loading Groovy macro: " + file.getName(), e); //$NON-NLS-1$
		} catch (RuntimeException e) {
			log.warn("error loading Groovy macro: " + file.getName(), e); //$NON-NLS-1$
		}
		return macro;
	}
}
