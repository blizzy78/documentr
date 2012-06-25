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

var documentr = {};

documentr.createPageTree = function(treeEl, options) {
	function getApplicationUrl() {
		return documentr.pageTreeOptions.applicationUrl;
	}
	
	function getProjectUrl(name) {
		return documentr.pageTreeOptions.projectUrl.replace(/_PROJECTNAME_/g, name);
	}
	
	function getBranchUrl(projectName, name) {
		return documentr.pageTreeOptions.branchUrl.replace(/_PROJECTNAME_/g, projectName).replace(/_BRANCHNAME_/g, name);
	}
	
	function getPageUrl(projectName, branchName, path) {
		return documentr.pageTreeOptions.pageUrl.replace(/_PROJECTNAME_/g, projectName).replace(/_BRANCHNAME_/g, branchName).replace(/_PAGEPATH_/g, path.replace(/\//g, ','));
	}
	
	if (options == null) {
		options = {
			start: {
				type: 'application'
			}
		};
	}
	
	var startUrl;
	if (typeof(options.start) != 'undefined') {
		if (options.start.type == 'project') {
			startUrl = getProjectUrl(options.start.projectName);
		} else if (options.start.type == 'branch') {
			startUrl = getBranchUrl(options.start.projectName, options.start.branchName);
		} else if (options.start.type == 'page') {
			startUrl = getPageUrl(options.start.projectName, options.start.branchName, options.start.pagePath);
		} else {
			startUrl = getApplicationUrl();
		}
	}
	
	var tree = treeEl.jstree({
		plugins: ['themes', 'json_data', 'ui'],
		core: {
			animation: 0
		},
		themes: {
			theme: 'documentr'
		},
		ui: {
			select_limit: 1
		},
		json_data: {
			ajax: {
				url: function(node) {
					var url = null;
					if (node == -1) {
						url = startUrl;
					} else {
						node = $(node);
						var type = node.data('type');
						if (type == 'project') {
							url = getProjectUrl(node.data('name'));
						} else if (type == 'branch') {
							url = getBranchUrl(node.data('projectName'), node.data('name'));
						} else if (type == 'page') {
							url = getPageUrl(node.data('projectName'), node.data('branchName'), node.data('path'));
						}
					}
					return url;
				},
				data: function(node) {
					var d = {};
					if (typeof(options.checkBranchPermissions) != 'undefined') {
						d.checkBranchPermissions = options.checkBranchPermissions;
					}
					return d;
				},
				type: 'POST',
				dataType: 'json',
				success: function(nodes) {
					var treeNodes = new Array();
					if (nodes.length > 0) {
						var type = nodes[0].type;
						if (type == 'PROJECT') {
							for (var i = 0; i < nodes.length; i++) {
								var node = nodes[i];
								treeNodes.push({
									data: documentr.pageTreeOptions.projectTitle.replace(/_PROJECTNAME_/g, node.name),
									metadata: {
										type: 'project',
										name: node.name
									},
									state: 'closed'
								});
							}
						} else if (type == 'BRANCH') {
							for (var i = 0; i < nodes.length; i++) {
								var node = nodes[i];
								treeNodes.push({
									data: documentr.pageTreeOptions.branchTitle.replace(/_BRANCHNAME_/g, node.name),
									metadata: {
										type: 'branch',
										projectName: node.projectName,
										name: node.name
									},
									state: 'closed'
								});
							}
						} else if (type == 'PAGE') {
							for (var i = 0; i < nodes.length; i++) {
								var node = nodes[i];
								if ((typeof(options.filterPage) == 'undefined') ||
									((node.projectName + '/' + node.branchName + '/' + node.path.replace(/\//g, ',')) !=
										options.filterPage)) {
									
									treeNodes.push({
										data: node.title,
										metadata: {
											type: 'page',
											projectName: node.projectName,
											branchName: node.branchName,
											path: node.path,
											hasBranchPermissions: node.hasBranchPermissions
										},
										state: 'closed'
									});
								}
							}
						}
					}
					return treeNodes;
				}
			}
		}
	})
	.delegate('a', 'click', function(event) {
		event.preventDefault();
	});
	return tree;
};

documentr.diffMarkdownAndGetHtml = function(markdown1, markdown2) {
	var dmp = new diff_match_patch();
	var diffs = dmp.diff_main(markdown1, markdown2);
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
};

documentr.openMessageDialog = function(title, message, buttons, options) {
	var backdrop = true;
	var keyboard = true;
	var wide = false;
	var messageAsHtml = false;
	if (typeof(options) != 'undefined') {
		if (typeof(options.backdrop) != 'undefined') {
			backdrop = options.backdrop;
		}
		if (typeof(options.keyboard) != 'undefined') {
			keyboard = options.keyboard;
		}
		if (typeof(options.wide) != 'undefined') {
			wide = options.wide;
		}
		if (typeof(options.messageAsHtml) != 'undefined') {
			messageAsHtml = options.messageAsHtml;
		}
	}
	
	var id = "dialog_" + new Date().getTime();
	var html =
		'<div class="modal" id="' + id + '" style="display: none;">' +
			'<div class="modal-header">' +
				'<button class="close" id="' + id + '_close">&#x00D7</button>' +
				'<h3 id="' + id + '_title">title</h3>' +
			'</div>' +
			'<div class="modal-body" id="' + id + '_body"></div>' +
			'<div class="modal-footer" id="' + id + '_footer"></div>' +
		'</div>';
	var dlgEl = $(html);
	if (wide) {
		dlgEl.addClass('modal-wide');
	}
	
	function close() {
		dlgEl.modal('hide');
		dlgEl.remove();
	}
	
	var dlg = {
		close: close
	};
	
	dlgEl.find('#' + id + '_title').text(title);
	if (messageAsHtml) {
		dlgEl.find('#' + id + '_body').html(message);
	} else {
		dlgEl.find('#' + id + '_body').text(message);
	}
	dlgEl.find('#' + id + '_close').click(function() {
		close();
	});
	var footerEl = dlgEl.find('#' + id + '_footer');
	for (var i = 0; i < buttons.length; i++) {
		var button = buttons[i];
		var b = $('<a href="#" class="btn"></a>');
		b.data('button_options', button);
		if ((typeof(button.onclick) != 'undefined') ||
			((typeof(button.cancel) != 'undefined') && button.cancel)) {
			
			b.click(function(event) {
				var buttonOptions = $(this).data('button_options');
				var closeDlg = (typeof(buttonOptions.close) != 'undefined') && buttonOptions.close;
				var cancelDlg = (typeof(buttonOptions.cancel) != 'undefined') && buttonOptions.cancel;
				if (cancelDlg) {
					closeDlg = true;
				}
				
				if (closeDlg) {
					close();
				}
				if (!cancelDlg) {
					buttonOptions.onclick.call(dlg);
				}
				event.preventDefault();
			});
		} else if (typeof(button.href) != 'undefined') {
			b.attr('href', button.href);
		}
		b.text(button.text);
		if (typeof(button.type) != 'undefined') {
			b.addClass('btn-' + button.type);
		}
		footerEl.append(b);
	}
	$(document.body).append(dlgEl);
	
	dlgEl.showModal({
		backdrop: backdrop,
		keyboard: keyboard
	});
	
	return dlg;
};


$.fn.extend({
	showModal: function(options) {
		this.modal(options);
		var win = $(window);
		this.css('margin-left', '0')
			.css('margin-top', '0')
			.css('left', Math.floor((win.width() - this.width()) / 2) + 'px')
			.css('top', Math.floor((win.height() - this.height()) / 2) + 'px');
	},
	
	hideModal: function() {
		this.modal('hide');
	},
	
	setPreventClick: function(preventClick) {
		var preventClickHandler = this.data('preventClickHandler');
		if ((preventClickHandler == null) && preventClick) {
			preventClickHandler = function(event) {
				event.preventDefault();
			};
			this.data('preventClickHandler', preventClickHandler);
			this.bind('click', preventClickHandler);
		} else if ((preventClickHandler != null) && !preventClick) {
			this.unbind('click', preventClickHandler);
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
