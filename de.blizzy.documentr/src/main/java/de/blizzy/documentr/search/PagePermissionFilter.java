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
package de.blizzy.documentr.search;

import java.util.BitSet;

import org.apache.lucene.index.AtomicReaderContext;
import org.apache.lucene.search.DocIdSet;
import org.apache.lucene.search.Filter;
import org.apache.lucene.util.Bits;
import org.apache.lucene.util.DocIdBitSet;

class PagePermissionFilter extends Filter {
	private BitSet visibleDocIds;
	private int visibleDocIdsLength;

	PagePermissionFilter(Bits visibleDocIds) {
		this.visibleDocIds = toBitSet(visibleDocIds);
		this.visibleDocIdsLength = this.visibleDocIds.size();
	}

	@Override
	public DocIdSet getDocIdSet(AtomicReaderContext context, Bits acceptDocs) {
		int docBase = context.docBase;
		BitSet result = new BitSet();
		int acceptDocsLen = (acceptDocs != null) ? acceptDocs.length() : -1;
		for (int i = docBase; i < visibleDocIdsLength; i++) {
			int resultIdx = i - docBase;
			if ((acceptDocsLen >= 0) && (resultIdx >= acceptDocsLen)) {
				break;
			}
			if (visibleDocIds.get(i) &&
				((acceptDocs == null) || acceptDocs.get(resultIdx))) {

				result.set(resultIdx);
			}
		}
		return new DocIdBitSet(result);
	}

	private BitSet toBitSet(Bits bits) {
		int len = bits.length();
		BitSet result = new BitSet(len + 1);
		for (int i = 0; i < len; i++) {
			if (bits.get(i)) {
				result.set(i);
			}
		}
		return result;
	}
}
