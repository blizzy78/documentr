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

import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

public class FirstParameter<T> implements Answer<T> {
	@Override
	public T answer(InvocationOnMock invocation) {
		@SuppressWarnings("unchecked")
		T result = (T) invocation.getArguments()[0];
		return result;
	}
}
