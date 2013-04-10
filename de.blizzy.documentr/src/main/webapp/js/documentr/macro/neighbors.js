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
		// saveChildrenOrderUrl
		// resetChildrenOrderUrl
		// pageChildrenDataUrl
		// pageUrl
		saveText: 'Save',
		cancelText: 'Cancel',
		sortAlphabeticallyText: 'Sort Alphabetically'
	};
	var effectiveModuleOptions = $.extend({}, defaultModuleOptions, module.config());
	var expandHoverTimeout = null;

	function showBusy(buttonsEl) {
		buttonsEl.empty().append($.parseHTML('<i class="icon-time icon-white"></i>'));
	}
	
	function save(activeEl, buttonsEl, projectName, branchName, pagePath) {
		showBusy(buttonsEl);
		
		var paths = [];
		activeEl.find('> ul > li').each(function() {
			paths.push($(this).data('path'));
		});
		$.ajax({
			url: effectiveModuleOptions.saveChildrenOrderUrl
				.replace(/_PROJECTNAME_/, projectName)
				.replace(/_BRANCHNAME_/, branchName)
				.replace(/_PAGEPATH_/, pagePath),
			type: 'POST',
			dataType: 'json',
			data: {
				childrenOrder: paths
			},
			success: function(result) {
				window.location.reload();
			}
		});
	}
	
	function reset(buttonsEl, projectName, branchName, pagePath) {
		showBusy(buttonsEl);
		$.ajax({
			url: effectiveModuleOptions.resetChildrenOrderUrl
				.replace(/_PROJECTNAME_/, projectName)
				.replace(/_BRANCHNAME_/, branchName)
				.replace(/_PAGEPATH_/, pagePath),
			type: 'GET',
			dataType: 'json',
			success: function(result) {
				window.location.reload();
			}
		});
	}
	
	function hookExpandHoversInternal(projectName, branchName) {
		$('.neighbors li > a').off('mouseenter mouseleave');
		$('.neighbors li.active').addClass('children-loaded');
		
		$('.neighbors li').not('.children-loaded').each(function() {
			var liEl = $(this);
			if (documentr.isSomething(liEl.data('path'))) {
				liEl.find('> a')
					.on('mouseenter', function() {
						cancelExpandHoverTimeout();
						expandHoverTimeout = window.setTimeout(function() {
							expandItem(projectName, branchName, liEl);
						}, 1000);
					})
					.on('mouseleave', function() {
						cancelExpandHoverTimeout();
					});
			}
		});
	}
	
	function cancelExpandHoverTimeout() {
		if (documentr.isSomething(expandHoverTimeout)) {
			window.clearTimeout(expandHoverTimeout);
			expandHoverTimeout = null;
		}
	}
	
	function expandItem(projectName, branchName, liEl) {
		var pagePath = liEl.data('path').replace(/\//g, ',');
		$.ajax({
			url: effectiveModuleOptions.pageChildrenDataUrl
				.replace(/_PROJECTNAME_/, projectName)
				.replace(/_BRANCHNAME_/, branchName)
				.replace(/_PAGEPATH_/, pagePath),
			type: 'GET',
			dataType: 'json',
			success: function(result) {
				liEl.addClass('children-loaded');
				if (result.length > 0) {
					var newUlEl = $($.parseHTML('<ul class="nav nav-list"/>'));
					newUlEl.css('display', 'none');
					$.each(result, function(idx, node) {
						var newLiEl = $($.parseHTML('<li/>'));
						newLiEl.data('path', node.path);
						var newAEl = $($.parseHTML('<a/>'));
						newAEl
							.attr('href', effectiveModuleOptions.pageUrl
								.replace(/_PROJECTNAME_/, projectName)
								.replace(/_BRANCHNAME_/, branchName)
								.replace(/_PAGEPATH_/, node.path.replace(/\//g, ',')))
							.text(node.title);
						newLiEl.append(newAEl);
						newUlEl.append(newLiEl);
					});
					liEl.append(newUlEl);
					newUlEl.slideDown();
					hookExpandHoversInternal(projectName, branchName);
				}
			}
		});
	}
	
	return {
		startNeighborsArrange: function(projectName, branchName, pagePath) {
			require(['jquery.ui']);

			var activeEl = $('.neighbors li.active');
			activeEl.find('i.icon-move').remove();

			var aEl = activeEl.find('> a').setPreventClick(true);
			var buttonsEl = aEl.find('.buttons');

			var saveButton = $($.parseHTML('<i class="icon-ok icon-white" title="' + effectiveModuleOptions.saveText + '"></i>'))
				.click(function() {
					save(activeEl, buttonsEl, projectName, branchName, pagePath);
				});
			var resetButton = $($.parseHTML('<i class="icon-arrow-down icon-white" title="' + effectiveModuleOptions.sortAlphabeticallyText + '"></i>'))
				.click(function() {
					reset(buttonsEl, projectName, branchName, pagePath);
				});
			var cancelButton = $($.parseHTML('<i class="icon-off icon-white" title="' + effectiveModuleOptions.cancelText + '"></i>'))
				.click(function() {
					window.location.reload();
				});
			buttonsEl.empty().append([saveButton, resetButton, cancelButton]);

			activeEl.find('> ul > li').addClass('arrangeable').each(function() {
				var liEl = $(this);
				var manualOrder = liEl.data('manual-order');
				if (!documentr.isSomething(manualOrder) || (manualOrder != true)) {
					liEl.addClass('automatic-order');
				}
			});
			activeEl.find('ul li').each(function() {
				var liEl = $(this);
				var aEl = liEl.find('> a');
				var textEl = $.parseHTML(aEl.text());
				aEl.remove();
				liEl.prepend(textEl)
			});

			require(['jquery.ui'], function() {
				activeEl.find('> ul')
					.sortable({
						placeholder: 'drop-area',
						forcePlaceholderSize: true
					})
					.disableSelection();
			});
		},
		
		hookExpandHovers: function(projectName, branchName) {
			hookExpandHoversInternal(projectName, branchName);
		}
	};
});
