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

define(['require'], function(require) {
	"use strict";
	
	$.fn.extend({
		setupCodeView: function() {
			var el = this[0];
			var that = this;
			require(['ace'], function(ace) {
				var editor = ace.edit(el);
				editor.setTheme('ace/theme/chrome');
				var type = that.attr('data-type');
				if (documentr.isSomething(type)) {
					editor.session.setMode('ace/mode/' + type);
				}
				editor.setReadOnly(true);
				editor.setDisplayIndentGuides(true);
				editor.renderer.setShowGutter(true);
				editor.session.setUseWrapMode(false);
				editor.renderer.setShowPrintMargin(false);
				editor.session.setUseSoftTabs(false);
				editor.setHighlightSelectedWord(false);
				editor.setHighlightActiveLine(false);
				editor.renderer.hideCursor();
				var lines = editor.session.getDocument().getLength();
				lines = Math.min(lines, 20);
				that.parent().css('height', ((lines * 20) + 2) + 'px');
				editor.resize();
			});
			return this;
		}
	});
});
