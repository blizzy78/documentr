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

<dt:pageJS>

<c:set var="hadConflicts" value="${false}"/>

var conflictTexts = {};
<c:forEach var="entry" items="${cherryPickResults}">
	<c:set var="branch" value="${entry.key}"/>
	<c:set var="branchResults" value="${entry.value}"/>
	<c:forEach var="result" items="${branchResults}">
		<c:set var="status"><c:out value="${result.status}"/></c:set>
		<c:if test="${status eq 'CONFLICT'}">
			conflictTexts['<c:out value="${branch}/${result.pageVersion.commitName}"/>'] = "<c:out value="${d:escapeJavaScript(result.conflictText)}" escapeXml="false"/>";
			<c:set var="hadConflicts" value="${true}"/>
		</c:if>
	</c:forEach>
</c:forEach>

var resolveTexts = {};
<c:forEach var="resolve" items="${resolves}">
	resolveTexts['<c:out value="${resolve.targetBranch}/${resolve.commit}"/>'] = "<c:out value="${d:escapeJavaScript(resolve.text)}" escapeXml="false"/>";
</c:forEach>

function editConflict(branchName, commit) {
	require(['documentr/dialog'], function() {
		var text;
		if (documentr.isSomething(resolveTexts[branchName + '/' + commit])) {
			text = resolveTexts[branchName + '/' + commit];
		} else {
			text = conflictTexts[branchName + '/' + commit];
		}
		var dlg = $('#conflict-dialog');
		var editor = dlg.data('editor');
		editor.setValue(text);
		dlg.data('branchName', branchName).data('commit', commit).showModal();
		editor.moveCursorTo(0, 0);
		editor.focus();
	});
}

function saveResolveText() {
	var dlg = $('#conflict-dialog');
	dlg.hideModal();
	var branchName = dlg.data('branchName');
	var commit = dlg.data('commit');
	var editor = dlg.data('editor');
	var text = editor.getValue();
	resolveTexts[branchName + '/' + commit] = text;
	preview();
}

function saveResolveTextsIntoForm() {
	var formEl = $('#cherryPickForm');
	$.each(resolveTexts, function(branchCommit, text) {
		var inputEl = $('<input type="hidden" name="resolveText_' + branchCommit + '"/>');
		formEl.append(inputEl);
		inputEl.val(text);
	});
}

function preview() {
	$('#cherryPickForm input[name="dryRun"]').val('true');
	saveResolveTextsIntoForm();
	$('#cherryPickForm')[0].submit();
}

<c:choose>
	<c:when test="${!hadConflicts}">
	
		function run() {
			$('#cherryPickForm input[name="dryRun"]').val('false');
			saveResolveTextsIntoForm();
			$('#cherryPickForm')[0].submit();
		}
	
	</c:when>
	<c:otherwise>

		$(function() {
			$('#finishButton').setButtonDisabled(true);
		});

	</c:otherwise>
</c:choose>

$(function() {
	require(['ace'], function(ace) {
		var editor = ace.edit('editor');
		$('#conflict-dialog').data('editor', editor);
		editor.setTheme('ace/theme/chrome');
		editor.session.setMode('ace/mode/merge_conflict');
		editor.setDisplayIndentGuides(true);
		editor.renderer.setShowGutter(false);
		editor.session.setUseWrapMode(true);
		editor.session.setWrapLimitRange(null, null);
		editor.renderer.setShowPrintMargin(false);
		editor.session.setUseSoftTabs(false);
		editor.setHighlightSelectedWord(false);
	});
});

</dt:pageJS>

<dt:breadcrumbs>
	<li><a href="<c:url value="/projects"/>"><spring:message code="title.projects"/></a> <span class="divider">/</span></li>
	<li><a href="<c:url value="/project/${projectName}"/>"><c:out value="${projectName}"/></a> <span class="divider">/</span></li>
	<li><a href="<c:url value="/page/${projectName}/${branchName}/home"/>"><c:out value="${branchName}"/></a> <span class="divider">/</span></li>
	<c:set var="hierarchy" value="${d:getPagePathHierarchy(projectName, branchName, path)}"/>
	<c:forEach var="entry" items="${hierarchy}" varStatus="status">
		<c:if test="${!status.first}">
			<li><a href="<c:url value="/page/${projectName}/${branchName}/${d:toUrlPagePath(entry)}"/>"><c:out value="${d:getPageTitle(projectName, branchName, entry)}"/></a> <span class="divider">/</span></li>
		</c:if>
	</c:forEach>
	<li class="active"><spring:message code="title.copyChangesToOtherBranches"/></li>
