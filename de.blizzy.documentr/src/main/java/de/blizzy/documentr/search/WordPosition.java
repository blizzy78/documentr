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

class WordPosition {
	private String word;
	private int start;
	private int end;

	WordPosition(String word, int start, int end) {
		this.word = word;
		this.start = start;
		this.end = end;
	}
	
	String getWord() {
		return word;
	}
	
	int getStart() {
		return start;
	}
	
	int getEnd() {
		return end;
	}
}
