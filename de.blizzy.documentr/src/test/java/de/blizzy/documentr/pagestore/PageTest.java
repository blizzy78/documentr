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
package de.blizzy.documentr.pagestore;

import static de.blizzy.documentr.TestUtil.*;
import static org.junit.Assert.*;

import org.junit.Test;

public class PageTest {
	private static final String PARENT = "parent"; //$NON-NLS-1$
	private static final String TITLE = "title"; //$NON-NLS-1$
	private static final String TEXT = "text"; //$NON-NLS-1$
	private static final String IMAGE_PNG = "image/png"; //$NON-NLS-1$

	@Test
	public void getParentPagePath() {
		PageTextData pageData = new PageTextData(TEXT); 
		Page page = new Page(PARENT, TITLE, pageData.getContentType(), pageData); 
		assertEquals(PARENT, page.getParentPagePath()); 
		
		page = Page.fromData(PARENT, new byte[] { 1, 2, 3 }, IMAGE_PNG); 
		assertEquals(PARENT, page.getParentPagePath()); 
		
		page = Page.fromText(PARENT, TITLE, TEXT); 
		assertEquals(PARENT, page.getParentPagePath()); 
		
		page = Page.fromMeta(PARENT, TITLE, IMAGE_PNG); 
		assertEquals(PARENT, page.getParentPagePath()); 
	}

	@Test
	public void getTitle() {
		PageTextData pageData = new PageTextData(TEXT); 
		Page page = new Page(PARENT, TITLE, pageData.getContentType(), pageData); 
		assertEquals(TITLE, page.getTitle()); 
		
		page = Page.fromData(PARENT, new byte[] { 1, 2, 3 }, IMAGE_PNG); 
		assertNull(page.getTitle());
		
		page = Page.fromText(PARENT, TITLE, TEXT); 
		assertEquals(TITLE, page.getTitle()); 
		
		page = Page.fromMeta(PARENT, TITLE, IMAGE_PNG); 
		assertEquals(TITLE, page.getTitle()); 
	}
	
	@Test
	public void getContentType() {
		PageTextData pageData = new PageTextData(TEXT); 
		Page page = new Page(PARENT, TITLE, pageData.getContentType(), pageData); 
		assertEquals(pageData.getContentType(), page.getContentType());
		
		page = Page.fromData(PARENT, new byte[] { 1, 2, 3 }, IMAGE_PNG); 
		assertEquals(IMAGE_PNG, page.getContentType()); 
		
		page = Page.fromText(PARENT, TITLE, TEXT); 
		assertEquals(PageTextData.CONTENT_TYPE, page.getContentType());
		
		page = Page.fromMeta(PARENT, TITLE, IMAGE_PNG); 
		assertEquals(IMAGE_PNG, page.getContentType()); 
	}
	
	@Test
	public void testEquals() {
		assertEqualsContract(
				Page.fromText(PARENT, TITLE, TEXT),
				Page.fromText(PARENT, TITLE, TEXT),
				Page.fromText(PARENT, TITLE, TEXT),
				Page.fromText(PARENT, TITLE, "bleh")); //$NON-NLS-1$
	}
	
	@Test
	public void testHashCode() {
		assertHashCodeContract(
				Page.fromText(PARENT, TITLE, TEXT),
				Page.fromText(PARENT, TITLE, TEXT));
	}
}
