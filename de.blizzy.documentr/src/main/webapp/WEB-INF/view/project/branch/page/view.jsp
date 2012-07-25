<%--
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
--%>
<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@ taglib prefix="sec" uri="http://www.springframework.org/security/tags" %>
<%@ taglib prefix="d" uri="http://documentr.org/tld/documentr" %>
<%@ taglib prefix="dt" tagdir="/WEB-INF/tags" %>

<%
long random = (long) (Math.random() * Long.MAX_VALUE);
pageContext.setAttribute("random", Long.valueOf(random)); //$NON-NLS-1$
%>

<sec:authorize access="hasPagePermission(#projectName, #branchName, #path, 'VIEW')">

<dt:headerJS>

<sec:authorize access="hasAnyBranchPermission(#projectName, 'EDIT_PAGE')">

function showCopyToBranchDialog() {
	$('#copy-dialog').showModal();
	copyToBranchSelected();
}

function copyToBranchSelected() {
	var button = $('#copyToBranchButton');
	button.setButtonDisabled(true);
	
	var el = $('#copyToBranchForm').find('select');
	var branch = el.val();
	$.ajax({
		url: '<c:url value="/page/generateName/${projectName}/"/>' + branch + '/<c:out value="${d:toURLPagePath(parentPagePath)}"/>/json',
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

<sec:authorize access="hasBranchPermission(#projectName, #branchName, 'EDIT_PAGE')">

function showDeleteDialog() {
	documentr.openMessageDialog('<spring:message code="title.deletePage"/>',
		<c:set var="text"><spring:message code="deletePageX.html" arguments="${title}" argumentSeparator="__DUMMY__SEPARATOR__${random}__"/></c:set>
		'<c:out value="${fn:replace(text, &quot;'&quot;, &quot;\\\\'&quot;)}" escapeXml="false"/>', [
		{
			text: '<spring:message code="button.delete"/>',
			type: 'danger',
			href: '<c:url value="/page/delete/${projectName}/${branchName}/${d:toURLPagePath(path)}"/>'
		},
		{
			text: '<spring:message code="button.cancel"/>',
			cancel: true
		}
	], {
		messageAsHtml: true
	});
}

function showRelocateDialog() {
	function showDialog() {
		$('#relocate-dialog').showModal();
	}

	var treeEl = $('#relocate-target-tree');
	if (treeEl.children().length === 0) {
		documentr.createPageTree(treeEl, {
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
				filterPage: '<c:out value="${projectName}/${branchName}/${d:toURLPagePath(path)}"/>'
			})
			.bind('loaded.jstree', function() {
				showDialog();
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
							$('#relocateForm').find('input:hidden[name="newParentPagePath"]').val(node.data('path'));
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

		$('#relocate-button').setButtonDisabled(true);
	} else {
		showDialog();
	}
}

</sec:authorize>

<sec:authorize access="hasPagePermission(#projectName, #branchName, #path, 'EDIT_PAGE')">

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

function saveInlineEditor() {
	var formEl = $('#inlineEditorForm');
	var textEl = formEl.data('textEl');
	formEl.data('textEl', null);
	formEl.hide();
	$('#pageText').after(formEl);
	$(textEl).show();
	$('#inlineEditorToolbar').hide();
	$.ajax({
		url: '<c:url value="/page/saveRange/${projectName}/${branchName}/${d:toURLPagePath(path)}/json"/>',
		type: 'POST',
		dataType: 'json',
		data: {
			markdown: formEl.find('textarea').val(),
			range: $(textEl).attr('data-text-range')
		},
		success: function(result) {
			$('#pageText').html(result.html);
			toggleHideFloatingElements(false);
			prettyPrint();
			$('#inlineEditorToolbar').hide();
			hookupInlineEditorToolbar();
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

	$.ajax({
		url: '<c:url value="/page/markdownInRange/${projectName}/${branchName}/${d:toURLPagePath(path)}/"/>' + range + '/json',
		type: 'GET',
		dataType: 'json',
		success: function(result) {
			var formEl = $('#inlineEditorForm');
			formEl.hide();
			formEl.detach();
			$(textEl).after(formEl);
			$(textEl).hide();
			toggleHideFloatingElements(true);
			formEl.data('textEl', textEl);
			formEl.find('textarea').val(result.markdown);
			formEl.show();
			formEl.find('textarea').focus();
		}
	});
}

function hookupInlineEditorToolbar() {
	function showEl(el) {
		el.stop(true).fadeTo(0, 1);
	}
	
	function hideEl(el) {
		el.stop(true).fadeTo(3000, 0.25);
	}

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
		url: '<c:url value="/page/restoreVersion/${projectName}/${branchName}/${d:toURLPagePath(path)}/json"/>',
		type: 'POST',
		dataType: 'json',
		data: {
			version: previousCommit
		},
		success: function(result) {
			window.location.href = '<c:url value="/page/${projectName}/${branchName}/${d:toURLPagePath(path)}"/>?_=' + new Date().getTime();
		}
	});
}

</sec:authorize>

<sec:authorize access="isAuthenticated()">

function showChangesDialog() {
	$.ajax({
		url: '<c:url value="/page/markdown/${projectName}/${branchName}/${d:toURLPagePath(path)}/json?versions=latest,previous"/>',
		type: 'GET',
		dataType: 'json',
		success: function(result) {
			var previous = documentr.isSomething(result.previous) ? result[result.previous] : '';
			var html = documentr.diffMarkdownAndGetHtml(previous, result[result.latest]);
			$('#changes-dialog-body').html(html);
			if (documentr.isSomething(result.previous)) {
				$('#changes-dialog').data('previousCommit', result.previous);
				$('#restore-old-commit-button').show();
			} else {
				$('#changes-dialog').data('previousCommit', null);
				$('#restore-old-commit-button').hide();
			}
			$('#changes-dialog').showModal();
		}
	});
}

</sec:authorize>

<sec:authorize access="hasPagePermission(#projectName, #branchName, #path, 'EDIT_PAGE')">

$(function() {
	hookupInlineEditorToolbar();
});

</sec:authorize>

</dt:headerJS>

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
						<c:when test="${!status.last}"><li><a href="<c:url value="/page/${projectName}/${branchName}/${d:toURLPagePath(entry)}"/>"><c:out value="${d:getPageTitle(projectName, branchName, entry)}"/></a> <span class="divider">/</span></li></c:when>
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

<sec:authorize access="isAuthenticated() or
	hasPagePermission(#projectName, #branchName, #path, 'EDIT_PAGE') or
	hasBranchPermission(#projectName, #branchName, 'EDIT_PAGE') or
	hasAnyBranchPermission(#projectName, 'EDIT_PAGE')">

	<div class="btn-toolbar pull-right page-toolbar">
		<sec:authorize access="hasPagePermission(#projectName, #branchName, #path, 'EDIT_PAGE')">
			<div class="btn-group">
				<a href="<c:url value="/page/edit/${projectName}/${branchName}/${d:toURLPagePath(path)}"/>" class="btn"><i class="icon-edit"></i> <spring:message code="button.edit"/></a>
			</div>
		</sec:authorize>

		<div class="btn-group">
			<a class="btn dropdown-toggle" data-toggle="dropdown" href="#"><i class="icon-cog"></i> <spring:message code="button.tools"/> <span class="caret"></span></a>
			<ul class="dropdown-menu">
				<sec:authorize access="hasPagePermission(#projectName, #branchName, #path, 'EDIT_PAGE')">
					<dt:dropdownEntry>
						<li><a href="<c:url value="/attachment/create/${projectName}/${branchName}/${d:toURLPagePath(path)}"/>"><i class="icon-download-alt"></i> <spring:message code="button.addAttachment"/></a></li>
					</dt:dropdownEntry>
				</sec:authorize>
				<sec:authorize access="isAuthenticated()">
					<dt:dropdownEntry>
						<c:set var="attachments" value="${d:listPageAttachments(projectName, branchName, path)}"/>
						<li><a href="<c:url value="/attachment/list/${projectName}/${branchName}/${d:toURLPagePath(path)}"/>"><i class="icon-list"></i>
								<c:choose>
									<c:when test="${!empty attachments}"><spring:message code="button.attachmentsX" arguments="${fn:length(attachments)}"/></c:when>
									<c:otherwise><spring:message code="button.attachments"/></c:otherwise>
								</c:choose>
							</a></li>
					</dt:dropdownEntry>
				</sec:authorize>
				<sec:authorize access="hasBranchPermission(#projectName, #branchName, 'EDIT_PAGE')">
					<dt:dropdownEntry divider="true">
						<li><a href="<c:url value="/page/create/${projectName}/${branchName}/${d:toURLPagePath(path)}"/>"><i class="icon-file"></i> <spring:message code="button.addChildPage"/></a></li>
					</dt:dropdownEntry>
				</sec:authorize>
				<sec:authorize access="isAuthenticated()">
					<dt:dropdownEntry divider="true">
						<li><a href="<c:url value="/page/changes/${projectName}/${branchName}/${d:toURLPagePath(path)}"/>"><i class="icon-book"></i> <spring:message code="button.changes"/></a></li>
					</dt:dropdownEntry>
				</sec:authorize>
				
				<c:if test="${path ne 'home'}">
					<dt:dropdownEntry divider="true">
						<sec:authorize access="hasAnyBranchPermission(#projectName, 'EDIT_PAGE')">
							<c:if test="${fn:length(branches) ge 2}">
								<%-- doesn't work correctly for "home" page --%>
								<li><a href="javascript:void(showCopyToBranchDialog());"><i class="icon-share-alt"></i> <spring:message code="button.copyToBranch"/>...</a></li>
							</c:if>
						</sec:authorize>
						<li><a href="javascript:void(showRelocateDialog());"><i class="icon-arrow-right"></i> <spring:message code="button.relocate"/>...</a></li>
						<%-- "home" page must not be deleted --%>
						<sec:authorize access="hasBranchPermission(#projectName, #branchName, 'EDIT_PAGE')">
							<li><a href="javascript:void(showDeleteDialog());"><i class="icon-trash"></i> <spring:message code="button.delete"/>...</a></li>
						</sec:authorize>
					</dt:dropdownEntry>
				</c:if>
			</ul>
		</div>
	</div>
</sec:authorize>

<div class="page-header">
<h1>
<c:out value="${title}"/>
</h1>
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
<div class="page-metadata"><spring:message code="lastEditX" arguments="${lastEdit}" argumentSeparator="|"/><%--
--%><sec:authorize access="isAuthenticated()"><%--
--%> (<a href="javascript:void(showChangesDialog());"><spring:message code="button.showChanges"/></a>)<%--
--%></sec:authorize><%--
--%><c:if test="${!empty branchNames}"> &ndash; <spring:message code="sharedWithX" arguments="${branchNames}" argumentSeparator="|"/></c:if><%--
--%></div>

</div>

<span id="pageText"><c:out value="${d:getPageHTML(projectName, branchName, path)}" escapeXml="false"/></span>

<sec:authorize access="hasPagePermission(#projectName, #branchName, #path, 'EDIT_PAGE')">
	<p class="spacer">
	<a href="<c:url value="/page/edit/${projectName}/${branchName}/${d:toURLPagePath(path)}"/>" class="btn"><i class="icon-edit"></i> <spring:message code="button.editPage"/></a>
	</p>

	<div id="inlineEditorToolbar" class="btn-toolbar btn-toolbar-icons btn-toolbar-floating" style="display: none; position: absolute;">
		<button class="btn" title="<spring:message code="button.edit"/>"><i class="icon-edit"></i></button>
	</div>
	
	<form id="inlineEditorForm" class="inline-editor" style="display: none;">
		<textarea class="code span12" rows="7"></textarea>
		<a class="btn btn-mini btn-primary" href="javascript:void(saveInlineEditor())"><spring:message code="button.save"/></a>
		<a class="btn btn-mini" href="javascript:void(cancelInlineEditor())"><spring:message code="button.cancel"/></a>
	</form>
</sec:authorize>

<sec:authorize access="hasAnyBranchPermission(#projectName, 'EDIT_PAGE')">
	<div class="modal" id="copy-dialog" style="display: none;">
		<div class="modal-header">
			<button class="close" onclick="$('#copy-dialog').modal('hide');">×</button>
			<h3><spring:message code="title.copyPageToBranch"/></h3>
		</div>
		<div class="modal-body">
			<form id="copyToBranchForm" action="<c:url value="/page/copyToBranch/${projectName}/${branchName}/${d:toURLPagePath(path)}"/>" method="POST" class="form-horizontal">
				<fieldset class="control-group">
					<label class="control-label"><spring:message code="label.copyToBranch"/>:</label>
					<select name="targetBranchName" onchange="copyToBranchSelected();">
						<c:forEach var="branch" items="${branches}">
							<c:if test="${branch ne branchName}">
								<sec:authorize access="hasBranchPermission(#projectName, #branch, 'EDIT_PAGE')">
									<option value="<c:out value="${branch}"/>"><c:out value="${branch}"/></option>
								</sec:authorize>
								<sec:authorize access="!hasBranchPermission(#projectName, #branch, 'EDIT_PAGE') and
									hasBranchPermission(#projectName, #branch, 'VIEW')">
									
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
			<a href="javascript:void($('#copy-dialog').modal('hide'));" class="btn"><spring:message code="button.cancel"/></a>
		</div>
	</div>
</sec:authorize>

<sec:authorize access="hasBranchPermission(#projectName, #branchName, 'EDIT_PAGE')">
	<div class="modal" id="relocate-dialog" style="display: none;">
		<div class="modal-header">
			<button class="close" onclick="$('#relocate-dialog').modal('hide');">×</button>
			<h3><spring:message code="title.relocatePage"/></h3>
		</div>
		<div class="modal-body">
			<form id="relocateForm" action="<c:url value="/page/relocate/${projectName}/${branchName}/${d:toURLPagePath(path)}"/>" method="POST" class="form-horizontal">
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
			<a href="javascript:void($('#relocate-dialog').modal('hide'));" class="btn"><spring:message code="button.cancel"/></a>
		</div>
	</div>
</sec:authorize>

<sec:authorize access="isAuthenticated()">
	<div class="modal modal-wide" id="changes-dialog" style="display: none;">
		<div class="modal-header">
			<button class="close" onclick="$('#changes-dialog').modal('hide');">×</button>
			<h3><spring:message code="title.changes"/></h3>
		</div>
		<div class="modal-body" id="changes-dialog-body"></div>
		<div class="modal-footer">
			<sec:authorize access="hasPagePermission(#projectName, #branchName, #path, 'EDIT_PAGE')">
				<a href="javascript:void(restoreOldVersion());" id="restore-old-commit-button" class="btn btn-warning"><spring:message code="button.restoreOldVersion"/></a>
			</sec:authorize>
			<a href="javascript:void($('#changes-dialog').modal('hide'));" class="btn btn-primary"><spring:message code="button.close"/></a>
		</div>
	</div>
</sec:authorize>

</dt:page>

</sec:authorize>
