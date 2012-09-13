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

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.common.collect.Sets;

import de.blizzy.documentr.Settings;

@Component
class GroovyMacroScanner {
	static final String MACROS_DIR_NAME = "macros"; //$NON-NLS-1$
	
	private static final Logger log = LoggerFactory.getLogger(GroovyMacroScanner.class);
	
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
		GroovyClassLoader classLoader = new GroovyClassLoader();
		Set<IMacro> macros = Sets.newHashSet();
		for (File file : macrosDir.listFiles(filter)) {
			try {
				Class<?> clazz = classLoader.parseClass(file);
				if (IMacro.class.isAssignableFrom(clazz)) {
					IMacro macro = (IMacro) clazz.newInstance();
					macros.add(macro);
				} else {
					Macro annotation = clazz.getAnnotation(Macro.class);
					if (annotation != null) {
						if (ISimpleMacro.class.isAssignableFrom(clazz)) {
							ISimpleMacro simpleMacro = (ISimpleMacro) clazz.newInstance();
							IMacro macro = new SimpleMacroMacro(simpleMacro, annotation, beanFactory);
							macros.add(macro);
						} else {
							log.warn("class {} not supported: {}", clazz.getName(), file.getName()); //$NON-NLS-1$
						}
					} else {
						log.warn("class {} not supported: {}", clazz.getName(), file.getName()); //$NON-NLS-1$
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
		}
		return macros;
	}
}
