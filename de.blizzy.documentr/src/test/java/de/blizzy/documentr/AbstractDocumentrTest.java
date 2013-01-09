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
package de.blizzy.documentr;

import java.util.Set;

import org.junit.After;
import org.junit.Before;
import org.mockito.MockitoAnnotations;

import com.google.common.collect.Sets;
import com.google.common.io.Closeables;

import de.blizzy.documentr.repository.ILockedRepository;

public abstract class AbstractDocumentrTest {
	private Set<ILockedRepository> repositories = Sets.newHashSet();

	@Before
	public void initMocks() {
		MockitoAnnotations.initMocks(this);
	}

	@After
	public void closeRepositories() {
		for (ILockedRepository repo : repositories) {
			Closeables.closeQuietly(repo);
		}
		repositories.clear();
	}

	protected void register(ILockedRepository repository) {
		if (repository != null) {
			repositories.add(repository);
		}
	}
}
