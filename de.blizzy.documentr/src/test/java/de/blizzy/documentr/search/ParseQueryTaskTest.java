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

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.util.Version;
import org.junit.Before;
import org.junit.Test;

public class ParseQueryTaskTest {
	private StandardAnalyzer analyzer;

	@Before
	public void setUp() {
		analyzer = new StandardAnalyzer(Version.LUCENE_40);
	}

	@Test
	public void call() throws ParseException {
		ParseQueryTask task = new ParseQueryTask("+foo -bar", analyzer); //$NON-NLS-1$
		Query result = task.call();

		BooleanQuery query = new BooleanQuery();
		query.add(new TermQuery(new Term(PageIndex.ALL_TEXT, "foo")), BooleanClause.Occur.MUST); //$NON-NLS-1$
		query.add(new TermQuery(new Term(PageIndex.ALL_TEXT, "bar")), BooleanClause.Occur.MUST_NOT); //$NON-NLS-1$
		assertEquals(query, result);
	}
}
