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
package de.blizzy.documentr.web.attachment;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;

import javax.servlet.ServletContext;

import org.junit.Before;
import org.junit.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.ui.Model;
import org.springframework.web.multipart.MultipartFile;

import de.blizzy.documentr.DocumentrConstants;
import de.blizzy.documentr.access.User;
import de.blizzy.documentr.access.UserStore;
import de.blizzy.documentr.pagestore.Page;
import de.blizzy.documentr.pagestore.PageNotFoundException;
import de.blizzy.documentr.pagestore.PageStore;

public class AttachmentControllerTest {
	private static final String PROJECT = "project"; //$NON-NLS-1$
	private static final String BRANCH = "branch"; //$NON-NLS-1$
	private static final String PAGE_PATH_URL = "home,foo"; //$NON-NLS-1$
	private static final String PAGE_PATH = "home/foo"; //$NON-NLS-1$
	private static final User USER = new User("currentUser", "pw", "admin@example.com", false, false); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
	
	private AttachmentController attachmentController;
	private PageStore pageStore;
	private Authentication authentication;

	@Before
	public void setUp() throws IOException {
		pageStore = mock(PageStore.class);
		
		ServletContext servletContext = mock(ServletContext.class);
		when(servletContext.getMimeType("test.png")).thenReturn("image/png"); //$NON-NLS-1$ //$NON-NLS-2$

		UserStore userStore = mock(UserStore.class);
		when(userStore.getUser(USER.getLoginName())).thenReturn(USER);
		
		attachmentController = new AttachmentController();
		attachmentController.setPageStore(pageStore);
		attachmentController.setServletContext(servletContext);
		attachmentController.setUserStore(userStore);
		
		authentication = mock(Authentication.class);
		when(authentication.getName()).thenReturn(USER.getLoginName());
	}
	
	@Test
	public void getAttachments() {
		Model model = mock(Model.class);
		String view = attachmentController.getAttachments(PROJECT, BRANCH, PAGE_PATH_URL, model);
		assertEquals("/project/branch/page/attachments", view); //$NON-NLS-1$
		
		verify(model).addAttribute("projectName", PROJECT); //$NON-NLS-1$
		verify(model).addAttribute("branchName", BRANCH); //$NON-NLS-1$
		verify(model).addAttribute("pagePath", PAGE_PATH); //$NON-NLS-1$
	}
	
	@Test
	public void getAttachment() throws IOException {
		byte[] data = { 1, 2, 3 };
		String contentType = "image/png"; //$NON-NLS-1$
		Page attachment = Page.fromData(null, data, contentType);
		when(pageStore.getAttachment(PROJECT, BRANCH, PAGE_PATH, "test.png")).thenReturn(attachment); //$NON-NLS-1$
		
		ResponseEntity<byte[]> result = attachmentController.getAttachment(
				PROJECT, BRANCH, PAGE_PATH_URL, "test.png"); //$NON-NLS-1$
		assertTrue(Arrays.equals(data, result.getBody()));
		assertEquals(MediaType.parseMediaType(contentType), result.getHeaders().getContentType());
		assertEquals(HttpStatus.OK, result.getStatusCode());
	}

	@Test
	public void getAttachmentMustReturn404IfNotFound() throws IOException {
		when(pageStore.getAttachment(PROJECT, BRANCH, PAGE_PATH, "test.png")) //$NON-NLS-1$
			.thenThrow(new PageNotFoundException(PROJECT, BRANCH, PAGE_PATH + "/test.png")); //$NON-NLS-1$
		
		ResponseEntity<byte[]> result = attachmentController.getAttachment(
				PROJECT, BRANCH, PAGE_PATH_URL, "test.png"); //$NON-NLS-1$
		assertEquals(HttpStatus.NOT_FOUND, result.getStatusCode());
	}

	@Test
	public void createAttachment() {
		Model model = mock(Model.class);
		String view = attachmentController.createAttachment(PROJECT, BRANCH, PAGE_PATH_URL, model);
		assertEquals("/project/branch/page/editAttachment", view); //$NON-NLS-1$
		
		verify(model).addAttribute("projectName", PROJECT); //$NON-NLS-1$
		verify(model).addAttribute("branchName", BRANCH); //$NON-NLS-1$
		verify(model).addAttribute("pagePath", PAGE_PATH); //$NON-NLS-1$
	}
	
	@Test
	public void saveAttachment() throws IOException {
		byte[] data = { 1, 2, 3 };
		InputStream inputStream = new ByteArrayInputStream(data);
		MultipartFile file = mock(MultipartFile.class);
		when(file.getInputStream()).thenReturn(inputStream);
		when(file.getOriginalFilename()).thenReturn("test.png"); //$NON-NLS-1$
		Model model = mock(Model.class);

		String view = attachmentController.saveAttachment(PROJECT, BRANCH, PAGE_PATH_URL, file, model, authentication);
		assertEquals("/project/branch/page/attachments", view); //$NON-NLS-1$
		
		Page attachment = Page.fromData(null, data, "image/png"); //$NON-NLS-1$
		verify(pageStore).saveAttachment(PROJECT, BRANCH, PAGE_PATH, "test.png", attachment, USER); //$NON-NLS-1$
	}

	@Test
	public void saveAttachmentMustUseDefaultContentTypeIfUnknown() throws IOException {
		byte[] data = { 1, 2, 3 };
		InputStream inputStream = new ByteArrayInputStream(data);
		MultipartFile file = mock(MultipartFile.class);
		when(file.getInputStream()).thenReturn(inputStream);
		when(file.getOriginalFilename()).thenReturn("test.dat"); //$NON-NLS-1$
		Model model = mock(Model.class);
		
		String view = attachmentController.saveAttachment(PROJECT, BRANCH, PAGE_PATH_URL, file, model, authentication);
		assertEquals("/project/branch/page/attachments", view); //$NON-NLS-1$
		
		Page attachment = Page.fromData(null, data, DocumentrConstants.DEFAULT_MIME_TYPE);
		verify(pageStore).saveAttachment(PROJECT, BRANCH, PAGE_PATH, "test.dat", attachment, USER); //$NON-NLS-1$
	}
}
