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
package de.blizzy.documentr.web.filter;

import static org.junit.Assert.*;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import org.apache.commons.io.IOUtils;
import org.junit.Test;

public class TrimWriterTest {
	@Test
	public void write() throws IOException {
		ByteArrayOutputStream out = null;
		try {
			out = new ByteArrayOutputStream();
			TrimWriter writer = new TrimWriter();
			@SuppressWarnings("nls")
			String text =
					"xyz\r\n" +
					"\tfoo\r\n" +
					"  bar  \r\n" +
					"<textarea class=\"test\">  a b c  \r\n" +
					"  d e  \r\n" +
					"  f g  </textarea>\r\n" +
					"  zzz \t  \r\n";
			writer.write(text, out, "UTF-8"); //$NON-NLS-1$
		} finally {
			IOUtils.closeQuietly(out);
		}
		
		String result = new String(out.toByteArray(), "UTF-8"); //$NON-NLS-1$
		@SuppressWarnings("nls")
		String expected =
				"xyz\n" +
				"foo\n" +
				"bar\n" +
				"<textarea class=\"test\">  a b c  \n" +
				"  d e  \n" +
				"  f g  </textarea>\n" +
				"zzz\n";
		assertEquals(expected, result);
	}
}
