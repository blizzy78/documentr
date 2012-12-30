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

define(['diff_match_patch'], function(diff_match_patch) {
	"use strict";
	
	return {
		diff: function(markdown1, markdown2) {
			var dmp = new diff_match_patch();
			var diffs = dmp.diff_main(markdown1.replace(/\r/g, ''), markdown2.replace(/\r/g, ''));
			dmp.diff_cleanupSemantic(diffs);
			
			var line = 0;
			var col = 0;
			var text = '';
			var markers = [];
			for (var i = 0; i < diffs.length; i++) {
				var op = diffs[i][0];
				var diffText = diffs[i][1];
				var marker = {
					startLine: line,
					startColumn: col
				};
				text += diffText;

				var lines = diffText.split('\n');
				for (var currLine = 0; currLine < lines.length; currLine++) {
					if (currLine > 0) {
						line++;
						col = 0;
					}
					col += lines[currLine].length;
				}

				if (op === DIFF_EQUAL) {
					continue;
				}

				marker.endLine = line;
				marker.endColumn = col;
				marker.insert = (op === DIFF_INSERT);
				marker.delete = (op === DIFF_DELETE);
				markers.push(marker);
			}

			return {
				text: text,
				markers: markers
			};
		}
	};
});
