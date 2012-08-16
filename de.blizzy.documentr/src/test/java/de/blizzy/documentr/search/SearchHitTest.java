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
package de.blizzy.documentr.search;

import static org.junit.Assert.*;

import java.util.List;

import org.junit.Before;
import org.junit.Test;

import com.google.common.collect.Lists;

public class SearchHitTest {
	private static final String PROJECT = "project"; //$NON-NLS-1$
	private static final String BRANCH = "branch"; //$NON-NLS-1$
	private static final String PAGE_PATH = "foo/bar"; //$NON-NLS-1$
	private static final String TITLE = "title"; //$NON-NLS-1$
	private static final String TEXT_HTML = "textHtml"; //$NON-NLS-1$
	private static final List<String> TAGS = Lists.newArrayList("tag1", "tag2", "tag3"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
	
	private SearchHit searchHit;
	
	@Before
	public void setUp() {
		searchHit = new SearchHit(PROJECT, BRANCH, PAGE_PATH, TITLE, TEXT_HTML, TAGS);
	}
	
	@Test
	public void getProjectName() {
		assertEquals(PROJECT, searchHit.getProjectName());
	}
	
	@Test
	public void getBranchName() {
		assertEquals(BRANCH, searchHit.getBranchName());
	}
	
	@Test
	public void getPath() {
		assertEquals(PAGE_PATH, searchHit.getPath());
	}
	
	@Test
	public void getTitle() {
		assertEquals(TITLE, searchHit.getTitle());
	}
	
	@Test
	public void getTextHtml() {
		assertEquals(TEXT_HTML, searchHit.getTextHtml());
	}
	
	@Test
	public void getTags() {
		assertEquals(TAGS, searchHit.getTags());
	}
}
