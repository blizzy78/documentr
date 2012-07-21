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

import static junit.framework.Assert.*;

import org.junit.Before;
import org.junit.Test;

public class SearchTextSuggestionTest {
	private static final String SEARCH_TEXT = "suggestion"; //$NON-NLS-1$
	private static final String SEARCH_TEXT_HTML = "html"; //$NON-NLS-1$
	private static final int TOTAL_HITS = 123;
	
	private SearchTextSuggestion suggestion;
	
	@Before
	public void setUp() {
		suggestion = new SearchTextSuggestion(SEARCH_TEXT, SEARCH_TEXT_HTML, TOTAL_HITS);
	}
	
	@Test
	public void getSearchText() {
		assertEquals(SEARCH_TEXT, suggestion.getSearchText());
	}
	
	@Test
	public void getSearchTextHtml() {
		assertEquals(SEARCH_TEXT_HTML, suggestion.getSearchTextHtml());
	}
	
	@Test
	public void getTotalHits() {
		assertEquals(TOTAL_HITS, suggestion.getTotalHits());
	}
}
