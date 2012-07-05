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

import org.apache.lucene.index.IndexWriter;

import de.blizzy.documentr.TestUtil;

final class TestIndexWriterUtil {
	private TestIndexWriterUtil() {}
	
	static boolean isClosed(IndexWriter writer) {
		Boolean result = (Boolean) TestUtil.invokeMethod(IndexWriter.class, writer, "isClosed", null, null); //$NON-NLS-1$
		return result.booleanValue();
	}
}
