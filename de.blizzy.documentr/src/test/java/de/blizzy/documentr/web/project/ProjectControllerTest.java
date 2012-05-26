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
package de.blizzy.documentr.web.project;

import static de.blizzy.documentr.DocumentrMatchers.*;
import static de.blizzy.documentr.TestUtil.*;
import static org.junit.Assert.*;
import static org.mockito.Matchers.*;
import static org.mockito.Mockito.*;

import java.io.IOException;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.junit.Before;
import org.junit.Test;
import org.springframework.security.core.Authentication;
import org.springframework.ui.Model;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.BindingResult;

import de.blizzy.documentr.access.User;
import de.blizzy.documentr.access.UserStore;
import de.blizzy.documentr.repository.GlobalRepositoryManager;

public class ProjectControllerTest {
	private static final String PROJECT = "project"; //$NON-NLS-1$
	private static final User USER = new User("currentUser", "pw", "admin@example.com", false, false); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
	
	private ProjectController projectController;
	private GlobalRepositoryManager repoManager;
	private Authentication authentication;

	@Before
	public void setUp() throws IOException {
		repoManager = mock(GlobalRepositoryManager.class);
		
		UserStore userStore = mock(UserStore.class);
		when(userStore.getUser(USER.getLoginName())).thenReturn(USER);

		projectController = new ProjectController();
		projectController.setGlobalRepositoryManager(repoManager);
		projectController.setUserStore(userStore);

		authentication = mock(Authentication.class);
		when(authentication.getName()).thenReturn(USER.getLoginName());
	}
	
	@Test
	public void getProject() {
		Model model = mock(Model.class);
		String view = projectController.getProject(PROJECT, model);
		assertEquals("/project/view", view); //$NON-NLS-1$
		
		verify(model).addAttribute("name", PROJECT); //$NON-NLS-1$
	}

	@Test
	public void createProject() {
		Model model = mock(Model.class);
		String view = projectController.createProject(model);
		assertEquals("/project/edit", view); //$NON-NLS-1$
		
		verify(model).addAttribute(eq("projectForm"), argProjectForm(StringUtils.EMPTY)); //$NON-NLS-1$
	}
	
	@Test
	public void saveProject() throws IOException, GitAPIException {
		ProjectForm projectForm = new ProjectForm(PROJECT);
		BindingResult bindingResult = new BeanPropertyBindingResult(projectForm, "projectForm"); //$NON-NLS-1$
		String view = projectController.saveProject(projectForm, bindingResult, authentication);
		assertEquals("/project/" + PROJECT, removeViewPrefix(view)); //$NON-NLS-1$
		assertRedirect(view);
		
		verify(repoManager).createProjectCentralRepository(PROJECT, USER);
	}
}
