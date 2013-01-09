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
package de.blizzy.documentr.web.search;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

import org.apache.lucene.queryparser.classic.ParseException;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.security.core.Authentication;
import org.springframework.ui.Model;

import de.blizzy.documentr.AbstractDocumentrTest;
import de.blizzy.documentr.search.PageIndex;
import de.blizzy.documentr.search.SearchResult;

public class SearchControllerTest extends AbstractDocumentrTest {
	@InjectMocks
	private SearchController searchController;
	@Mock
	private PageIndex pageIndex;
	@Mock
	private Authentication authentication;
	@Mock
	private Model model;
	@Mock
	private SearchResult searchResult;

	@Test
	public void findPages() throws IOException, ParseException, TimeoutException {
		when(pageIndex.findPages("text", 1, authentication)).thenReturn(searchResult); //$NON-NLS-1$

		String view = searchController.findPages("text", 1, authentication, model); //$NON-NLS-1$
		assertEquals("/search/result", view); //$NON-NLS-1$

		verify(model).addAttribute("searchText", "text"); //$NON-NLS-1$ //$NON-NLS-2$
		verify(model).addAttribute("searchResult", searchResult); //$NON-NLS-1$
		verify(model).addAttribute("page", 1); //$NON-NLS-1$
	}
}
