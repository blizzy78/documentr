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
package de.blizzy.documentr.web.macro;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import javax.validation.Valid;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.google.common.base.Function;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import de.blizzy.documentr.DocumentrConstants;
import de.blizzy.documentr.markdown.macro.CompilationMessage;
import de.blizzy.documentr.markdown.macro.MacroFactory;

@Controller
@RequestMapping("/macro")
public class MacroController {
	@Autowired
	private MacroFactory macroFactory;

	@RequestMapping(value="/create", method=RequestMethod.GET)
	@PreAuthorize("hasApplicationPermission(ADMIN)")
	public String createMacro(Model model) {
		MacroForm form = new MacroForm(StringUtils.EMPTY, StringUtils.EMPTY);
		model.addAttribute("macroForm", form); //$NON-NLS-1$
		return "/macro/edit"; //$NON-NLS-1$
	}

	@RequestMapping(value="/edit/{name:" + DocumentrConstants.MACRO_NAME_PATTERN + "}", method=RequestMethod.GET)
	@PreAuthorize("hasApplicationPermission(ADMIN)")
	public String editMacro(@PathVariable String name, Model model) throws IOException {
		String code = macroFactory.getGroovyMacroCode(name);
		MacroForm form = new MacroForm(name, code);
		model.addAttribute("macroForm", form); //$NON-NLS-1$
		return "/macro/edit"; //$NON-NLS-1$
	}

	@RequestMapping(value="/verify/json", method=RequestMethod.POST)
	@PreAuthorize("hasApplicationPermission(ADMIN)")
	@ResponseBody
	public Map<String, Object> verifyMacro(@RequestParam String code) {
		Map<String, Object> result = Maps.newHashMap();
		List<CompilationMessage> verificationMessages = macroFactory.verifyGroovyMacro(code);
		if (!verificationMessages.isEmpty()) {
			Function<CompilationMessage, Map<String, Object>> function =
					new Function<CompilationMessage, Map<String, Object>>() {
						@Override
						public Map<String, Object> apply(CompilationMessage message) {
							return toJson(message);
						}
					};
			List<Map<String, Object>> messages = Lists.transform(verificationMessages, function);
			result.put("messages", messages); //$NON-NLS-1$
		}
		return result;
	}

	private Map<String, Object> toJson(CompilationMessage message) {
		Map<String, Object> result = Maps.newHashMap();
		result.put("type", message.getType().name()); //$NON-NLS-1$
		result.put("startLine", message.getStartLine()); //$NON-NLS-1$
		result.put("startColumn", message.getStartColumn()); //$NON-NLS-1$
		result.put("endLine", message.getEndLine()); //$NON-NLS-1$
		result.put("endColumn", message.getEndColumn()); //$NON-NLS-1$
		result.put("message", message.getMessage()); //$NON-NLS-1$
		return result;
	}

	@RequestMapping(value="/save", method=RequestMethod.POST)
	@PreAuthorize("hasApplicationPermission(ADMIN)")
	public String saveMacro(@ModelAttribute @Valid MacroForm form, BindingResult bindingResult) throws IOException {
		if (bindingResult.hasErrors()) {
			return "/macro/edit"; //$NON-NLS-1$
		}

		macroFactory.saveGroovyMacro(form.getName(), form.getCode());

		return "redirect:/macros"; //$NON-NLS-1$
	}

	@RequestMapping(value="/delete/{name:" + DocumentrConstants.MACRO_NAME_PATTERN + "}", method=RequestMethod.GET)
	@PreAuthorize("hasApplicationPermission(ADMIN)")
	public String deleteMacro(@PathVariable String name) throws IOException {
		macroFactory.deleteGroovyMacro(name);
		return "redirect:/macros"; //$NON-NLS-1$
	}

	@ModelAttribute
	public MacroForm createMacroForm(@RequestParam(required=false) String name,
			@RequestParam(required=false) String code) {

		return new MacroForm(name, code);
	}
}
