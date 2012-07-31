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

<sec:authorize access="isAuthenticated() and hasPagePermission(#projectName, #branchName, #path, VIEW)">

<c:set var="versions" value="${d:listPageVersions(projectName, branchName, path)}"/>

<c:if test="${fn:length(versions) >= 2}">

<dt:headerJS>

function getCheckedRadioIndex(radiosSel, checkedRadioSel) {
	var radioEls = $(radiosSel);
	var checkedRadioEl = $(checkedRadioSel);
	var idx;
	for (var i = 0; i < radioEls.length; i++) {
		if ($(radioEls[i]).val() === checkedRadioEl.val()) {
			idx = i;
			break;
		}
	}
	return idx;
}

function updateButtons() {
	var idx = getCheckedRadioIndex('input:radio[name="version1"]', 'input:radio:checked[name="version1"]');
	var otherRadioEls = $('input:radio[name="version2"]');
	for (var i = 0; i < otherRadioEls.length; i++) {
		$(otherRadioEls[i]).attr('disabled', (i > idx) ? 'disabled' : null);
	}

	idx = getCheckedRadioIndex('input:radio[name="version2"]', 'input:radio:checked[name="version2"]');
	otherRadioEls = $('input:radio[name="version1"]');
	for (var i = 0; i < otherRadioEls.length; i++) {
		$(otherRadioEls[i]).attr('disabled', (i < idx) ? 'disabled' : null);
	}
}

function showChangesDialog() {
	var version1 = $('input:radio:checked[name="version1"]').val();
	var version2 = $('input:radio:checked[name="version2"]').val();

	$.ajax({
		url: '<c:url value="/page/markdown/${projectName}/${branchName}/${d:toURLPagePath(path)}/json?versions="/>' + version1 + ',' + version2,
		type: 'GET',
		dataType: 'json',
		success: function(result) {
			var html = documentr.diffMarkdownAndGetHtml(result[version1], result[version2]);
			$('#changes-dialog-body').html(html);
			$('#changes-dialog').data('previousCommit', version1)
				.showModal();
		}
	});
}

<sec:authorize access="hasPagePermission(#projectName, #branchName, #path, EDIT_PAGE)">

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
			window.location.href = '<c:url value="/page/${projectName}/${branchName}/${d:toURLPagePath(path)}"/>';
		}
	});
}

</sec:authorize>

$(function() {
	updateButtons();
});

</dt:headerJS>

</c:if>

<c:set var="pagePathUrl" value="${d:toURLPagePath(pagePath)}"/>
<dt:breadcrumbs>
	<li><a href="<c:url value="/projects"/>"><spring:message code="title.projects"/></a> <span class="divider">/</span></li>
	<li><a href="<c:url value="/project/${projectName}"/>"><c:out value="${projectName}"/></a> <span class="divider">/</span></li>
	<li><a href="<c:url value="/page/${projectName}/${branchName}/home"/>"><c:out value="${branchName}"/></a> <span class="divider">/</span></li>
	<c:set var="hierarchy" value="${d:getPagePathHierarchy(projectName, branchName, path)}"/>
	<c:forEach var="entry" items="${hierarchy}" varStatus="status">
		<c:if test="${!status.first}">
			<li><a href="<c:url value="/page/${projectName}/${branchName}/${d:toURLPagePath(entry)}"/>"><c:out value="${d:getPageTitle(projectName, branchName, entry)}"/></a> <span class="divider">/</span></li>
		</c:if>
	</c:forEach>
	<li class="active"><spring:message code="title.changes"/></li>
</dt:breadcrumbs>

<dt:pageTitle><spring:message code="title.changes"/></dt:pageTitle>

<dt:page>

<div class="page-header"><h1><spring:message code="title.changes"/></h1></div>

<table class="table table-documentr table-bordered table-striped">
	<thead>
		<tr>
			<c:if test="${fn:length(versions) >= 2}">
				<th colspan="2">&nbsp;</th>
			</c:if>
			<th colspan="2"><spring:message code="title.lastEdit"/></th>
		</tr>
	</thead>
	<tbody>
		<c:forEach var="version" items="${versions}" varStatus="status">
			<tr>
				<c:if test="${fn:length(versions) >= 2}">
					<td class="radio">
						<c:choose>
							<c:when test="${status.index eq 1}"><input type="radio" name="version1" value="<c:out value="${version.commitName}"/>" checked="checked" onclick="updateButtons()"/></c:when>
							<c:when test="${!status.first}"><input type="radio" name="version1" value="<c:out value="${version.commitName}"/>" onclick="updateButtons()"/></c:when>
							<c:otherwise>&nbsp;</c:otherwise>
						</c:choose>
					</td>
					<td class="radio">
						<c:choose>
							<c:when test="${status.first}"><input type="radio" name="version2" value="<c:out value="${version.commitName}"/>" checked="checked" onclick="updateButtons()"/></c:when>
							<c:when test="${!status.last}"><input type="radio" name="version2" value="<c:out value="${version.commitName}"/>" onclick="updateButtons()"/></c:when>
							<c:otherwise>&nbsp;</c:otherwise>
						</c:choose>
					</td>
				</c:if>
				<td><c:out value="${version.lastEditedBy}"/></td>
				<td><fmt:formatDate value="${version.lastEdited}" type="both" dateStyle="MEDIUM" timeStyle="SHORT"/></td>
			</tr>
		</c:forEach>
	</tbody>
</table>

<c:if test="${fn:length(versions) >= 2}">
	<p>
	<a href="javascript:void(showChangesDialog());" class="btn"><spring:message code="button.showChangesBetweenSelectedVersions"/></a>
	</p>
</c:if>

<div class="modal modal-wide" id="changes-dialog" style="display: none;">
	<div class="modal-header">
		<button class="close" onclick="$('#changes-dialog').modal('hide');">×</button>
		<h3><spring:message code="title.changes"/></h3>
	</div>
	<div class="modal-body" id="changes-dialog-body"></div>
	<div class="modal-footer">
		<sec:authorize access="hasPagePermission(#projectName, #branchName, #path, EDIT_PAGE)">
			<a href="javascript:void(restoreOldVersion());" id="restore-old-commit-button" class="btn btn-warning"><spring:message code="button.restoreOldVersion"/></a>
		</sec:authorize>
		<a href="javascript:void($('#changes-dialog').modal('hide'));" class="btn btn-primary"><spring:message code="button.close"/></a>
	</div>
</div>

</dt:page>

</sec:authorize>
