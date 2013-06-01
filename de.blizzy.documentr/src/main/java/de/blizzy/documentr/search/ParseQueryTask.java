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

import java.util.concurrent.Callable;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.Query;
import org.apache.lucene.util.Version;

class ParseQueryTask implements Callable<Query> {
	private String searchText;
	private Analyzer analyzer;

	ParseQueryTask(String searchText, Analyzer analyzer) {
		this.searchText = searchText;
		this.analyzer = analyzer;
	}

	@Override
	public Query call() throws ParseException {
		QueryParser parser = new QueryParser(Version.LUCENE_43, PageIndex.ALL_TEXT, analyzer);
		parser.setAllowLeadingWildcard(true);
		return parser.parse(searchText);
	}
}
