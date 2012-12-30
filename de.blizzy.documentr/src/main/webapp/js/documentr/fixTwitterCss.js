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

define(function() {
	"use strict";
	
	var twitterCssEl = null;
	var documentrCssEl = null;
	$('link[rel="stylesheet"]').each(function() {
		var el = $(this);
		var href = el.attr('href');
		if (documentr.isSomething(href)) {
			if (href.indexOf('.twimg.com') > 0) {
				twitterCssEl = el;
			} else if (href.indexOf('/documentr.css') > 0) {
				documentrCssEl = el;
			}
		}
	});
	if ((twitterCssEl !== null) && (documentrCssEl !== null)) {
		twitterCssEl.detach();
		documentrCssEl.before(twitterCssEl);
	}
	$('.twtr-widget').removeAttr('id');
});
