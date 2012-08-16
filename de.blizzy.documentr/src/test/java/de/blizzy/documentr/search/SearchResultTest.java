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

import java.util.Collections;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import com.google.common.collect.Lists;

public class SearchResultTest {
	@SuppressWarnings("nls")
	private static final List<SearchHit> HITS = Lists.newArrayList(
			new SearchHit("project", "branch", "foo/bar", "title", "textHtml", Collections.<String>emptyList()),
			new SearchHit("project", "branch", "foo/bar/baz", "title2", "textHtml2", Collections.<String>emptyList()));
	private static final int TOTAL_HITS = HITS.size();
	private static final int HITS_PER_PAGE = 30;
	
	private SearchResult searchResult;

	@Before
	public void setUp() {
		searchResult = new SearchResult(HITS, TOTAL_HITS, HITS_PER_PAGE);
	}
	
	@Test
	public void getHits() {
		assertEquals(HITS, searchResult.getHits());
	}
	
	@Test
	public void getTotalHits() {
		assertEquals(TOTAL_HITS, searchResult.getTotalHits());
	}
	
	@Test
	public void getHitsPerPage() {
		assertEquals(HITS_PER_PAGE, searchResult.getHitsPerPage());
	}
}
