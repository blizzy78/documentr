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
package de.blizzy.documentr.web.attachment;

import static de.blizzy.documentr.TestUtil.*;
import static org.junit.Assert.*;
import static org.mockito.Matchers.*;
import static org.mockito.Mockito.*;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.ui.Model;
import org.springframework.web.multipart.MultipartFile;

import de.blizzy.documentr.AbstractDocumentrTest;
import de.blizzy.documentr.DocumentrConstants;
import de.blizzy.documentr.access.User;
import de.blizzy.documentr.access.UserStore;
import de.blizzy.documentr.page.IPageStore;
import de.blizzy.documentr.page.Page;
import de.blizzy.documentr.page.PageMetadata;
import de.blizzy.documentr.page.PageNotFoundException;
import de.blizzy.documentr.page.TestPageUtil;

public class AttachmentControllerTest extends AbstractDocumentrTest {
	private static final String PROJECT = "project"; //$NON-NLS-1$
	private static final String BRANCH = "branch"; //$NON-NLS-1$
	private static final String PAGE_PATH_URL = DocumentrConstants.HOME_PAGE_NAME + ",foo"; //$NON-NLS-1$
	private static final String PAGE_PATH = DocumentrConstants.HOME_PAGE_NAME + "/foo"; //$NON-NLS-1$
	private static final User USER = new User("currentUser", "pw", "admin@example.com", false); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$

	@Mock
	private IPageStore pageStore;
	@Mock
	private Authentication authentication;
	@Mock
	private ServletContext servletContext;
	@Mock
	private UserStore userStore;
	@Mock
	private Model model;
	@Mock
	private HttpServletRequest request;
	@Mock
	private HttpSession session;
	@Mock
	private MultipartFile multipartFile;
	@InjectMocks
	private AttachmentController attachmentController;

	@Before
	public void setUp() throws IOException {
		when(servletContext.getMimeType("test.png")).thenReturn("image/png"); //$NON-NLS-1$ //$NON-NLS-2$

		when(userStore.getUser(USER.getLoginName())).thenReturn(USER);

		when(authentication.isAuthenticated()).thenReturn(true);
		when(authentication.getName()).thenReturn(USER.getLoginName());
	}

	@Test
	public void getAttachments() {
		String view = attachmentController.getAttachments(PROJECT, BRANCH, PAGE_PATH_URL, model);
		assertEquals("/project/branch/page/attachments", view); //$NON-NLS-1$

		verify(model).addAttribute("projectName", PROJECT); //$NON-NLS-1$
		verify(model).addAttribute("branchName", BRANCH); //$NON-NLS-1$
		verify(model).addAttribute("pagePath", PAGE_PATH); //$NON-NLS-1$
	}

	@Test
	public void getAttachment() throws IOException {
		when(session.getAttribute("authenticationCreationTime")).thenReturn(System.currentTimeMillis()); //$NON-NLS-1$

		when(request.getDateHeader(anyString())).thenReturn(-1L);
		when(request.getSession()).thenReturn(session);

		getAttachment(false, request);
	}

	@Test
	public void getAttachmentMustReturnNormallyIfModified() throws IOException {
		when(session.getAttribute("authenticationCreationTime")).thenReturn(System.currentTimeMillis()); //$NON-NLS-1$

		when(request.getDateHeader("If-Modified-Since")).thenReturn( //$NON-NLS-1$
				new GregorianCalendar(2000, Calendar.JANUARY, 1).getTimeInMillis());
		when(request.getSession()).thenReturn(session);

		getAttachment(false, request);
	}

	@Test
	public void getAttachmentAsDownload() throws IOException {
		when(session.getAttribute("authenticationCreationTime")).thenReturn(System.currentTimeMillis()); //$NON-NLS-1$

		when(request.getDateHeader(anyString())).thenReturn(-1L);
		when(request.getSession()).thenReturn(session);

		getAttachment(true, request);
	}

	private void getAttachment(boolean download, HttpServletRequest request) throws IOException {
		when(pageStore.getAttachmentMetadata(PROJECT, BRANCH, PAGE_PATH, "test.png")) //$NON-NLS-1$
			.thenReturn(new PageMetadata("user", new Date(), 123, "commit")); //$NON-NLS-1$ //$NON-NLS-2$

		byte[] data = { 1, 2, 3 };
		String contentType = "image/png"; //$NON-NLS-1$
		Page attachment = Page.fromData(data, contentType);
		when(pageStore.getAttachment(PROJECT, BRANCH, PAGE_PATH, "test.png")).thenReturn(attachment); //$NON-NLS-1$

		SecurityContextHolder.setContext(createSecurityContext(authentication));
		ResponseEntity<byte[]> result = attachmentController.getAttachment(
				PROJECT, BRANCH, PAGE_PATH_URL, "test.png", download, request); //$NON-NLS-1$
		SecurityContextHolder.clearContext();
		assertTrue(Arrays.equals(data, result.getBody()));
		assertEquals(MediaType.parseMediaType(contentType), result.getHeaders().getContentType());
		assertEquals(HttpStatus.OK, result.getStatusCode());
		if (download) {
			assertEquals("attachment; filename=\"test.png\"", //$NON-NLS-1$
					result.getHeaders().getFirst("Content-Disposition")); //$NON-NLS-1$
		}
	}

