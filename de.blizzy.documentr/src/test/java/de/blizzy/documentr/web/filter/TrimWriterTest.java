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
package de.blizzy.documentr.web.filter;

import static org.junit.Assert.*;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import org.junit.Test;

import com.google.common.base.Charsets;
import com.google.common.io.Closeables;

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
					"  <pre> single </pre>  \r\n" +
					"  bar  \r\n" +
					"<pre class=\"xyz\">  a b c  \r\n" +
					"  d e  \r\n" +
					"  f g  </pre>\r\n" +
					"  \t  \r\n" +
					"<textarea class=\"test\">  a b c  \r\n" +
					"  d e  \r\n" +
					"  f g  </textarea>\r\n" +
					"  zz<!-- hello world-->z \t  \r\n" +
					"\t<div class=\"code-view-wrapper\"><!--__NOTRIM__--><div class=\"code-view\">  a \r\n" +
					"  b  \r\n" +
					"  c   </div><!--__/NOTRIM__--></div>\t\r\n";
			writer.write(text, out, Charsets.UTF_8);
		} finally {
			Closeables.closeQuietly(out);
		}

		String result = new String(out.toByteArray(), Charsets.UTF_8);
		@SuppressWarnings("nls")
		String expected =
				"xyz\n" +
				"foo\n" +
				"<pre> single </pre>\n" +
				"bar\n" +
				"<pre class=\"xyz\">  a b c  \n" +
				"  d e  \n" +
				"  f g  </pre>\n" +
				"<textarea class=\"test\">  a b c  \n" +
				"  d e  \n" +
				"  f g  </textarea>\n" +
				"zzz\n" +
				"<div class=\"code-view-wrapper\"><div class=\"code-view\">  a \n" +
				"  b  \n" +
				"  c   </div></div>\n";
		assertEquals(expected, result);
	}
}
