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

var documentr = {};

(function() {
	"use strict";

	documentr.isSomething = function(x) {
		return (typeof(x) !== 'undefined') && (x !== null);
	};

	documentr.setupCodeViews = function() {
		$('.code-view-wrapper .code-view').each(function() {
			var that = $(this);
			if (!that.hasClass('ace_editor')) {
				require(['documentr/codeView'], function() {
					that.setupCodeView();
				});
			}
		});
	};
	
	documentr.setupLightbox = function() {
		var images = $('img[data-lightbox="lightbox"]');
		if (images.length > 0) {
			require(['slimbox'], function() {
				images.slimbox(null, function(el) {
					return [el.parentElement.href, $(el).attr('data-title')];
				}, null);
			});
		}
	};
	
	documentr.waitFor = function(checkCallback, callback, interval) {
		if (!documentr.isSomething(interval)) {
			interval = 100;
		}
		
		var waitFunc = null;
		waitFunc = function() {
			if (checkCallback()) {
				callback();
			} else {
				window.setTimeout(waitFunc, interval);
			}
		};
		waitFunc();
	};


	$.fn.extend({
		setPreventClick: function(preventClick) {
			var preventClickHandler = this.data('preventClickHandler');
			if (!documentr.isSomething(preventClickHandler) && preventClick) {
				preventClickHandler = function(event) {
					event.preventDefault();
				};
				this.data('preventClickHandler', preventClickHandler);
				this.click(preventClickHandler);
			} else if (documentr.isSomething(preventClickHandler) && !preventClick) {
				this.off('click', preventClickHandler);
				this.data('preventClickHandler', null);
			}
			return this;
		},
		
		setButtonDisabled: function(disabled) {
			if (disabled) {
				this.addClass('disabled');
			} else {
				this.removeClass('disabled');
			}
			this.setPreventClick(disabled);
			return this;
		}
	});


	$.ajaxSetup({
		cache: false
	});

	$(function() {
		documentr.setupCodeViews();
		documentr.setupLightbox();

		$('body').tooltip({
			selector: '[rel="tooltip"]'
		});

		$('#siteSearch input').bind('webkitspeechchange', function() {
			$('#siteSearch')[0].submit();
		});
	});
})();