	@Test
	public void getAttachmentMustReturn404IfNotFound() throws IOException {
		when(request.getDateHeader(anyString())).thenReturn(-1L);

		when(pageStore.getAttachmentMetadata(PROJECT, BRANCH, PAGE_PATH, "test.png")) //$NON-NLS-1$
			.thenThrow(new PageNotFoundException(PROJECT, BRANCH, PAGE_PATH + "/test.png")); //$NON-NLS-1$

		ResponseEntity<byte[]> result = attachmentController.getAttachment(
				PROJECT, BRANCH, PAGE_PATH_URL, "test.png", false, request); //$NON-NLS-1$
		assertEquals(HttpStatus.NOT_FOUND, result.getStatusCode());
	}

	@Test
	public void getAttachmentMustReturn304IfNotModified() throws IOException {
		when(session.getAttribute("authenticationCreationTime")).thenReturn( //$NON-NLS-1$
				new GregorianCalendar(2012, Calendar.JUNE, 1).getTime().getTime());

		when(request.getDateHeader("If-Modified-Since")).thenReturn( //$NON-NLS-1$
				new GregorianCalendar(2012, Calendar.JUNE, 9).getTimeInMillis());
		when(request.getSession()).thenReturn(session);

		when(pageStore.getAttachmentMetadata(PROJECT, BRANCH, PAGE_PATH, "test.png")) //$NON-NLS-1$
			.thenReturn(new PageMetadata("user", new GregorianCalendar(2012, Calendar.JUNE, 1).getTime(), 123, "commit")); //$NON-NLS-1$ //$NON-NLS-2$

		TestPageUtil.clearProjectEditTimes();

		ResponseEntity<byte[]> result = attachmentController.getAttachment(
				PROJECT, BRANCH, PAGE_PATH_URL, "test.png", false, request); //$NON-NLS-1$
		assertEquals(HttpStatus.NOT_MODIFIED, result.getStatusCode());
	}

	@Test
	public void createAttachment() {
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
		when(multipartFile.getInputStream()).thenReturn(inputStream);
		when(multipartFile.getOriginalFilename()).thenReturn("test.png"); //$NON-NLS-1$

		String view = attachmentController.saveAttachment(PROJECT, BRANCH, PAGE_PATH_URL, multipartFile, authentication);
		assertEquals("/attachment/list/" + PROJECT + "/" + BRANCH + "/" + PAGE_PATH_URL, removeViewPrefix(view)); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		assertRedirect(view);

		Page attachment = Page.fromData(data, "image/png"); //$NON-NLS-1$
		verify(pageStore).saveAttachment(PROJECT, BRANCH, PAGE_PATH, "test.png", attachment, USER); //$NON-NLS-1$
	}

	@Test
	public void saveAttachmentMustUseDefaultContentTypeIfUnknown() throws IOException {
		byte[] data = { 1, 2, 3 };
		InputStream inputStream = new ByteArrayInputStream(data);
		when(multipartFile.getInputStream()).thenReturn(inputStream);
		when(multipartFile.getOriginalFilename()).thenReturn("test.dat"); //$NON-NLS-1$

		String view = attachmentController.saveAttachment(PROJECT, BRANCH, PAGE_PATH_URL, multipartFile, authentication);
		assertEquals("/attachment/list/" + PROJECT + "/" + BRANCH + "/" + PAGE_PATH_URL, removeViewPrefix(view)); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		assertRedirect(view);

		Page attachment = Page.fromData(data, DocumentrConstants.DEFAULT_MIME_TYPE);
		verify(pageStore).saveAttachment(PROJECT, BRANCH, PAGE_PATH, "test.dat", attachment, USER); //$NON-NLS-1$
	}

	@Test
	public void deleteAttachment() throws IOException {
		String view = attachmentController.deleteAttachment(PROJECT, BRANCH, PAGE_PATH_URL, "test.dat", authentication); //$NON-NLS-1$
		assertEquals("/attachment/list/" + PROJECT + "/" + BRANCH + "/" + PAGE_PATH_URL, removeViewPrefix(view)); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		assertRedirect(view);

		verify(pageStore).deleteAttachment(PROJECT, BRANCH, PAGE_PATH, "test.dat", USER); //$NON-NLS-1$
	}
}
