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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.OutputStream;
import java.io.StringReader;
import java.nio.charset.Charset;

import org.apache.commons.lang3.StringUtils;

import com.google.common.io.Closeables;

class TrimWriter {
	void write(String text, OutputStream out, Charset charset) throws IOException {
		BufferedReader in = null;
		try {
			in = new BufferedReader(new StringReader(text));
			String line;
			boolean textarea = false;
			boolean pre = false;
			boolean notrim = false;
			while ((line = in.readLine()) != null) {
				String origLine = line;

				line = StringUtils.replace(line, "__NOTRIM__", StringUtils.EMPTY); //$NON-NLS-1$
				line = StringUtils.replace(line, "__/NOTRIM__", StringUtils.EMPTY); //$NON-NLS-1$
				
				if (origLine.contains("<textarea")) { //$NON-NLS-1$
					textarea = true;
				}
				if (origLine.contains("<pre")) { //$NON-NLS-1$
					pre = true;
				}
				if (origLine.contains("__NOTRIM__")) { //$NON-NLS-1$
					notrim = true;
				}

				if (textarea || pre || notrim) {
					writeln(line, out, charset);
				} else {
					line = line.trim();
					if (StringUtils.isNotBlank(line)) {
						writeln(line, out, charset);
					}
				}

				if (origLine.contains("</textarea")) { //$NON-NLS-1$
					textarea = false;
				}
				if (origLine.contains("</pre")) { //$NON-NLS-1$
					pre = false;
				}
				if (origLine.contains("__/NOTRIM__")) { //$NON-NLS-1$
					notrim = false;
				}
			}
		} finally {
			Closeables.closeQuietly(in);
		}
	}

	private void writeln(String line, OutputStream out, Charset charset) throws IOException {
		byte[] lineData = line.getBytes(charset);
		out.write(lineData);
		out.write('\n');
	}
}
