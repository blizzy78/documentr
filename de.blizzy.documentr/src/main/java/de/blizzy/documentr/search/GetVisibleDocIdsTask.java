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

import java.io.IOException;
import java.util.BitSet;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.util.Bits;
import org.apache.lucene.util.DocIdBitSet;
import org.springframework.security.core.Authentication;

import com.google.common.util.concurrent.ListeningExecutorService;

import de.blizzy.documentr.DocumentrConstants;
import de.blizzy.documentr.access.DocumentrPermissionEvaluator;
import de.blizzy.documentr.access.Permission;
import de.blizzy.documentr.access.UserStore;
import de.blizzy.documentr.util.Util;

class GetVisibleDocIdsTask implements Callable<Bits> {
	private IndexSearcher searcher;
	private Authentication authentication;
	private UserStore userStore;
	private DocumentrPermissionEvaluator permissionEvaluator;
	private ListeningExecutorService taskExecutor;

	GetVisibleDocIdsTask(IndexSearcher searcher, Authentication authentication, UserStore userStore,
			DocumentrPermissionEvaluator permissionEvaluator, ListeningExecutorService taskExecutor) {

		this.searcher = searcher;
		this.authentication = authentication;
		this.userStore = userStore;
		this.permissionEvaluator = permissionEvaluator;
		this.taskExecutor = taskExecutor;
	}

	@Override
	public Bits call() throws IOException, TimeoutException {
		Future<BitSet> branchPagesFuture = taskExecutor.submit(new GetVisibleBranchDocIdsTask(
				searcher, authentication, permissionEvaluator));
		Future<BitSet> inaccessibleDocsFuture = taskExecutor.submit(new GetInaccessibleDocIdsTask(
				searcher, Permission.VIEW, authentication, userStore, permissionEvaluator));
		try {
			BitSet docIds = branchPagesFuture.get(DocumentrConstants.INTERACTIVE_TIMEOUT, TimeUnit.SECONDS);
			docIds.andNot(inaccessibleDocsFuture.get(DocumentrConstants.INTERACTIVE_TIMEOUT, TimeUnit.SECONDS));
			return new DocIdBitSet(docIds);
		} catch (InterruptedException e) {
			throw new RuntimeException(e);
		} catch (ExecutionException e) {
			Throwable cause = e.getCause();
			if (cause instanceof IOException) {
				throw (IOException) cause;
			} else {
				throw Util.toRuntimeException(cause);
			}
		} finally {
			branchPagesFuture.cancel(false);
			inaccessibleDocsFuture.cancel(false);
		}
	}
}
