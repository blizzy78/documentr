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
package de.blizzy.documentr.web.access;

import java.io.IOException;
import java.util.Collections;
import java.util.EnumSet;
import java.util.Set;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import com.google.common.collect.Sets;

import de.blizzy.documentr.DocumentrConstants;
import de.blizzy.documentr.access.Permission;
import de.blizzy.documentr.access.Role;
import de.blizzy.documentr.access.User;
import de.blizzy.documentr.access.UserStore;

@Controller
@RequestMapping("/role")
public class RoleController {
	@Autowired
	private UserStore userStore;

	@RequestMapping(value="/add", method=RequestMethod.GET)
	@PreAuthorize("hasApplicationPermission('ADMIN')")
	public String addRole(Model model) {
		RoleForm form = new RoleForm(null, Collections.<String>emptySet());
		model.addAttribute("roleForm", form); //$NON-NLS-1$
		return "/user/role/edit"; //$NON-NLS-1$
	}

	@RequestMapping(value="/edit/{roleName:" + DocumentrConstants.ROLE_NAME_PATTERN + "}", method=RequestMethod.GET)
	@PreAuthorize("hasApplicationPermission('ADMIN')")
	public String editRole(@PathVariable String roleName, Model model) throws IOException {
		Role role = userStore.getRole(roleName);
		Set<String> permissions = Sets.newHashSet();
		for (Permission permission : role.getPermissions()) {
			permissions.add(permission.name());
		}
		RoleForm form = new RoleForm(role.getName(), permissions);
		model.addAttribute("roleForm", form); //$NON-NLS-1$
		return "/user/role/edit"; //$NON-NLS-1$
	}

	@RequestMapping(value="/save", method=RequestMethod.POST)
	@PreAuthorize("hasApplicationPermission('ADMIN')")
	public String saveRole(@ModelAttribute @Valid RoleForm form, BindingResult bindingResult,
			Authentication authentication) throws IOException {
		
		if (bindingResult.hasErrors()) {
			return "/user/role/edit"; //$NON-NLS-1$
		}

		EnumSet<Permission> permissions = EnumSet.noneOf(Permission.class);
		for (String permission : form.getPermissions()) {
			permissions.add(Permission.valueOf(permission));
		}
		Role role = new Role(form.getName(), permissions);
		User user = userStore.getUser(authentication.getName());
		userStore.saveRole(role, user);
		
		return "redirect:/roles"; //$NON-NLS-1$
	}

	@ModelAttribute
	public RoleForm createRoleForm(@RequestParam(required=false) String name,
			@RequestParam(required=false) Set<String> permissions) {
		
		if (permissions == null) {
			permissions = Collections.emptySet();
		}
		return (name != null) ?
				new RoleForm(name, permissions) :
				null;
	}

	void setUserStore(UserStore userStore) {
		this.userStore = userStore;
	}
}
