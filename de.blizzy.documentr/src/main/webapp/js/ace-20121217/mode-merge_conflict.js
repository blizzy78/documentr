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

ace.define('ace/mode/merge_conflict', function(require, exports, module) {
	"use strict";
	
	var oop = require('../lib/oop');

	var MergeConflictHighlightRules = function() {
		this.$rules = {
			start: [
				{
					token: 'merge_conflict',
					regex: /^(?:(?:<<<<<<<|>>>>>>>) .*|=======$)/
				}
			]
		};
	};
	var TextHighlightRules = require('./text_highlight_rules').TextHighlightRules;
	oop.inherits(MergeConflictHighlightRules, TextHighlightRules);

	exports.Mode = function() {
		var Tokenizer = require('../tokenizer').Tokenizer;
		this.$tokenizer = new Tokenizer(new MergeConflictHighlightRules().getRules());
	};
	var TextMode = require('./text').Mode;
	oop.inherits(exports.Mode, TextMode);
});
