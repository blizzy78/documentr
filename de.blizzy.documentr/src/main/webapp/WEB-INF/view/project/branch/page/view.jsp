<%--
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
--%>
<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@ taglib prefix="sec" uri="http://www.springframework.org/security/tags" %>
<%@ taglib prefix="d" uri="http://documentr.org/tld/documentr" %>
<%@ taglib prefix="dt" tagdir="/WEB-INF/tags" %>

<sec:authorize access="hasPagePermission(#projectName, #branchName, #path, VIEW)">

<dt:pageJS>

<sec:authorize access="hasPagePermission(#projectName, #branchName, #path, EDIT_PAGE)">

var currentCommit = '<c:out value="${commit}"/>';

</sec:authorize>

<sec:authorize access="hasAnyBranchPermission(#projectName, EDIT_PAGE)">

function showCopyToBranchDialog() {
	require(['documentr/dialog'], function() {
		$('#copy-dialog').showModal();
		copyToBranchSelected();
	});
}

function copyToBranchSelected() {
	var button = $('#copyToBranchButton');
	button.setButtonDisabled(true);
	
	var el = $('#copyToBranchForm select');
	var branch = el.val();
	$.ajax({
		url: '<c:url value="/page/generateName/${projectName}/"/>' + branch + '/<c:out value="${d:toUrlPagePath(parentPagePath)}"/>/json',
		type: 'POST',
		dataType: 'json',
		data: {
			title: '<c:out value="${pageName}"/>'
		},
		success: function(result) {
			if (result.exists) {
				button.removeClass('btn-primary').addClass('btn-warning');
				button.text('<spring:message code="button.overwrite"/>');
			} else {
				button.removeClass('btn-warning').addClass('btn-primary');
				button.text('<spring:message code="button.copy"/>');
			}
		},
		complete: function() {
			button.setButtonDisabled(false);
		}
	});
}

</sec:authorize>

<sec:authorize access="hasBranchPermission(#projectName, #branchName, EDIT_PAGE)">

