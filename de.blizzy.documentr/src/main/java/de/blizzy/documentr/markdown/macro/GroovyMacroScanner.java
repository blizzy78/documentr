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

import groovy.lang.GroovyClassLoader;
import groovy.lang.Script;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

import javax.annotation.PostConstruct;

import lombok.extern.slf4j.Slf4j;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.codehaus.groovy.control.CompilerConfiguration;
import org.codehaus.groovy.control.MultipleCompilationErrorsException;
import org.codehaus.groovy.control.customizers.ImportCustomizer;
import org.codehaus.groovy.control.messages.Message;
import org.codehaus.groovy.control.messages.SyntaxErrorMessage;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.common.base.Charsets;
import com.google.common.base.Function;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import de.blizzy.documentr.DocumentrConstants;
import de.blizzy.documentr.Settings;

@Component
@Slf4j
class GroovyMacroScanner {
	private static final class GroovyFileFilterImplementation implements FileFilter {
		@Override
		public boolean accept(File file) {
			return file.isFile() && file.getName().endsWith(".groovy"); //$NON-NLS-1$
		}
	}

	static final String MACROS_DIR_NAME = "macros"; //$NON-NLS-1$

	@Autowired
	private Settings settings;
	@Autowired
	private BeanFactory beanFactory;
	private File macrosDir;

	@PostConstruct
	public void init() throws IOException {
		macrosDir = new File(settings.getDocumentrDataDir(), MACROS_DIR_NAME);
		if (!macrosDir.exists()) {
			FileUtils.forceMkdir(macrosDir);
		}
	}

	Set<IMacro> findGroovyMacros() {
		log.info("registering macros from Groovy scripts in folder {}", macrosDir.getAbsolutePath()); //$NON-NLS-1$
		GroovyClassLoader classLoader = getGroovyClassLoader();
		Set<IMacro> macros = Sets.newHashSet();
		for (File file : findGroovyMacroFiles()) {
			IMacro macro = getMacro(file, classLoader);
			if (macro != null) {
				macros.add(macro);
			}
		}
		return macros;
	}

	private Set<File> findGroovyMacroFiles() {
		Set<File> result = Sets.newHashSet();
		for (File file : macrosDir.listFiles(new GroovyFileFilterImplementation())) {
			result.add(file);
		}
		return result;
	}

	private GroovyClassLoader getGroovyClassLoader() {
		ImportCustomizer importCustomizer = new ImportCustomizer();
		importCustomizer.addStarImports(DocumentrConstants.GROOVY_DEFAULT_IMPORTS.toArray(ArrayUtils.EMPTY_STRING_ARRAY));
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

	List<String> listMacros() {
		List<File> files = Lists.newArrayList(findGroovyMacroFiles());
		Function<File, String> function = new Function<File, String>() {
			@Override
			public String apply(File file) {
				return StringUtils.substringBeforeLast(file.getName(), ".groovy"); //$NON-NLS-1$
			}
		};
		List<String> result = Lists.newArrayList(Lists.transform(files, function));
		Collections.sort(result, new Comparator<String>() {
			@Override
			public int compare(String name1, String name2) {
				return name1.compareToIgnoreCase(name2);
			}
		});
		return result;
	}

	String getMacroCode(String name) throws IOException {
		File file = new File(macrosDir, name + ".groovy"); //$NON-NLS-1$
		return FileUtils.readFileToString(file, Charsets.UTF_8);
	}

	List<CompilationMessage> verifyMacro(String code) {
		List<CompilationMessage> result = Lists.newArrayList();
		if (StringUtils.isNotBlank(code)) {
			GroovyClassLoader groovyClassLoader = getGroovyClassLoader();
			try {
				Class<?> clazz = groovyClassLoader.parseClass(code);
				if (Script.class.isAssignableFrom(clazz)) {
					result.add(new CompilationMessage(CompilationMessage.Type.ERROR, 1, 1, 1, 1,
							"Script must represent a class implementing IMacro, ISimpleMacro, or " + //$NON-NLS-1$
							"IMacroRunnable.")); //$NON-NLS-1$
				} else if (!IMacro.class.isAssignableFrom(clazz)) {
					Macro annotation = clazz.getAnnotation(Macro.class);
					if (annotation != null) {
						if (!ISimpleMacro.class.isAssignableFrom(clazz) &&
							!IMacroRunnable.class.isAssignableFrom(clazz)) {

							result.add(new CompilationMessage(CompilationMessage.Type.ERROR, 1, 1, 1, 1,
									"Class " + clazz.getSimpleName() + " must implement IMacro or ISimpleMacro.")); //$NON-NLS-1$ //$NON-NLS-2$
						}
					} else {
						result.add(new CompilationMessage(CompilationMessage.Type.ERROR, 1, 1, 1, 1,
								"Class " + clazz.getSimpleName() + " not supported for macros: " + //$NON-NLS-1$ //$NON-NLS-2$
								"No @Macro annotation found.")); //$NON-NLS-1$
					}
				}
			} catch (MultipleCompilationErrorsException e) {
				@SuppressWarnings("unchecked")
				List<Message> errors = e.getErrorCollector().getErrors();
				result.addAll(toCompilationMessages(errors));
			} catch (RuntimeException e) {
				// ignore
			}
		}
		return result;
	}

	private List<CompilationMessage> toCompilationMessages(List<Message> errors) {
		List<CompilationMessage> messages = Lists.newArrayList();
		if (errors != null) {
			for (Message error : errors) {
				if (error instanceof SyntaxErrorMessage) {
					SyntaxErrorMessage syntaxError = (SyntaxErrorMessage) error;
					int startLine = syntaxError.getCause().getStartLine();
					int startColumn = syntaxError.getCause().getStartColumn();
					int endLine = syntaxError.getCause().getEndLine();
					int endColumn = syntaxError.getCause().getEndColumn();
					String message = syntaxError.getCause().getMessage();
					messages.add(new CompilationMessage(CompilationMessage.Type.ERROR,
							startLine, startColumn, endLine, endColumn, message));
				}
			}
		}
		return messages;
	}

	public void saveMacro(String name, String code) throws IOException {
		File file = new File(macrosDir, name + ".groovy"); //$NON-NLS-1$
		FileUtils.writeStringToFile(file, code, Charsets.UTF_8);
	}

	public void deleteMacro(String name) throws IOException {
		File file = new File(macrosDir, name + ".groovy"); //$NON-NLS-1$
		FileUtils.forceDelete(file);
	}
}
