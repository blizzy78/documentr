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
package de.blizzy.documentr.web.system;

import java.io.IOException;
import java.util.Map;

import javax.validation.Valid;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import com.google.common.base.Strings;
import com.google.common.collect.Maps;

import de.blizzy.documentr.access.UserStore;
import de.blizzy.documentr.system.SystemSettingsStore;

@Controller
@RequestMapping("/system")
public class SystemController {
	@Autowired
	private SystemSettingsStore systemSettingsStore;
	@Autowired
	private UserStore userStore;
	
	@RequestMapping(value="/edit", method=RequestMethod.GET)
	@PreAuthorize("hasApplicationPermission(ADMIN)")
	public String editSettings(Model model) {
		Map<String, String> settings = systemSettingsStore.getSettings();
		SystemSettingsForm form = new SystemSettingsForm(
				settings.get(SystemSettingsStore.DOCUMENTR_HOST),
				settings.get(SystemSettingsStore.SITE_NOTICE),
				settings.get(SystemSettingsStore.MAIL_HOST_NAME),
				Integer.parseInt(settings.get(SystemSettingsStore.MAIL_HOST_PORT)),
				settings.get(SystemSettingsStore.MAIL_SENDER_EMAIL),
				settings.get(SystemSettingsStore.MAIL_SENDER_NAME),
				settings.get(SystemSettingsStore.MAIL_SUBJECT_PREFIX),
				settings.get(SystemSettingsStore.MAIL_DEFAULT_LANGUAGE),
				Integer.parseInt(settings.get(SystemSettingsStore.BCRYPT_ROUNDS)));
		model.addAttribute("systemSettingsForm", form); //$NON-NLS-1$
		return "/system/edit"; //$NON-NLS-1$
	}
	
	@RequestMapping(value="/save", method=RequestMethod.POST)
	@PreAuthorize("hasApplicationPermission(ADMIN)")
	public String saveSystemSettings(@ModelAttribute @Valid SystemSettingsForm form, BindingResult bindingResult,
			Authentication authentication) throws IOException {

		if (bindingResult.hasErrors()) {
			return "/system/edit"; //$NON-NLS-1$
		}
		
		Map<String, String> settings = Maps.newHashMap();
		String documentrHost = form.getDocumentrHost();
		// remove trailing slash
		documentrHost = StringUtils.removeEnd(documentrHost, "/"); //$NON-NLS-1$
		
		settings.put(SystemSettingsStore.DOCUMENTR_HOST, documentrHost);
		settings.put(SystemSettingsStore.SITE_NOTICE, form.getSiteNotice());
		settings.put(SystemSettingsStore.MAIL_HOST_NAME, form.getMailHostName());
		settings.put(SystemSettingsStore.MAIL_HOST_PORT, String.valueOf(form.getMailHostPort()));
		settings.put(SystemSettingsStore.MAIL_SENDER_EMAIL, form.getMailSenderEmail());
		settings.put(SystemSettingsStore.MAIL_SENDER_NAME, form.getMailSenderName());
		settings.put(SystemSettingsStore.MAIL_SUBJECT_PREFIX, form.getMailSubjectPrefix());
		settings.put(SystemSettingsStore.MAIL_DEFAULT_LANGUAGE, form.getMailDefaultLanguage());
		settings.put(SystemSettingsStore.BCRYPT_ROUNDS, String.valueOf(form.getBcryptRounds()));
		systemSettingsStore.saveSettings(settings, userStore.getUser(authentication.getName()));
		return "redirect:/system/edit"; //$NON-NLS-1$
	}

	@ModelAttribute
	public SystemSettingsForm createSystemSettingsForm(
			@RequestParam(required=false) String documentrHost,
			@RequestParam(required=false) String siteNotice,
			@RequestParam(required=false) String mailHostName,
			@RequestParam(required=false) Integer mailHostPort,
			@RequestParam(required=false) String mailSenderEmail,
			@RequestParam(required=false) String mailSenderName,
			@RequestParam(required=false) String mailSubjectPrefix,
			@RequestParam(required=false) String mailDefaultLanguage,
			@RequestParam(required=false) Integer bcryptRounds) {
		
		return new SystemSettingsForm(
				Strings.emptyToNull(documentrHost),
				Strings.emptyToNull(siteNotice),
				Strings.emptyToNull(mailHostName),
				(mailHostPort != null) ? mailHostPort.intValue() : Integer.MIN_VALUE,
				Strings.emptyToNull(mailSenderEmail),
				Strings.emptyToNull(mailSenderName),
				Strings.emptyToNull(mailSubjectPrefix),
				Strings.emptyToNull(mailDefaultLanguage),
				(bcryptRounds != null) ? bcryptRounds.intValue() : Integer.MIN_VALUE);
	}
}
