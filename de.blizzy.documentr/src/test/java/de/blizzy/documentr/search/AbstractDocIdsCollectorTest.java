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

import static org.junit.Assert.*;

import java.util.BitSet;

import org.apache.lucene.index.AtomicReaderContext;
import org.apache.lucene.search.Scorer;
import org.junit.Test;

public class AbstractDocIdsCollectorTest {
	private static final class TestCollector extends AbstractDocIdsCollector {
		@Override
		public void setScorer(Scorer scorer) {
		}

		@Override
		public void collect(int doc) {
			getDocIds().set(doc);
		}

		@Override
		public void setNextReader(AtomicReaderContext context) {
		}

		@Override
		public boolean acceptsDocsOutOfOrder() {
			return true;
		}
	}

	@Test
	public void collect() {
		TestCollector collector = new TestCollector();
		collector.collect(1);
		collector.collect(3);
		BitSet docIds = collector.getDocIds();
		assertEquals(2, docIds.cardinality());
		assertTrue(docIds.get(1));
		assertTrue(docIds.get(3));
	}
}
