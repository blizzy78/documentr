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
package de.blizzy.documentr.markdown.macro.impl;

import static de.blizzy.documentr.TestUtil.*;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringEscapeUtils;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.util.MultiValueMap;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;

import com.google.common.collect.Sets;

import de.blizzy.documentr.AbstractDocumentrTest;
import de.blizzy.documentr.markdown.HtmlSerializerContext;
import de.blizzy.documentr.markdown.macro.IMacroContext;
import de.blizzy.documentr.markdown.macro.IMacroSettings;
import de.blizzy.documentr.page.IPageStore;
import de.blizzy.documentr.page.Page;

public class FlattrMacroTest extends AbstractDocumentrTest {
	private static final String FLATTR_USER_ID = "flattrUserId"; //$NON-NLS-1$
	private static final String PAGE_TITLE = "pageTitle"; //$NON-NLS-1$
	private static final String PROJECT = "project"; //$NON-NLS-1$
	private static final String BRANCH = "branch"; //$NON-NLS-1$
	private static final String PAGE = "page"; //$NON-NLS-1$
	private static final String TAG_1 = "tag1"; //$NON-NLS-1$
	private static final String TAG_2 = "tag2"; //$NON-NLS-1$
	private static final String PAGE_URI = "pageUri"; //$NON-NLS-1$
	private static final String PAGE_URL = "pageUrl"; //$NON-NLS-1$

	@Mock
	private IMacroContext macroContext;
	@Mock
	private HtmlSerializerContext htmlSerializerContext;
	@Mock
	private IPageStore pageStore;
	@Mock
	private IMacroSettings macroSettings;
	@InjectMocks
	private FlattrMacro macro;

	@Before
	public void setUp() throws IOException {
		when(htmlSerializerContext.getPageStore()).thenReturn(pageStore);
		when(macroContext.getHtmlSerializerContext()).thenReturn(htmlSerializerContext);
		when(macroContext.getSettings()).thenReturn(macroSettings);
		when(macroSettings.getSetting("userId")).thenReturn(FLATTR_USER_ID); //$NON-NLS-1$

		when(htmlSerializerContext.getProjectName()).thenReturn(PROJECT);
		when(htmlSerializerContext.getBranchName()).thenReturn(BRANCH);
		when(htmlSerializerContext.getPagePath()).thenReturn(PAGE);
		when(htmlSerializerContext.getPageUri(PAGE)).thenReturn(PAGE_URI);
		when(htmlSerializerContext.getUrl(PAGE_URI)).thenReturn(PAGE_URL);

		Page page = Page.fromText(PAGE_TITLE, "text"); //$NON-NLS-1$
		page.setTags(Sets.newHashSet(TAG_1, TAG_2));
		when(pageStore.getPage(PROJECT, BRANCH, PAGE, false)).thenReturn(page);
	}

	@Test
	public void getHtml() {
		String html = macro.getHtml(macroContext);
		@SuppressWarnings("nls")
		String re = "^<a href=\"([^\"]+)\">" +
				"<img src=\"https://api\\.flattr\\.com/button/flattr-badge-large\\.png\"/>" +
				"</a>$"; //$NON-NLS-1$
		assertRE(re, html);

		Matcher matcher = Pattern.compile(re, Pattern.DOTALL).matcher(html);
		matcher.find();
		String url = StringEscapeUtils.unescapeHtml4(matcher.group(1));
		UriComponents components = UriComponentsBuilder.fromHttpUrl(url).build();
		assertEquals("https", components.getScheme()); //$NON-NLS-1$
		assertEquals("flattr.com", components.getHost()); //$NON-NLS-1$
		assertEquals(-1, components.getPort());
		assertEquals("/submit/auto", components.getPath()); //$NON-NLS-1$
		MultiValueMap<String, String> params = components.getQueryParams();
		assertEquals(FLATTR_USER_ID, params.getFirst("user_id")); //$NON-NLS-1$
		assertEquals(PAGE_URL, params.getFirst("url")); //$NON-NLS-1$
		assertEquals(PAGE_TITLE, params.getFirst("title")); //$NON-NLS-1$
		assertEquals("text", params.getFirst("category")); //$NON-NLS-1$ //$NON-NLS-2$
		assertTrue(params.getFirst("tags").equals(TAG_1 + "," + TAG_2) || //$NON-NLS-1$ //$NON-NLS-2$
				params.getFirst("tags").equals(TAG_2 + "," + TAG_1)); //$NON-NLS-1$ //$NON-NLS-2$
	}
}
