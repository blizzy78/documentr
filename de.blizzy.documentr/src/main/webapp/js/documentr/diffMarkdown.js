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

define(['diff_match_patch'], function(diff_match_patch) {
	"use strict";
	
	return {
		diff: function(markdown1, markdown2) {
			var dmp = new diff_match_patch();
			var diffs = dmp.diff_main(markdown1.replace(/\r/g, ''), markdown2.replace(/\r/g, ''));
			dmp.diff_cleanupSemantic(diffs);
			
			var html = [];
			for (var i = 0; i < diffs.length; i++) {
				var op = diffs[i][0];
				var text = diffs[i][1]
				.replace(/&/g, '&amp;')
				.replace(/</g, '&lt;')
				.replace(/>/g, '&gt;')
				.replace(/\n/g, '\n');
				switch (op) {
				case DIFF_INSERT:
					html[i] = '<ins>' + text + '</ins>';
					break;
				case DIFF_DELETE:
					html[i] = '<del>' + text + '</del>';
					break;
				case DIFF_EQUAL:
					html[i] = text;
					break;
				}
			}
			return '<pre class="changes"><code>' + html.join('') + '</code></pre>';
		}
	};
});
