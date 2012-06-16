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

documentr.diffsToHtml = function(diffs) {
	var html = [];
	var pattern_amp = /&/g;
	var pattern_lt = /</g;
	var pattern_gt = />/g;
	var pattern_para = /\n/g;
	for (var x = 0; x < diffs.length; x++) {
		var op = diffs[x][0]; // Operation (insert, delete, equal)
		var data = diffs[x][1]; // Text of change.
		var text = data.replace(pattern_amp, '&amp;').replace(pattern_lt, '&lt;')
			.replace(pattern_gt, '&gt;').replace(pattern_para, '\n');
		switch (op) {
			case DIFF_INSERT:
				html[x] = '<ins>' + text + '</ins>';
				break;
			case DIFF_DELETE:
				html[x] = '<del>' + text + '</del>';
				break;
			case DIFF_EQUAL:
				html[x] = text;
				break;
		}
	}
	return '<pre class="changes"><code>' + html.join('') + '</code></pre>';
};


$.fn.extend({
	showModal: function(options) {
		this.modal(options);
		this.position({
			my: 'center center',
			at: 'center center',
			of: window
		});
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
