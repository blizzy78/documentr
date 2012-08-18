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

import java.util.concurrent.Callable;

import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.util.Bits;
import org.springframework.security.core.Authentication;

class GetVisibleDocIdsTask implements Callable<Bits> {
	private Authentication authentication;
	private PageIndex pageIndex;
	private IndexSearcher searcher;

	GetVisibleDocIdsTask(IndexSearcher searcher, Authentication authentication, PageIndex pageIndex) {
		this.searcher = searcher;
		this.authentication = authentication;
		this.pageIndex = pageIndex;
	}
	
	@Override
	public Bits call() throws Exception {
		return pageIndex.getVisibleDocIds(searcher, authentication);
	}
}