</dt:breadcrumbs>

<dt:pageTitle><spring:message code="title.copyChangesToOtherBranches"/></dt:pageTitle>

<dt:page>

<div class="page-header"><h1><spring:message code="title.copyChangesToOtherBranches" htmlEscape="false"/></h1></div>

<form id="cherryPickForm" action="<c:url value="/page/cherryPick/${projectName}/${branchName}/${d:toUrlPagePath(path)}"/>" method="POST">
	<input type="hidden" name="version1" value="${version1}"/>
	<input type="hidden" name="version2" value="${version2}"/>
	<c:forEach var="entry" items="${cherryPickResults}">
		<c:set var="branch" value="${entry.key}"/>
		<input type="hidden" name="branch" value="${branch}"/>
	</c:forEach>
	<input type="hidden" name="dryRun" value=""/>

	<table class="table table-bordered table-striped cherry-pick-preview">
		<c:forEach var="entry" items="${cherryPickResults}">
			<c:set var="branch" value="${entry.key}"/>
			<c:set var="branchResults" value="${entry.value}"/>
			<tr>
				<th colspan="4"><spring:message code="title.branchX" arguments="${branch}"/></th>
			</tr>

			<c:forEach var="result" items="${branchResults}">
				<c:set var="status"><c:out value="${result.status}"/></c:set>
				<c:choose>
					<c:when test="${status eq 'OK'}"><c:set var="cssStatus" value="success"/></c:when>
					<c:when test="${status eq 'CONFLICT'}"><c:set var="cssStatus" value="error"/></c:when>
					<c:otherwise><c:set var="cssStatus" value="status-unknown"/></c:otherwise>
				</c:choose>
				<tr class="${cssStatus}">
					<td><c:out value="${result.pageVersion.lastEditedBy}"/></td>
					<td><fmt:formatDate value="${result.pageVersion.lastEdited}" type="both" dateStyle="MEDIUM" timeStyle="SHORT"/></td>
					<td>
						<c:choose>
							<c:when test="${status eq 'OK'}"><spring:message code="merge.ok"/></c:when>
							<c:when test="${status eq 'CONFLICT'}"><spring:message code="merge.conflict"/></c:when>
							<c:otherwise><spring:message code="merge.pending"/></c:otherwise>
						</c:choose>
					</td>
					<td>
						<c:if test="${status eq 'CONFLICT'}"><a href="javascript:void(editConflict('${branch}', '${result.pageVersion.commitName}'));" class="btn btn-mini"><spring:message code="button.resolveConflict"/>...</a></c:if>
					</td>
				</tr>
			</c:forEach>
		</c:forEach>
	</table>
	
	<p>
		<c:if test="${hadConflicts}"><c:set var="cssPreviewPrimary" value="btn-primary"/></c:if>
		<a href="javascript:void(preview());" class="btn ${cssPreviewPrimary}"><spring:message code="button.preview"/></a>
		<c:if test="${!hadConflicts}"><c:set var="cssFinishPrimary" value="btn-primary"/></c:if>
		<a id="finishButton" href="javascript:void(run());" class="btn ${cssFinishPrimary}"><spring:message code="button.finish"/></a>
		<a href="<c:url value="/page/${projectName}/${branchName}/${d:toUrlPagePath(path)}"/>" class="btn"><spring:message code="button.cancel"/></a>
	</p>
</form>

<div class="modal modal-wide" id="conflict-dialog" style="display: none;">
	<div class="modal-header">
		<button class="close" onclick="$('#conflict-dialog').hideModal();">&#x00D7</button>
		<h3><spring:message code="title.resolveConflict"/></h3>
	</div>
	<div class="modal-body">
		<div class="editor-wrapper"><div id="editor"></div></div>
	</div>
	<div class="modal-footer">
		<a href="javascript:void(saveResolveText());" class="btn btn-primary"><spring:message code="button.save"/></a>
		<a href="javascript:void($('#conflict-dialog').hideModal());" class="btn"><spring:message code="button.cancel"/></a>
	</div>
</div>

</dt:page>
