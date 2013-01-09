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
package de.blizzy.documentr;

import static org.mockito.Matchers.*;

import java.util.EnumSet;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.hamcrest.Matcher;
import org.mockito.ArgumentMatcher;
import org.mockito.Matchers;
import org.mockito.internal.matchers.Equals;
import org.mockito.internal.matchers.Not;

import de.blizzy.documentr.access.OpenId;
import de.blizzy.documentr.access.Permission;
import de.blizzy.documentr.access.Role;
import de.blizzy.documentr.access.User;
import de.blizzy.documentr.page.Page;
import de.blizzy.documentr.page.PageTextData;
import de.blizzy.documentr.web.access.RoleForm;
import de.blizzy.documentr.web.access.UserForm;
import de.blizzy.documentr.web.branch.BranchForm;
import de.blizzy.documentr.web.macro.MacroForm;
import de.blizzy.documentr.web.page.PageForm;
import de.blizzy.documentr.web.project.ProjectForm;

public final class DocumentrMatchers {
	public static final String ANY = DocumentrMatchers.class.getName() + "_ANY"; //$NON-NLS-1$

	private DocumentrMatchers() {}

	public static Page argPage(String title, String text) {
		return argPage(ANY, title, text, ANY);
	}

	public static Page argPage(final String parentPagePath, final String title, final String text,
			final String viewRestrictionRole) {

		Matcher<Page> matcher = new ArgumentMatcher<Page>() {
			@Override
			public boolean matches(Object argument) {
				Page page = (Page) argument;
				String pageText = null;
				if (page.getData() instanceof PageTextData) {
					pageText = ((PageTextData) page.getData()).getText();
				}
				return (StringUtils.equals(parentPagePath, ANY) ||
							StringUtils.equals(page.getParentPagePath(), parentPagePath)) &&
						StringUtils.equals(page.getTitle(), title) &&
						StringUtils.equals(pageText, text) &&
						(StringUtils.equals(viewRestrictionRole, ANY) ||
							StringUtils.equals(page.getViewRestrictionRole(), viewRestrictionRole));
			}
		};
		return argThat(matcher);
	}

	public static PageForm argPageForm(final String projectName, final String branchName, final String path,
			final String parentPagePath, final String title, final String text, final String commit) {

		Matcher<PageForm> matcher = new ArgumentMatcher<PageForm>() {
			@Override
			public boolean matches(Object argument) {
				PageForm form = (PageForm) argument;
				return StringUtils.equals(form.getProjectName(), projectName) &&
						StringUtils.equals(form.getBranchName(), branchName) &&
						StringUtils.equals(form.getPath(), path) &&
						StringUtils.equals(form.getParentPagePath(), parentPagePath) &&
						StringUtils.equals(form.getTitle(), title) &&
						StringUtils.equals(form.getText(), text) &&
						StringUtils.equals(form.getCommit(), commit);
			}
		};
		return argThat(matcher);
	}

	public static User argUser(final String loginName, final String password, final String email,
			final boolean disabled) {

		Matcher<User> matcher = new ArgumentMatcher<User>() {
			@Override
			public boolean matches(Object argument) {
				User user = (User) argument;
				return StringUtils.equals(user.getLoginName(), loginName) &&
						StringUtils.equals(user.getPassword(), password) &&
						StringUtils.equals(user.getEmail(), email) &&
						(user.isDisabled() == disabled);
			}
		};
		return argThat(matcher);
	}

	public static User argUser(final String loginName, final String password, final String email,
			final boolean disabled, final Set<OpenId> openIds) {

		Matcher<User> matcher = new ArgumentMatcher<User>() {
			@Override
			public boolean matches(Object argument) {
				User user = (User) argument;
				return StringUtils.equals(user.getLoginName(), loginName) &&
						StringUtils.equals(user.getPassword(), password) &&
						StringUtils.equals(user.getEmail(), email) &&
						(user.isDisabled() == disabled) &&
						(user.getOpenIds().equals(openIds));
			}
		};
		return argThat(matcher);
	}

	public static UserForm argUserForm(final String loginName, final String password1, final String password2,
			final boolean disabled) {

		Matcher<UserForm> matcher = new ArgumentMatcher<UserForm>() {
			@Override
			public boolean matches(Object argument) {
				UserForm form = (UserForm) argument;
				return StringUtils.equals(form.getLoginName(), loginName) &&
						StringUtils.equals(form.getPassword1(), password1) &&
						StringUtils.equals(form.getPassword2(), password2) &&
						(form.isDisabled() == disabled);
			}
		};
		return argThat(matcher);
	}

	public static BranchForm argBranchForm(final String projectName, final String name, final String startingBranch) {
		Matcher<BranchForm> matcher = new ArgumentMatcher<BranchForm>() {
			@Override
			public boolean matches(Object argument) {
				BranchForm form = (BranchForm) argument;
				return StringUtils.equals(form.getProjectName(), projectName) &&
						StringUtils.equals(form.getName(), name) &&
						StringUtils.equals(form.getStartingBranch(), startingBranch);
			}
		};
		return argThat(matcher);
	}

	public static ProjectForm argProjectForm(final String name) {
		Matcher<ProjectForm> matcher = new ArgumentMatcher<ProjectForm>() {
			@Override
			public boolean matches(Object argument) {
				ProjectForm form = (ProjectForm) argument;
				return StringUtils.equals(form.getName(), name);
			}
		};
		return argThat(matcher);
	}

	public static RoleForm argRoleForm(final String name, final Set<String> permissions) {
		Matcher<RoleForm> matcher = new ArgumentMatcher<RoleForm>() {
			@Override
			public boolean matches(Object argument) {
				RoleForm form = (RoleForm) argument;
				return StringUtils.equals(form.getName(), name) &&
					form.getPermissions().equals(permissions);
			}
		};
		return argThat(matcher);
	}

	public static Role argRole(final String name, final EnumSet<Permission> permissions) {
		Matcher<Role> matcher = new ArgumentMatcher<Role>() {
			@Override
			public boolean matches(Object argument) {
				Role role = (Role) argument;
				return StringUtils.equals(role.getName(), name) &&
					role.getPermissions().equals(permissions);
			}
		};
		return argThat(matcher);
	}

	@SuppressWarnings("unchecked")
	public static String notEq(String s) {
		return Matchers.<String>argThat(new Not(new Equals(s)));
	}

	public static MacroForm argMacroForm(final String name, final String code) {
		Matcher<MacroForm> matcher = new ArgumentMatcher<MacroForm>() {
			@Override
			public boolean matches(Object argument) {
				MacroForm form = (MacroForm) argument;
				return StringUtils.equals(form.getName(), name) &&
						StringUtils.equals(form.getCode(), code);
			}
		};
		return argThat(matcher);
	}

	public static <T> T eqReflection(final T expected) {
		Matcher<T> matcher = new ArgumentMatcher<T>() {
			@Override
			public boolean matches(Object argument) {
				return EqualsBuilder.reflectionEquals(expected, argument);
			}
		};
		return argThat(matcher);
	}
}
