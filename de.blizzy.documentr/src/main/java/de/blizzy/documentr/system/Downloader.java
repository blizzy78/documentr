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

import java.io.IOException;
import java.net.URI;
import java.nio.charset.Charset;
import java.util.concurrent.TimeUnit;

import org.apache.commons.io.IOUtils;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatus.Series;
import org.springframework.http.client.ClientHttpRequest;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.http.client.SimpleClientHttpRequestFactory;

class Downloader {
	private static final long TIMEOUT = TimeUnit.MILLISECONDS.convert(10, TimeUnit.SECONDS);

	String getTextFromUrl(String url, Charset encoding) throws IOException {
		ClientHttpResponse response = null;
		try {
			SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
			requestFactory.setConnectTimeout((int) TIMEOUT);
			requestFactory.setReadTimeout((int) TIMEOUT);
			ClientHttpRequest request = requestFactory.createRequest(URI.create(url), HttpMethod.GET);
			response = request.execute();
			HttpStatus status = response.getStatusCode();
			if (status.series() == Series.SUCCESSFUL) {
				return IOUtils.toString(response.getBody(), encoding);
			}
		} finally {
			if (response != null) {
				response.close();
			}
		}
		return null;
	}
}
