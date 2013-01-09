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
package de.blizzy.documentr.system;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.client.ClientHttpRequest;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.http.client.SimpleClientHttpRequestFactory;

import com.google.common.base.Charsets;

import de.blizzy.documentr.AbstractDocumentrTest;

@RunWith(PowerMockRunner.class)
@PrepareForTest(Downloader.class)
public class DownloaderTest extends AbstractDocumentrTest {
	private static final String URL = "http://www.example.com/foo.txt"; //$NON-NLS-1$
	private static final String TEXT = "text"; //$NON-NLS-1$

	@Mock
	private SimpleClientHttpRequestFactory requestFactory;
	@Mock
	private ClientHttpRequest request;
	@Mock
	private ClientHttpResponse response;
	private Downloader downloader;

	@Before
	public void setUp() throws Exception {
		downloader = new Downloader();

		PowerMockito.whenNew(SimpleClientHttpRequestFactory.class).withNoArguments().thenReturn(requestFactory);
	}

	@Test
	public void getTextFromUrl() throws IOException {
		when(requestFactory.createRequest(URI.create(URL), HttpMethod.GET)).thenReturn(request);
		when(request.execute()).thenReturn(response);
		when(response.getStatusCode()).thenReturn(HttpStatus.OK);
		when(response.getBody()).then(new Answer<InputStream>() {
			@Override
			public InputStream answer(InvocationOnMock invocation) {
				byte[] data = TEXT.getBytes(Charsets.UTF_8);
				return new ByteArrayInputStream(data);
			}
		});

		String result = downloader.getTextFromUrl(URL, Charsets.UTF_8);
		assertEquals(TEXT, result);

		ArgumentCaptor<Integer> connectTimeoutCaptor = ArgumentCaptor.forClass(Integer.class);
		verify(requestFactory).setConnectTimeout(connectTimeoutCaptor.capture());
		assertTrue(connectTimeoutCaptor.getValue() > 0);

		ArgumentCaptor<Integer> readTimeoutCaptor = ArgumentCaptor.forClass(Integer.class);
		verify(requestFactory).setReadTimeout(readTimeoutCaptor.capture());
		assertTrue(readTimeoutCaptor.getValue() > 0);
	}
}
