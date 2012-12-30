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

define(['module', 'jquery.jstree'], function(module) {
	"use strict";

	$.fn.extend({
		destroyPageTree: function() {
			var jstree = $.jstree._reference(this);
			if (documentr.isSomething(jstree)) {
				jstree.destroy();
			}
		},
		
		isPageTree: function() {
			var jstree = $.jstree._reference(this);
			return documentr.isSomething(jstree);
		}
	});

	var pageTreeOptions = module.config();
	
	return {
		createPageTree: function(treeEl, options) {
			var jstree = $.jstree._reference(treeEl);
			if (documentr.isSomething(jstree)) {
				return treeEl;
			}
			
			function getApplicationUrl() {
				return pageTreeOptions.applicationUrl;
			}
			
			function getProjectUrl(name) {
				return pageTreeOptions.projectUrl.replace(/_PROJECTNAME_/g, name);
			}
			
			function getBranchUrl(projectName, name) {
				return pageTreeOptions.branchUrl.replace(/_PROJECTNAME_/g, projectName).replace(/_BRANCHNAME_/g, name);
			}
			
			function getPageUrl(projectName, branchName, path) {
				return pageTreeOptions.pageUrl.replace(/_PROJECTNAME_/g, projectName).replace(/_BRANCHNAME_/g, branchName).replace(/_PAGEPATH_/g, path.replace(/\//g, ','));
			}
		
			if (!documentr.isSomething(options)) {
				options = {
					start: {
						type: 'application'
					},
				};
			}
			
			var startUrl = '';
			if (documentr.isSomething(options.start)) {
				if (options.start.type === 'project') {
					startUrl = getProjectUrl(options.start.projectName);
				} else if (options.start.type === 'branch') {
					startUrl = getBranchUrl(options.start.projectName, options.start.branchName);
				} else if (options.start.type === 'page') {
					startUrl = getPageUrl(options.start.projectName, options.start.branchName, options.start.pagePath);
				} else {
					startUrl = getApplicationUrl();
				}
			}
	
			var selectable = documentr.isSomething(options.selectable) ? options.selectable : {};
			var projectsSelectable = documentr.isSomething(selectable.projects) ? selectable.projects : true;
			var branchesSelectable = documentr.isSomething(selectable.branches) ? selectable.branches : true;
			var pagesSelectable = documentr.isSomething(selectable.pages) ? selectable.pages : true;
			
			var showPages = documentr.isSomething(options.showPages) ? options.showPages : true;
			var showAttachments = documentr.isSomething(options.showAttachments) ? options.showAttachments : false;
	
			var idPrefix = parseInt(Math.random() * 100000000, 10);
			var currentAttachmentId = 1;
			var attachmentIds = {};
			
			var tree = treeEl.jstree({
				plugins: ['themes', 'json_data', 'ui', 'types'],
				core: {
					animation: 0
				},
				themes: {
					theme: 'documentr'
				},
				ui: {
					select_limit: 1
				},
				types: {
					max_children: -2,
					max_depth: -2,
					types: {
						project: {
							select_node: projectsSelectable,
							icon: {
								image: pageTreeOptions.iconUrls.project
							}
						},
						
						branch: {
							select_node: branchesSelectable,
							icon: {
								image: pageTreeOptions.iconUrls.branch
							}
						},
						
						page: {
							select_node: pagesSelectable,
							icon: {
								image: pageTreeOptions.iconUrls.page
							}
						},
						
						attachment: {
							icon: {
								image: pageTreeOptions.iconUrls.attachment
							}
						}
					}
				},
				json_data: {
					ajax: {
						url: function(node) {
							var url = null;
							if (node === -1) {
								url = startUrl;
							} else {
								node = $(node);
								var type = node.data('type');
								if (type === 'project') {
									url = getProjectUrl(node.data('name'));
								} else if (type === 'branch') {
									url = getBranchUrl(node.data('projectName'), node.data('name'));
								} else if (type === 'page') {
									url = getPageUrl(node.data('projectName'), node.data('branchName'), node.data('path'));
								}
							}
							return url;
						},
						data: function(node) {
							var d = {};
							if (documentr.isSomething(options.checkBranchPermissions)) {
								d.checkBranchPermissions = options.checkBranchPermissions;
							}
							d.pages = showPages;
							d.attachments = showAttachments;
							return d;
						},
						type: 'POST',
						dataType: 'json',
						success: function(nodes) {
							var treeNodes = [];
							for (var i = 0; i < nodes.length; i++) {
								var node = nodes[i];
								if (node.type === 'PROJECT') {
									treeNodes.push({
										data: pageTreeOptions.projectTitle.replace(/_PROJECTNAME_/g, node.name),
										attr: {
											rel: 'project'
										},
										metadata: {
											type: 'project',
											name: node.name
										},
										state: 'closed'
									});
								} else if (node.type === 'BRANCH') {
									treeNodes.push({
										data: pageTreeOptions.branchTitle.replace(/_BRANCHNAME_/g, node.name),
										attr: {
											rel: 'branch'
										},
										metadata: {
											type: 'branch',
											projectName: node.projectName,
											name: node.name
										},
										state: 'closed'
									});
								} else if (node.type === 'PAGE') {
									if (!documentr.isSomething(options.filterPage) ||
										((node.projectName + '/' + node.branchName + '/' + node.path.replace(/\//g, ',')) !==
											options.filterPage)) {
										
										treeNodes.push({
											data: node.title,
											attr: {
												rel: 'page'
											},
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
								} else if (node.type === 'ATTACHMENT') {
									var id = 'tree_' + idPrefix + '_attachment_' + currentAttachmentId;
									currentAttachmentId++;
									treeNodes.push({
										data: node.name,
										attr: {
											rel: 'attachment',
											id: id
										},
										metadata: {
											type: 'attachment',
											projectName: node.projectName,
											branchName: node.branchName,
											pagePath: node.pagePath,
											name: node.name
										}
									});
									var attachmentId = node.name;
									attachmentIds[attachmentId] = id;
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
			jstree = $.jstree._reference(tree);
			tree.extend({
				selectAttachment: function(attachmentId) {
					var id = attachmentIds[attachmentId];
					if (documentr.isSomething(id)) {
						jstree.select_node('#' + id, true);
					}
				}
			});
			return tree;
		}
	};
});
