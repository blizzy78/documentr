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

define(['module'], function(module) {
	"use strict";
	
	var defaultModuleOptions = {
		pageSplitStartText: 'Page Split Start',
		pageSplitEndText: 'Page Split End'
	};
	var effectiveModuleOptions = $.extend({}, defaultModuleOptions, module.config());

	var internal = {};
	
	function updatePageSplitRange() {
		var startEl = internal.pageSplitStart;
		var endEl = internal.pageSplitEnd;
		var inside = false;
		if (!documentr.isSomething(startEl) && !documentr.isSomething(endEl)) {
			inside = true;
		}
		internal.pageTextEl.find('> *[data-text-range]').each(function() {
			var textEl = $(this);
			var float = textEl.css('float');
			if ((float !== 'left') && (float !== 'right')) {
				var el = textEl[0];
				if (documentr.isSomething(startEl) && (el === startEl)) {
					inside = true;
				}
			
				if (inside) {
					textEl
						.removeClass('page-split-outside')
						.children()
							.removeClass('page-split-outside');
				} else {
					textEl
						.addClass('page-split-outside')
						.children()
							.addClass('page-split-outside');
				}
	
				if (documentr.isSomething(endEl) && (el === endEl)) {
					inside = false;
				}
			}
		});
	
		var mode = internal.pageSplitMode;
		var textEl = (mode === 'start') ? startEl : endEl;
		if (documentr.isSomething(textEl)) {
			var wasHidden = internal.splitCursorEl.css('display') === 'none';
		
			textEl = $(textEl);
			internal.splitCursorEl.css({
					left: textEl.offset().left,
					top: (mode === 'start') ?
							(textEl.offset().top - internal.splitCursorEl.outerHeight() - 3) :
							(textEl.offset().top + textEl.outerHeight() + 3),
					width: textEl.outerWidth()
				})
				.show();
	
			if (mode === 'start') {
				internal.splitCursorEl.find('.above').show();
				internal.splitCursorEl.find('.below').hide();
				internal.splitCursorStartEl.hide();
				internal.splitBGEl.hide();
			} else {
				internal.splitCursorEl.find('.above').hide();
				internal.splitCursorEl.find('.below').show();
				internal.splitCursorStartEl.show();
				internal.splitBGEl.css({
					left: internal.splitCursorStartEl.offset().left,
					top: internal.splitCursorStartEl.offset().top + internal.splitCursorStartEl.outerHeight(),
					// minus 2 to account for border
					width: internal.splitCursorStartEl.outerWidth() - 2,
					height: internal.splitCursorEl.offset().top - internal.splitCursorStartEl.offset().top - internal.splitCursorStartEl.outerHeight()
				}).show();
			}
			
			// force another refresh if cursor was hidden before to set cursor top position correctly
			if (wasHidden) {
				window.setTimeout(updatePageSplitRange, 1);
			}
		} else {
			internal.splitCursorEl.hide();
			internal.splitCursorStartEl.hide();
			internal.splitBGEl.hide();
		}
	}

	function toggleHideFloatingElements(hide) {
		internal.pageTextEl.children().each(function() {
			var el = $(this);
			var float = el.css('float');
			if ((float === 'left') || (float === 'right')) {
				if (hide) {
					el.hide();
				} else {
					el.show();
				}
			}
		});
	}

	function createSplitCursor() {
		if (!documentr.isSomething(internal.splitCursorEl)) {
			var splitCursorEl = $.parseHTML('<div class="page-split-cursor" style="display: none;">' +
				'<div class="above"><i class="icon-chevron-down"></i> ' + effectiveModuleOptions.pageSplitStartText + '</div>' +
				'<div class="line"></div>' +
				'<div class="below"><i class="icon-chevron-up"></i> ' + effectiveModuleOptions.pageSplitEndText + '</div>' +
				'</div>');
			var splitCursorStartEl = $.parseHTML('<div class="page-split-cursor" style="display: none;">' +
				'<div class="above"><i class="icon-chevron-down"></i> ' + effectiveModuleOptions.pageSplitStartText + '</div>' +
				'<div class="line"></div>' +
				'</div>');
			var splitBGEl = $.parseHTML('<div id="splitBG" class="page-split-background" style="display: none;"></div>');
			internal.splitCursorEl = $(splitCursorEl);
			internal.splitCursorStartEl = $(splitCursorStartEl);
			internal.splitBGEl = $(splitBGEl);
			$(document.body).append([internal.splitCursorEl, internal.splitCursorStartEl, internal.splitBGEl]);
		}
	}
	
	function hookupSplitCursor() {
		internal.pageTextEl.find('> *[data-text-range]')
			.mouseenter(function() {
				var mode = internal.pageSplitMode;
				if ((mode === 'start') || (mode === 'end')) {
					var textEl = $(this);
					var float = textEl.css('float');
					if ((float !== 'left') && (float !== 'right')) {
						var ok = true;
						if (mode === 'end') {
							var els = internal.pageTextEl.find('> *[data-text-range]');
							var startEl = internal.pageSplitStart;
							var startIdx = els.index(startEl);
							var endIdx = els.index(textEl[0]);
							if (endIdx < startIdx) {
								ok = false;
							}
						}

						if (ok) {
							if (mode === 'start') {
								internal.pageSplitStart = textEl[0];
							} else {
								internal.pageSplitEnd = textEl[0];
							}
							updatePageSplitRange();
						}
					}
				}
			})
			.click(function() {
				var mode = internal.pageSplitMode;
				if (mode === 'start') {
					internal.pageSplitEnd = internal.pageSplitStart;
					internal.pageSplitMode = 'end';
					internal.splitCursorStartEl.css({
						left: internal.splitCursorEl.offset().left,
						top: internal.splitCursorEl.offset().top,
						width: internal.splitCursorEl.outerWidth()
					});
					updatePageSplitRange();
				} else if (mode === 'end') {
					var startEl = $(internal.pageSplitStart);
					var endEl = $(internal.pageSplitEnd);
					var start = startEl.attr('data-text-range').replace(/,.*/, '');
					var end = endEl.attr('data-text-range').replace(/.*,/, '');
					window.location.href = effectiveModuleOptions.pageSplitUrl
						.replace(/_PROJECTNAME_/, internal.projectName)
						.replace(/_BRANCHNAME_/, internal.branchName)
						.replace(/_PAGEPATH_/, internal.pagePath)
						.replace(/_START_/, start)
						.replace(/_END_/, end);
				}
			});
	}

	function startSplit() {
		toggleHideFloatingElements(true);
		internal.pageSplitMode = 'start';
		internal.pageSplitStart = null;
		internal.pageSplitEnd = null;
		updatePageSplitRange();
	}
	
	return {
		start: function(pageTextEl, projectName, branchName, pagePath) {
			internal.pageTextEl = pageTextEl;
			internal.projectName = projectName;
			internal.branchName = branchName;
			internal.pagePath = pagePath;

			createSplitCursor();
			hookupSplitCursor();
			startSplit();
		}
	};
});
