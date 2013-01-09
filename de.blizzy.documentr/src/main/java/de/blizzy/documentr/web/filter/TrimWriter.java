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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.OutputStream;
import java.io.StringReader;
import java.nio.charset.Charset;

import org.apache.commons.lang3.StringUtils;

import com.google.common.io.Closeables;

import de.blizzy.documentr.util.Replacement;

class TrimWriter {
	private static final Replacement REMOVE_COMMENT = new Replacement("<!--.*?-->", StringUtils.EMPTY); //$NON-NLS-1$
	private static final Replacement TRIM_LEFT = new Replacement("^[ \t]+", StringUtils.EMPTY); //$NON-NLS-1$
	private static final Replacement TRIM_RIGHT = new Replacement("[ \t]+$", StringUtils.EMPTY); //$NON-NLS-1$

	void write(String text, OutputStream out, Charset charset) throws IOException {
		BufferedReader in = null;
		try {
			in = new BufferedReader(new StringReader(text));
			String line;
			boolean textareaTag = false;
			boolean preTag = false;
			boolean notrimTag = false;
			boolean trim = true;
			while ((line = in.readLine()) != null) {
				String origLine = line;

				line = StringUtils.replace(line, "__NOTRIM__", StringUtils.EMPTY); //$NON-NLS-1$
				line = StringUtils.replace(line, "__/NOTRIM__", StringUtils.EMPTY); //$NON-NLS-1$
				line = REMOVE_COMMENT.replaceAll(line);

				if (trim) {
					line = TRIM_LEFT.replaceAll(line);
				}

				if (origLine.contains("<textarea")) { //$NON-NLS-1$
					textareaTag = true;
				}
				if (origLine.contains("<pre")) { //$NON-NLS-1$
					preTag = true;
				}
				if (origLine.contains("__NOTRIM__")) { //$NON-NLS-1$
					notrimTag = true;
				}
				if (textareaTag || preTag || notrimTag) {
					trim = false;
				}

				if (origLine.contains("</textarea")) { //$NON-NLS-1$
					textareaTag = false;
				}
				if (origLine.contains("</pre")) { //$NON-NLS-1$
					preTag = false;
				}
				if (origLine.contains("__/NOTRIM__")) { //$NON-NLS-1$
					notrimTag = false;
				}
				if (!textareaTag && !preTag && !notrimTag) {
					trim = true;
				}

				if (trim) {
					line = TRIM_RIGHT.replaceAll(line);
				}

				boolean doWrite = !trim || StringUtils.isNotBlank(line);
				if (doWrite) {
					writeln(line, out, charset);
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
