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
package de.blizzy.documentr.page;

import static de.blizzy.documentr.TestUtil.*;
import static org.junit.Assert.*;

import java.util.Arrays;

import org.junit.Test;

public class PageTest {
	private static final String TITLE = "title"; //$NON-NLS-1$
	private static final String TEXT = "text"; //$NON-NLS-1$
	private static final String IMAGE_PNG = "image/png"; //$NON-NLS-1$

	@Test
	public void getTitle() {
		PageTextData pageData = new PageTextData(TEXT);
		Page page = new Page(TITLE, pageData.getContentType(), pageData);
		assertEquals(TITLE, page.getTitle());

		page = Page.fromData(new byte[] { 1, 2, 3 }, IMAGE_PNG);
		assertNull(page.getTitle());

		page = Page.fromText(TITLE, TEXT);
		assertEquals(TITLE, page.getTitle());

		page = Page.fromMeta(TITLE, IMAGE_PNG);
		assertEquals(TITLE, page.getTitle());
	}

	@Test
	public void getContentType() {
		PageTextData pageData = new PageTextData(TEXT);
		Page page = new Page(TITLE, pageData.getContentType(), pageData);
		assertEquals(pageData.getContentType(), page.getContentType());

		page = Page.fromData(new byte[] { 1, 2, 3 }, IMAGE_PNG);
		assertEquals(IMAGE_PNG, page.getContentType());

		page = Page.fromText(TITLE, TEXT);
		assertEquals(PageTextData.CONTENT_TYPE, page.getContentType());

		page = Page.fromMeta(TITLE, IMAGE_PNG);
		assertEquals(IMAGE_PNG, page.getContentType());
	}

	@Test
	public void getData() {
		PageTextData pageData = new PageTextData(TEXT);
		Page page = new Page(TITLE, pageData.getContentType(), pageData);
		assertEquals(pageData, page.getData());

		page = Page.fromData(new byte[] { 1, 2, 3 }, IMAGE_PNG);
		assertTrue(Arrays.equals(new byte[] { 1, 2, 3 }, page.getData().getData()));

		page = Page.fromText(TITLE, TEXT);
		assertEquals(TEXT, ((PageTextData) page.getData()).getText());
	}

	@Test
	public void testEquals() {
		assertEqualsContract(
				Page.fromText(TITLE, TEXT),
				Page.fromText(TITLE, TEXT),
				Page.fromText(TITLE, TEXT),
				Page.fromText(TITLE, "bleh")); //$NON-NLS-1$
	}

	@Test
	public void testHashCode() {
		assertHashCodeContract(
				Page.fromText(TITLE, TEXT),
				Page.fromText(TITLE, TEXT));
	}
}