function toggleHideFloatingElements(hide) {
	$('#pageText').children().each(function() {
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

function showDeleteDialog() {
	require(['documentr/dialog'], function(dialog) {
		dialog.openMessageDialog('<spring:message code="title.deletePage"/>',
			<c:set var="text"><spring:message code="deletePageX.html" arguments="${title}" argumentSeparator="__DUMMY__SEPARATOR__"/></c:set>
			'<c:out value="${fn:replace(text, &quot;'&quot;, &quot;\\\\'&quot;)}" escapeXml="false"/>', [
			{
				text: '<spring:message code="button.delete"/>',
				type: 'danger',
				href: '<c:url value="/page/delete/${projectName}/${branchName}/${d:toUrlPagePath(path)}"/>'
			},
			{
				text: '<spring:message code="button.cancel"/>',
				cancel: true
			}
		], {
			messageAsHtml: true
		});
	});
}

function showRelocateDialog() {
	require(['documentr/pageTree', 'documentr/dialog'], function(pageTree) {
		function showDialog() {
			$('#relocate-dialog').showModal();
		}
		
		var treeEl = $('#relocate-target-tree');
		if (treeEl.children().length === 0) {
			pageTree.createPageTree(treeEl, {
					start: {
						type: 'branch',
						projectName: '<c:out value="${projectName}"/>',
						branchName: '<c:out value="${branchName}"/>'
					},
					selectable: {
						projects: false,
						branches: false
					},
					checkBranchPermissions: 'EDIT_PAGE',
					filterPage: '<c:out value="${projectName}/${branchName}/${d:toUrlPagePath(path)}"/>'
				})
				.bind('select_node.jstree', function(event, data) {
					var node = data.rslt.obj;
					var button = $('#relocate-button');
					if ((node.data('type') === 'page') && node.data('hasBranchPermissions') &&
						(node.data('path') !== '<c:out value="${parentPagePath}"/>')) {
						button.setButtonDisabled(true);
						
						$.ajax({
							url: '<c:url value="/page/generateName/"/>' + node.data('projectName') + '/' + node.data('branchName') + '/' + node.data('path').replace(/\//g, ',') + '/json',
							type: 'POST',
							dataType: 'json',
							data: {
								title: '<c:out value="${pageName}"/>'
							},
							success: function(result) {
								button.text(result.exists ? '<spring:message code="button.overwriteAndRelocate"/>' : '<spring:message code="button.relocate"/>');
								if (result.exists) {
									button.removeClass('btn-primary').addClass('btn-warning');
								} else {
									button.removeClass('btn-warning').addClass('btn-primary');
								}
								$('#relocateForm input:hidden[name="newParentPagePath"]').val(node.data('path'));
							},
							complete: function() {
								button.setButtonDisabled(false);
							}
						});
					} else {
						button.text('<spring:message code="button.relocate"/>');
						button.removeClass('btn-warning').addClass('btn-primary');
						button.setButtonDisabled(true);
					}
				})
				.bind('deselect_node.jstree', function() {
					var button = $('#relocate-button');
					button.text('<spring:message code="button.relocate"/>');
					button.removeClass('btn-warning').addClass('btn-primary');
					button.setButtonDisabled(true);
				});
	
			window.setTimeout(showDialog, 500);
			$('#relocate-button').setButtonDisabled(true);
		} else {
			showDialog();
		}
	});
}

function startPageSplit() {
	toggleHideFloatingElements(true);
	$('#pageText').data({
		pageSplitMode: 'start',
		pageSplitStart: null,
		pageSplitEnd: null
	});
	updatePageSplitRange();
}

function updatePageSplitRange() {
	var pageText = $('#pageText');
	var startEl = pageText.data('pageSplitStart');
	var endEl = pageText.data('pageSplitEnd');
	var inside = false;
	if (!documentr.isSomething(startEl) && !documentr.isSomething(endEl)) {
		inside = true;
	}
	$('#pageText > *[data-text-range]').each(function() {
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

	var mode = $('#pageText').data('pageSplitMode');
	var textEl = (mode === 'start') ? startEl : endEl;
	var splitCursorEl = $('#splitCursor');
	var splitCursorStartEl = $('#splitCursorStart');
	var splitBGEl = $('#splitBG');
	if (documentr.isSomething(textEl)) {
		var wasHidden = $('#splitCursor:hidden').length > 0;
	
		textEl = $(textEl);
		splitCursorEl.css({
				left: textEl.offset().left,
				top: (mode === 'start') ? (textEl.offset().top - splitCursorEl.outerHeight() - 3) : (textEl.offset().top + textEl.outerHeight() + 3),
				width: textEl.outerWidth()
			})
			.show();

		if (mode === 'start') {
			splitCursorEl.find('.above').show();
			splitCursorEl.find('.below').hide();
			splitCursorStartEl.hide();
			$('#splitBG').hide();
		} else {
			splitCursorEl.find('.above').hide();
			splitCursorEl.find('.below').show();
			splitCursorStartEl.show();
			splitBGEl.css({
				left: splitCursorStartEl.offset().left,
				top: splitCursorStartEl.offset().top + splitCursorStartEl.outerHeight(),
				<%-- minus 2 to account for border --%>
				width: splitCursorStartEl.outerWidth() - 2,
				height: splitCursorEl.offset().top - splitCursorStartEl.offset().top - splitCursorStartEl.outerHeight()
			}).show();
		}
		
		<%-- force another refresh if cursor was hidden before to set cursor top position correctly --%>
		if (wasHidden) {
			window.setTimeout(updatePageSplitRange, 1);
		}
	} else {
		splitCursorEl.hide();
		splitCursorStartEl.hide();
		splitBGEl.hide();
	}
}

function hookupSplitCursor() {
	$('#pageText > *[data-text-range]')
		.mouseenter(function() {
			var mode = $('#pageText').data('pageSplitMode');
			if ((mode === 'start') || (mode === 'end')) {
				var textEl = $(this);
				var float = textEl.css('float');
				if ((float !== 'left') && (float !== 'right')) {
					var ok = true;
					if (mode === 'end') {
						var els = $('#pageText > *[data-text-range]');
						var startEl = $('#pageText').data('pageSplitStart');
						var startIdx = els.index(startEl);
						var endIdx = els.index(textEl[0]);
						if (endIdx < startIdx) {
							ok = false;
						}
					}

					if (ok) {
						$('#pageText').data((mode === 'start') ? 'pageSplitStart' : 'pageSplitEnd', textEl[0]);
						updatePageSplitRange();
					}
				}
			}
		})
		.click(function() {
			var pageTextEl = $('#pageText');
			var mode = pageTextEl.data('pageSplitMode');
			if (mode === 'start') {
				pageTextEl.data({
					pageSplitEnd: $('#pageText').data('pageSplitStart'),
					pageSplitMode: 'end'
				});
				var splitCursorEl = $('#splitCursor');
				$('#splitCursorStart').css({
					left: splitCursorEl.offset().left,
					top: splitCursorEl.offset().top,
					width: splitCursorEl.outerWidth()
				});
				updatePageSplitRange();
			} else if (mode === 'end') {
				var startEl = $(pageTextEl.data('pageSplitStart'));
				var endEl = $(pageTextEl.data('pageSplitEnd'));
				var start = startEl.attr('data-text-range').replace(/,.*/, '');
				var end = endEl.attr('data-text-range').replace(/.*,/, '');
				window.location.href = '<c:url value="/page/split/${projectName}/${branchName}/${d:toUrlPagePath(path)}/"/>' + start + ',' + end;
			}
		});
}

</sec:authorize>

<sec:authorize access="hasPagePermission(#projectName, #branchName, #path, EDIT_PAGE)">

function saveInlineEditor() {
	var formEl = $('#inlineEditorForm');
	var textEl = formEl.data('textEl');
	formEl.data('textEl', null);
	formEl.hide();
	$('#pageText').after(formEl);
	$(textEl).show();
	$('#inlineEditorToolbar').hide();
	$.ajax({
		url: '<c:url value="/page/saveRange/${projectName}/${branchName}/${d:toUrlPagePath(path)}/json"/>',
		type: 'POST',
		dataType: 'json',
		data: {
			markdown: formEl.data('editor').getValue(),
			range: $(textEl).attr('data-text-range'),
			commit: currentCommit
		},
		success: function(result) {
			if (documentr.isSomething(result.conflict) && result.conflict) {
				window.location.href = '<c:url value="/page/edit/${projectName}/${branchName}/${d:toUrlPagePath(path)}"/>';
			} else {
				$('#pageText').html(result.html);
				currentCommit = result.commit;
				toggleHideFloatingElements(false);
				documentr.setupCodeViews();
				$('#inlineEditorToolbar').hide();
				hookupInlineEditorToolbar();
			}
		}
	});
}

function cancelInlineEditor() {
	var formEl = $('#inlineEditorForm');
	var textEl = formEl.data('textEl');
	if (documentr.isSomething(textEl)) {
		formEl.data('textEl', null);
		formEl.hide();
		$(textEl).show();
		$('#inlineEditorToolbar').hide();
		toggleHideFloatingElements(false);
	}
}

function startInlineEditor(textEl, range) {
	cancelInlineEditor();

	var editor = null;

	require(['ace'], function(ace) {
		var ed = $('#inlineEditorForm').data('editor');
		if (!documentr.isSomething(ed)) {
			ed = ace.edit('editor');
			$('#inlineEditorForm').data('editor', ed);
			ed.setTheme('ace/theme/chrome');
			ed.session.setMode('ace/mode/markdown');
			ed.setDisplayIndentGuides(true);
			ed.renderer.setShowGutter(false);
			ed.session.setUseWrapMode(true);
			ed.session.setWrapLimitRange(null, null);
			ed.renderer.setShowPrintMargin(false);
			ed.session.setUseSoftTabs(false);
			ed.setHighlightSelectedWord(false);
			ed.setHighlightActiveLine(false);
		}
		editor = ed;
	});

	$.ajax({
		url: '<c:url value="/page/markdownInRange/${projectName}/${branchName}/${d:toUrlPagePath(path)}/"/>' + range + '/' + currentCommit + '/json',
		type: 'GET',
		dataType: 'json',
		success: function(result) {
			documentr.waitFor(function() {
				return documentr.isSomething(editor);
			}, function() {
				var formEl = $('#inlineEditorForm');
				formEl.hide();
				formEl.detach();
				$(textEl).after(formEl);
				$(textEl).hide();
				toggleHideFloatingElements(true);
				formEl.data('textEl', textEl);
				editor.setValue(result.markdown);
				formEl.show();
				editor.focus();
				editor.moveCursorTo(0, 0);
			});
		}
	});
}

function hookupInlineEditorToolbar() {
	$('#pageText > *[data-text-range]').mouseenter(function() {
		var textEl = $(this);
		var float = textEl.css('float');
		if ((float !== 'left') && (float !== 'right')) {
			var toolbarEl = $('#inlineEditorToolbar');
			toolbarEl
				.css('left', textEl.offset().left - toolbarEl.width() - 10)
				.css('top', textEl.offset().top)
				.fadeTo(0, 0.5);
			var buttonEl = $('#inlineEditorToolbar button');
			buttonEl.off('click');
			buttonEl.click(
				{
					el: this,
					range: textEl.attr('data-text-range')
				},
				function(event) {
					startInlineEditor(event.data.el, event.data.range);
				});
		}
	});
	
	$('#inlineEditorToolbar').hover(
		function() {
			$(this).fadeTo(0, 1);
		},
		function() {
			$(this).fadeTo(0, 0.5);
		});
}

function restoreOldVersion() {
	var previousCommit = $('#changes-dialog').data('previousCommit');
	$.ajax({
		url: '<c:url value="/page/restoreVersion/${projectName}/${branchName}/${d:toUrlPagePath(path)}/json"/>',
		type: 'POST',
		dataType: 'json',
		data: {
			version: previousCommit
		},
		success: function(result) {
			window.location.href = '<c:url value="/page/${projectName}/${branchName}/${d:toUrlPagePath(path)}"/>?_=' + new Date().getTime();
		}
	});
}

</sec:authorize>

<sec:authorize access="isAuthenticated()">

function showChangesDialog() {
	var dlg = $('#changes-dialog');
	var editor = null;

	require(['ace'], function(ace) {
		var ed = dlg.data('editor');
		if (!documentr.isSomething(ed)) {
			ed = ace.edit('changes-editor');
			dlg.data('editor', ed);
			ed.setTheme('ace/theme/chrome');
			ed.session.setMode('ace/mode/markdown');
			ed.setReadOnly(true);
			ed.setDisplayIndentGuides(true);
			ed.renderer.setShowGutter(false);
			ed.session.setUseWrapMode(true);
			ed.session.setWrapLimitRange(null, null);
			ed.renderer.setShowPrintMargin(false);
			ed.session.setUseSoftTabs(false);
			ed.setHighlightSelectedWord(false);
			ed.setHighlightActiveLine(false);
			ed.renderer.hideCursor();
		}
		editor = ed;
	});

	require(['documentr/diffMarkdown', 'documentr/dialog']);
	$.ajax({
		url: '<c:url value="/page/markdown/${projectName}/${branchName}/${d:toUrlPagePath(path)}/json?versions=latest,previous"/>',
		type: 'GET',
		dataType: 'json',
		success: function(markdownResult) {
			require(['ace', 'documentr/diffMarkdown', 'documentr/dialog'], function(ace, diffMarkdown) {
				var previous = documentr.isSomething(markdownResult.previous) ? markdownResult[markdownResult.previous] : '';
				var diffResult = diffMarkdown.diff(previous, markdownResult[markdownResult.latest]);

				documentr.waitFor(function() {
					return documentr.isSomething(editor);
				}, function() {
					$.each(editor.session.getMarkers(false), function(idx, marker) {
						if ((marker.clazz === 'editor-marker-insert') || (marker.clazz === 'editor-marker-delete')) {
							editor.session.removeMarker(marker.id);
						}
					});
				
					editor.setValue(diffResult.text);
					var Range = ace.require('ace/range').Range;
					$.each(diffResult.markers, function(idx, marker) {
						var range = new Range(marker.startLine, marker.startColumn, marker.endLine, marker.endColumn);
						editor.session.addMarker(range, marker.insert ? 'editor-marker-insert' : 'editor-marker-delete', 'text');
					});

					if (documentr.isSomething(markdownResult.previous)) {
						$('#changes-dialog').data('previousCommit', markdownResult.previous);
						$('#restore-old-commit-button').show();
					} else {
						$('#changes-dialog').data('previousCommit', null);
						$('#restore-old-commit-button').hide();
					}

					editor.focus();
					editor.moveCursorTo(0, 0);

					dlg.showModal();
				});
			});
		}
	});
}

<sec:authorize access="hasPagePermissionInOtherBranches(#projectName, #branchName, #path, VIEW)">
function showCompareWithBranchDialog() {
	compareWithBranchSelected();
}

function compareWithBranchSelected() {
	$('#compareWithBranch').attr('disabled', 'disabled');
	var branch = $('#compareWithBranch').val();
	var markdown = {};
	var dlg = $('#compare-with-branch-dialog');
	var editor = null;

	require(['ace'], function(ace) {
		var ed = dlg.data('editor');
		if (!documentr.isSomething(ed)) {
			ed = ace.edit('compare-with-branch-editor');
			dlg.data('editor', ed);
			ed.setTheme('ace/theme/chrome');
			ed.session.setMode('ace/mode/markdown');
			ed.setReadOnly(true);
			ed.setDisplayIndentGuides(true);
			ed.renderer.setShowGutter(false);
			ed.session.setUseWrapMode(true);
			ed.session.setWrapLimitRange(null, null);
			ed.renderer.setShowPrintMargin(false);
			ed.session.setUseSoftTabs(false);
			ed.setHighlightSelectedWord(false);
			ed.setHighlightActiveLine(false);
			ed.renderer.hideCursor();
		}
		editor = ed;
	});

	require(['documentr/diffMarkdown', 'documentr/dialog']);
	
	markdown.current = $('body').data('currentMarkdown');
	if (!documentr.isSomething(markdown.current)) {
		$.ajax({
			url: '<c:url value="/page/markdown/${projectName}/${branchName}/${d:toUrlPagePath(path)}/json?versions=latest"/>',
			type: 'GET',
			dataType: 'json',
			success: function(result) {
				markdown.current = result[result.latest];
				$('body').data('currentMarkdown', markdown.current);
			}
		});
	}
	$.ajax({
		url: '<c:url value="/page/markdown/${projectName}/__BRANCH__/${d:toUrlPagePath(path)}/json?versions=latest"/>'.replace(/__BRANCH__/, branch),
		type: 'GET',
		dataType: 'json',
		success: function(result) {
			if (documentr.isSomething(result.latest)) {
				markdown.other = result[result.latest];
			}
			markdown.otherLoaded = true;
		}
	});
	documentr.waitFor(function() {
		return documentr.isSomething(editor) &&
			documentr.isSomething(markdown.current) && documentr.isSomething(markdown.otherLoaded);
	}, function() {
		require(['documentr/diffMarkdown', 'documentr/dialog'], function(diffMarkdown) {
			$.each(editor.session.getMarkers(false), function(idx, marker) {
				if ((marker.clazz === 'editor-marker-insert') || (marker.clazz === 'editor-marker-delete')) {
					editor.session.removeMarker(marker.id);
				}
			});

			var markdownOther = documentr.isSomething(markdown.other) ? markdown.other : '';
			var diffResult = diffMarkdown.diff(markdown.current, markdownOther);
			editor.setValue(diffResult.text);
			var Range = ace.require('ace/range').Range;
			$.each(diffResult.markers, function(idx, marker) {
				var range = new Range(marker.startLine, marker.startColumn, marker.endLine, marker.endColumn);
				editor.session.addMarker(range, marker.insert ? 'editor-marker-insert' : 'editor-marker-delete', 'text');
			});

			editor.focus();
			editor.moveCursorTo(0, 0);

			dlg.showModal();
			$('#compareWithBranch').removeAttr('disabled');
		});
	});
}
</sec:authorize>

function subscribe() {
	require(['documentr/dialog']);
	$.ajax({
		url: '<c:url value="/subscription/subscribe/${projectName}/${branchName}/${d:toUrlPagePath(path)}/json"/>',
		type: 'GET',
		success: function() {
			require(['documentr/dialog'], function(dialog) {
				dialog.openMessageDialog('<spring:message code="title.subscribe"/>',
					'<spring:message code="subscribedSuccessfully"/>', [
						{
							text: '<spring:message code="button.close"/>',
							cancel: true
						}
					]);
			});
		}
	});
}

function unsubscribe() {
	require(['documentr/dialog']);
	$.ajax({
		url: '<c:url value="/subscription/unsubscribe/${projectName}/${branchName}/${d:toUrlPagePath(path)}/json"/>',
		type: 'GET',
		success: function() {
			require(['documentr/dialog'], function(dialog) {
				dialog.openMessageDialog('<spring:message code="title.unsubscribe"/>',
					'<spring:message code="unsubscribedSuccessfully"/>', [
						{
							text: '<spring:message code="button.close"/>',
							cancel: true
						}
					]);
			});
		}
	});
}

</sec:authorize>

<sec:authorize access="isAuthenticated() or
	hasPagePermission(#projectName, #branchName, #path, EDIT_PAGE)">

	$(function() {
		<sec:authorize access="isAuthenticated()">
			<%-- subscription mails entry point --%>
			if (window.location.hash === '#changes') {
				showChangesDialog();
			}
		</sec:authorize>
		
		<sec:authorize access="hasPagePermission(#projectName, #branchName, #path, EDIT_PAGE)">
			hookupInlineEditorToolbar();
			hookupSplitCursor();
		</sec:authorize>
	});
</sec:authorize>

</dt:pageJS>

<dt:breadcrumbs>
	<li><a href="<c:url value="/projects"/>"><spring:message code="title.projects"/></a> <span class="divider">/</span></li>
	<li><a href="<c:url value="/project/${projectName}"/>"><c:out value="${projectName}"/></a> <span class="divider">/</span></li>
	<c:set var="hierarchy" value="${d:getPagePathHierarchy(projectName, branchName, path)}"/>
	<c:choose>
		<c:when test="${fn:length(hierarchy) gt 1}">
			<li><a href="<c:url value="/page/${projectName}/${branchName}/home"/>"><c:out value="${branchName}"/></a> <span class="divider">/</span></li>
			<c:forEach var="entry" items="${hierarchy}" varStatus="status">
				<c:if test="${!status.first}">
					<c:choose>
						<c:when test="${!status.last}"><li><a href="<c:url value="/page/${projectName}/${branchName}/${d:toUrlPagePath(entry)}"/>"><c:out value="${d:getPageTitle(projectName, branchName, entry)}"/></a> <span class="divider">/</span></li></c:when>
						<c:otherwise><li class="active"><c:out value="${d:getPageTitle(projectName, branchName, entry)}"/></li></c:otherwise>
					</c:choose>
				</c:if>
			</c:forEach>
		</c:when>
		<c:otherwise><li class="active"><c:out value="${branchName}"/></c:otherwise>
	</c:choose>
</dt:breadcrumbs>

<dt:pageTitle><c:out value="${title}"/></dt:pageTitle>

<dt:page>

<c:set var="branches" value="${d:listProjectBranches(projectName)}"/>
<c:set var="pageHeader"><c:out value="${d:getPageHeaderHtml(projectName, branchName, path)}" escapeXml="false"/></c:set>

<c:set var="pageHeaderToolbar">
<sec:authorize access="isAuthenticated() or
	hasPagePermission(#projectName, #branchName, #path, EDIT_PAGE) or
	hasBranchPermission(#projectName, #branchName, EDIT_PAGE) or
	hasAnyBranchPermission(#projectName, EDIT_PAGE)">

	<c:if test="${!empty pageHeader}"><c:set var="cssNoMargin" value="btn-toolbar-no-top-margin"/></c:if>

	<div class="btn-toolbar ${cssNoMargin} pull-right page-toolbar">
		<sec:authorize access="hasPagePermission(#projectName, #branchName, #path, EDIT_PAGE)">
			<div class="btn-group">
				<a href="<c:url value="/page/edit/${projectName}/${branchName}/${d:toUrlPagePath(path)}"/>" class="btn"><i class="icon-edit"></i> <spring:message code="button.edit"/></a>
			</div>
		</sec:authorize>

		<div class="btn-group">
			<a class="btn dropdown-toggle" data-toggle="dropdown" href="#"><i class="icon-cog"></i> <spring:message code="button.tools"/> <span class="caret"></span></a>
			<ul class="dropdown-menu">
				<dt:dropdownEntry divider="true">
					<sec:authorize access="hasBranchPermission(#projectName, #branchName, EDIT_PAGE)">
						<li><a href="<c:url value="/page/create/${projectName}/${branchName}/${d:toUrlPagePath(path)}"/>"><i class="icon-file"></i> <spring:message code="button.addChildPage"/></a></li>
						<li><a href="javascript:void(startPageSplit());"><i class="icon-file"></i> <spring:message code="button.splitPage"/></a></li>
					</sec:authorize>
				</dt:dropdownEntry>

				<dt:dropdownEntry divider="true">
					<sec:authorize access="hasPagePermission(#projectName, #branchName, #path, EDIT_PAGE)">
						<li><a href="<c:url value="/attachment/create/${projectName}/${branchName}/${d:toUrlPagePath(path)}"/>"><i class="icon-download-alt"></i> <spring:message code="button.addAttachment"/></a></li>
					</sec:authorize>
					<sec:authorize access="isAuthenticated()">
						<c:set var="attachments" value="${d:listPageAttachments(projectName, branchName, path)}"/>
						<li><a href="<c:url value="/attachment/list/${projectName}/${branchName}/${d:toUrlPagePath(path)}"/>"><i class="icon-list"></i>
								<c:choose>
									<c:when test="${!empty attachments}"><spring:message code="button.attachmentsX" arguments="${fn:length(attachments)}"/></c:when>
									<c:otherwise><spring:message code="button.attachments"/></c:otherwise>
								</c:choose>
							</a></li>
					</sec:authorize>
				</dt:dropdownEntry>

				<dt:dropdownEntry divider="true">
					<sec:authorize access="isAuthenticated()">
						<li><a href="<c:url value="/page/changes/${projectName}/${branchName}/${d:toUrlPagePath(path)}"/>"><i class="icon-book"></i> <spring:message code="button.changes"/></a></li>
						<sec:authorize access="hasPagePermissionInOtherBranches(#projectName, #branchName, #path, VIEW)">
							<li><a href="javascript:void(showCompareWithBranchDialog());"><i class="icon-book"></i> <spring:message code="button.compareWithBranch"/></a></li>
						</sec:authorize>
						<c:choose>
							<c:when test="${!d:isSubscribed(projectName, branchName, path)}">
								<li><a href="javascript:void(subscribe());"><i class="icon-envelope"></i> <spring:message code="button.subscribe"/></a>
							</c:when>
							<c:otherwise>
								<li><a href="javascript:void(unsubscribe());"><i class="icon-envelope"></i> <spring:message code="button.unsubscribe"/></a>
							</c:otherwise>
						</c:choose>
					</sec:authorize>
				</dt:dropdownEntry>

				<dt:dropdownEntry divider="true">
					<c:if test="${path ne 'home'}">
						<sec:authorize access="hasAnyBranchPermission(#projectName, EDIT_PAGE)">
							<c:if test="${fn:length(branches) ge 2}">
								<%-- doesn't work correctly for "home" page --%>
								<li><a href="javascript:void(showCopyToBranchDialog());"><i class="icon-share-alt"></i> <spring:message code="button.copyToBranch"/>...</a></li>
							</c:if>
						</sec:authorize>
						<sec:authorize access="hasBranchPermission(#projectName, #branchName, EDIT_PAGE)">
							<li><a href="javascript:void(showRelocateDialog());"><i class="icon-arrow-right"></i> <spring:message code="button.relocate"/>...</a></li>
						</sec:authorize>
						<%-- "home" page must not be deleted --%>
						<sec:authorize access="hasBranchPermission(#projectName, #branchName, EDIT_PAGE)">
							<li><a href="javascript:void(showDeleteDialog());"><i class="icon-trash"></i> <spring:message code="button.delete"/>...</a></li>
						</sec:authorize>
					</c:if>
				</dt:dropdownEntry>
			</ul>
		</div>
	</div>
</sec:authorize>
</c:set>

<c:set var="metadata" value="${d:getPageMetadata(projectName, branchName, path)}"/>
<c:set var="lastEdited"><fmt:formatDate value="${metadata.lastEdited}" type="both" dateStyle="MEDIUM" timeStyle="SHORT"/></c:set>
<c:choose>
	<c:when test="${!empty metadata.lastEditedBy}"><c:set var="lastEdit"><spring:message code="lastEdit.userXOnDateX" arguments="${metadata.lastEditedBy}|${lastEdited}" argumentSeparator="|"/></c:set></c:when>
	<c:otherwise><c:set var="lastEdit" value="${lastEdited}"/></c:otherwise>
</c:choose>
<sec:authorize access="isAuthenticated()">
	<c:set var="branchesSharedWith" value="${d:getBranchesPageIsSharedWith(projectName, branchName, path)}"/>
	<c:if test="${fn:length(branchesSharedWith) ge 2}">
		<c:set var="branchNames" value="${d:join(branchesSharedWith, ', ')}"/>
	</c:if>
</sec:authorize>

<c:if test="${empty pageHeader}"><c:set var="pageHeader"><h1><c:out value="${title}"/></h1></c:set></c:if>
<div class="page-header">
	<c:out value="${pageHeaderToolbar}" escapeXml="false"/>
	<c:out value="${pageHeader}" escapeXml="false"/>
	<div class="page-metadata"><spring:message code="lastEditX" arguments="${lastEdit}" argumentSeparator="|"/><%--
		--%><sec:authorize access="isAuthenticated()"><%--
		--%> (<a href="javascript:void(showChangesDialog());"><spring:message code="button.showChanges"/></a>)<%--
		--%></sec:authorize><%--
		--%><c:if test="${!empty branchNames}"> &ndash; <spring:message code="sharedWithX" arguments="${branchNames}" argumentSeparator="|"/></c:if><%--
		--%></div>
</div>

<span id="pageText"><c:out value="${d:getPageHtml(projectName, branchName, path)}" escapeXml="false"/></span>

<sec:authorize access="hasPagePermission(#projectName, #branchName, #path, EDIT_PAGE)">
	<p class="spacer">
	<a href="<c:url value="/page/edit/${projectName}/${branchName}/${d:toUrlPagePath(path)}"/>" class="btn"><i class="icon-edit"></i> <spring:message code="button.editPage"/></a>
	</p>

	<div id="inlineEditorToolbar" class="btn-toolbar btn-toolbar-icons btn-toolbar-floating" style="display: none; position: absolute;">
		<button class="btn" title="<spring:message code="button.edit"/>"><i class="icon-edit"></i></button>
	</div>
	
	<form id="inlineEditorForm" class="inline-editor" style="display: none;">
		<div class="editor-wrapper"><div id="editor"></div></div>
		<a class="btn btn-mini btn-primary" href="javascript:void(saveInlineEditor())"><spring:message code="button.save"/></a>
		<a class="btn btn-mini" href="javascript:void(cancelInlineEditor())"><spring:message code="button.cancel"/></a>
	</form>
</sec:authorize>

<sec:authorize access="hasAnyBranchPermission(#projectName, EDIT_PAGE)">
	<div class="modal" id="copy-dialog" style="display: none;">
		<div class="modal-header">
			<button class="close" onclick="$('#copy-dialog').hideModal();">&#x00D7</button>
			<h3><spring:message code="title.copyPageToBranch"/></h3>
		</div>
		<div class="modal-body">
			<form id="copyToBranchForm" action="<c:url value="/page/copyToBranch/${projectName}/${branchName}/${d:toUrlPagePath(path)}"/>" method="POST" class="form-horizontal">
				<fieldset class="control-group">
					<label class="control-label"><spring:message code="label.copyToBranch"/>:</label>
					<select name="targetBranchName" onchange="copyToBranchSelected();">
						<c:forEach var="branch" items="${branches}">
							<c:if test="${branch ne branchName}">
								<sec:authorize access="hasBranchPermission(#projectName, #branch, EDIT_PAGE)">
									<option value="<c:out value="${branch}"/>"><c:out value="${branch}"/></option>
								</sec:authorize>
								<sec:authorize access="!hasBranchPermission(#projectName, #branch, EDIT_PAGE) and
									hasBranchPermission(#projectName, #branch, VIEW)">
									
									<option value="<c:out value="${branch}"/>" disabled="disabled"><c:out value="${branch}"/></option>
								</sec:authorize>
							</c:if>
						</c:forEach>
					</select>
				</fieldset>
			</form>
		</div>
		<div class="modal-footer">
			<a id="copyToBranchButton" href="javascript:$('#copyToBranchForm').submit();" class="btn btn-primary"><spring:message code="button.copy"/></a>
			<a href="javascript:void($('#copy-dialog').hideModal());" class="btn"><spring:message code="button.cancel"/></a>
		</div>
	</div>
</sec:authorize>

<sec:authorize access="hasBranchPermission(#projectName, #branchName, EDIT_PAGE)">
	<div id="splitCursor" class="page-split-cursor" style="display: none;"><div class="above"><i class="icon-chevron-down"></i> <spring:message code="title.pageSplitStart"/></div><div class="line"></div><div class="below"><i class="icon-chevron-up"></i> <spring:message code="title.pageSplitEnd"/></div></div>
	<div id="splitCursorStart" class="page-split-cursor" style="display: none;"><div class="above"><i class="icon-chevron-down"></i> <spring:message code="title.pageSplitStart"/></div><div class="line"></div></div>
	<div id="splitBG" class="page-split-background" style="display: none;"></div>

	<div class="modal" id="relocate-dialog" style="display: none;">
		<div class="modal-header">
			<button class="close" onclick="$('#relocate-dialog').hideModal();">&#x00D7</button>
			<h3><spring:message code="title.relocatePage"/></h3>
		</div>
		<div class="modal-body">
			<form id="relocateForm" action="<c:url value="/page/relocate/${projectName}/${branchName}/${d:toUrlPagePath(path)}"/>" method="POST" class="form-horizontal">
				<fieldset>
					<input type="hidden" name="newParentPagePath" value=""/>
	
					<div class="control-group">
						<label class="control-label"><spring:message code="label.newParentPage"/>:</label>
						<div class="controls">
							<div id="relocate-target-tree"></div>
						</div>
					</div>
				</fieldset>
			</form>
		</div>
		<div class="modal-footer">
			<a id="relocate-button" href="javascript:$('#relocateForm').submit();" class="btn btn-primary"><spring:message code="button.relocate"/></a>
			<a href="javascript:void($('#relocate-dialog').hideModal());" class="btn"><spring:message code="button.cancel"/></a>
		</div>
	</div>
</sec:authorize>

<sec:authorize access="isAuthenticated()">
	<div class="modal modal-wide" id="changes-dialog" style="display: none;">
		<div class="modal-header">
			<button class="close" onclick="$('#changes-dialog').hideModal();">&#x00D7</button>
			<h3><spring:message code="title.changes"/></h3>
		</div>
		<div class="modal-body" id="changes-dialog-body">
			<div class="editor-wrapper"><div id="changes-editor" class="code-view"></div></div>
		</div>
		<div class="modal-footer">
			<sec:authorize access="hasPagePermission(#projectName, #branchName, #path, EDIT_PAGE)">
				<a href="javascript:void(restoreOldVersion());" id="restore-old-commit-button" class="btn btn-warning"><spring:message code="button.restoreOldVersion"/></a>
			</sec:authorize>
			<a href="javascript:void($('#changes-dialog').hideModal());" class="btn"><spring:message code="button.close"/></a>
		</div>
	</div>
	
	<sec:authorize access="hasPagePermissionInOtherBranches(#projectName, #branchName, #path, VIEW)">
		<div class="modal modal-wide" id="compare-with-branch-dialog" style="display: none;">
			<div class="modal-header">
				<button class="close" onclick="$('#compare-with-branch-dialog').hideModal();">&#x00D7</button>
				<h3><spring:message code="title.compareWithBranch"/></h3>
				<form class="form-inline">
					<label for="compareWithBranch"><strong><spring:message code="label.compareWithBranch"/>:</strong></label>
					<select id="compareWithBranch" class="input-large" onchange="compareWithBranchSelected()">
						<c:forEach var="branch" items="${branches}">
							<c:if test="${branch ne branchName}">
								<option value="${branch}"><c:out value="${branch}"/></option>
							</c:if>
						</c:forEach>
					</select>
				</form>
			</div>
			<div class="modal-body" id="compare-with-branch-dialog-body">
				<div class="editor-wrapper"><div id="compare-with-branch-editor" class="code-view"></div></div>
			</div>
			<div class="modal-footer">
				<a href="javascript:void($('#compare-with-branch-dialog').hideModal());" class="btn"><spring:message code="button.close"/></a>
			</div>
		</div>
	</sec:authorize>
</sec:authorize>

</dt:page>

</sec:authorize>
