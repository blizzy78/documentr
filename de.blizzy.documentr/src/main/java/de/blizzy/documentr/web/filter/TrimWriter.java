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

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;

class TrimWriter {
	void write(String text, OutputStream out, String encoding) throws IOException {
		BufferedReader in = null;
		try {
			in = new BufferedReader(new StringReader(text));
			String line;
			boolean textarea = false;
			boolean pre = false;
			while ((line = in.readLine()) != null) {
				if (line.contains("<textarea")) { //$NON-NLS-1$
					textarea = true;
				}
				if (line.contains("<pre")) { //$NON-NLS-1$
					pre = true;
				}

				if (textarea || pre) {
					writeln(line, out, encoding);
				} else {
					line = line.trim();
					if (StringUtils.isNotBlank(line)) {
						writeln(line, out, encoding);
					}
				}

				if (line.contains("</textarea")) { //$NON-NLS-1$
					textarea = false;
				}
				if (line.contains("</pre")) { //$NON-NLS-1$
					pre = false;
				}
			}
		} finally {
			IOUtils.closeQuietly(in);
		}
	}

	private void writeln(String line, OutputStream out, String encoding) throws IOException {
		byte[] lineData = line.getBytes(encoding);
		out.write(lineData);
		out.write('\n');
	}
}
