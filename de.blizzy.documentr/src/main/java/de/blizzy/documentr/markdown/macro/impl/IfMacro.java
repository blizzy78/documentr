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

import groovy.lang.Binding;
import groovy.lang.GroovyShell;
import lombok.extern.slf4j.Slf4j;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.codehaus.groovy.control.CompilerConfiguration;
import org.codehaus.groovy.control.customizers.ImportCustomizer;

import de.blizzy.documentr.DocumentrConstants;
import de.blizzy.documentr.markdown.HtmlSerializerContext;
import de.blizzy.documentr.markdown.macro.IMacroContext;
import de.blizzy.documentr.markdown.macro.IMacroRunnable;
import de.blizzy.documentr.markdown.macro.Macro;

@Macro(name="if", insertText="{{if EXPRESSION}}[CONTENTS]{{/if}}", cacheable=false)
@Slf4j
public class IfMacro implements IMacroRunnable {
	@Override
	public String getHtml(IMacroContext macroContext) {
		String code = macroContext.getParameters();
		if (StringUtils.isNotBlank(code)) {
			try {
				GroovyShell shell = getGroovyShell(macroContext);
				log.debug("evaluating expression: {}", code); //$NON-NLS-1$
				Object result = shell.evaluate(code);
				if ((result instanceof Boolean) && ((Boolean) result).booleanValue()) {
					return macroContext.getBody();
				}
			} catch (RuntimeException e) {
				log.error("error evaluating {{if}} expression", e); //$NON-NLS-1$
			}
		}
		return null;
	}

	private GroovyShell getGroovyShell(IMacroContext macroContext) {
		HtmlSerializerContext htmlSerializerContext = macroContext.getHtmlSerializerContext();
		Binding binding = new Binding();
		binding.setVariable("projectName", htmlSerializerContext.getProjectName()); //$NON-NLS-1$
		binding.setVariable("branchName", htmlSerializerContext.getBranchName()); //$NON-NLS-1$
		binding.setVariable("pagePath", htmlSerializerContext.getPagePath()); //$NON-NLS-1$
		ImportCustomizer importCustomizer = new ImportCustomizer();
		importCustomizer.addStarImports(DocumentrConstants.GROOVY_DEFAULT_IMPORTS.toArray(ArrayUtils.EMPTY_STRING_ARRAY));
		CompilerConfiguration compilerConfiguration = new CompilerConfiguration();
		compilerConfiguration.addCompilationCustomizers(importCustomizer);
		return new GroovyShell(binding, compilerConfiguration);
	}

	@Override
	public String cleanupHtml(String html) {
		return null;
	}
}
