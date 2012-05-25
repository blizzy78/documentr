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
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@ taglib prefix="sec" uri="http://www.springframework.org/security/tags" %>
<%@ taglib prefix="d" uri="http://documentr.org/tld/documentr" %>
<%@ taglib prefix="dt" tagdir="/WEB-INF/tags" %>

<%
long random = (long) (Math.random() * Long.MAX_VALUE);
pageContext.setAttribute("random", Long.valueOf(random)); //$NON-NLS-1$
%>

<sec:authorize access="isAuthenticated()">
<dt:headerJS>

function showCopyToBranchDialog() {
	$('#copy-dialog').showModal({backdrop: true, keyboard: true});
	copyToBranchSelected();
}

function copyToBranchSelected() {
	var button = $('#copyToBranchButton');
	button.addClass('disabled');
	
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
			button.removeClass('disabled');
		}
	});
}

function showDeleteDialog() {
	$('#delete-dialog').showModal({backdrop: true, keyboard: true});
}

</dt:headerJS>
</sec:authorize>

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

<dt:page>

<sec:authorize access="isAuthenticated()">
	<c:set var="attachments" value="${d:listPageAttachments(projectName, branchName, path)}"/>
	<div class="btn-toolbar pull-right page-toolbar">
		<div class="btn-group">
			<a href="<c:url value="/page/edit/${projectName}/${branchName}/${d:toURLPagePath(path)}"/>" class="btn" title="<spring:message code="button.editPage"/>"><i class="icon-edit"></i> <spring:message code="button.edit"/></a>
		</div>
		<div class="btn-group">
			<a class="btn dropdown-toggle" data-toggle="dropdown" href="#"><i class="icon-cog"></i> <spring:message code="button.tools"/> <span class="caret"></span></a>
			<ul class="dropdown-menu">
				<li><a href="<c:url value="/attachment/create/${projectName}/${branchName}/${d:toURLPagePath(path)}"/>"><i class="icon-download-alt"></i> <spring:message code="button.addAttachment"/></a></li>
				<li><a href="<c:url value="/attachment/list/${projectName}/${branchName}/${d:toURLPagePath(path)}"/>"><i class="icon-list"></i>
						<c:choose>
							<c:when test="${!empty attachments}"><spring:message code="button.attachmentsX" arguments="${fn:length(attachments)}"/></c:when>
							<c:otherwise><spring:message code="button.attachments"/></c:otherwise>
						</c:choose>
					</a></li>
				<li class="divider"></li>
				<li><a href="<c:url value="/page/create/${projectName}/${branchName}/${d:toURLPagePath(path)}"/>"><i class="icon-file"></i> <spring:message code="button.addChildPage"/></a></li>
				
				<c:if test="${path ne 'home'}">
					<li class="divider"></li>
					<%-- doesn't work correctly for "home" page --%>
					<li><a href="javascript:void(showCopyToBranchDialog());"><i class="icon-share-alt"></i> <spring:message code="button.copyToBranch"/>...</a></li>
					<%-- "home" page must not be deleted --%>
					<li><a href="javascript:void(showDeleteDialog());"><i class="icon-trash"></i> <spring:message code="button.delete"/>...</a></li>
				</c:if>
			</ul>
		</div>
	</div>
</sec:authorize>

<div class="page-header"><h1><c:out value="${title}"/>
<sec:authorize access="isAuthenticated()">
	<c:set var="branches" value="${d:getBranchesPageIsSharedWith(projectName, branchName, path)}"/>
	<c:if test="${fn:length(branches) ge 2}">
		<c:set var="branches" value="${d:join(branches, ', ')}"/>
		<span class="shared-page">(<spring:message code="sharedWithX" arguments="${branches}" argumentSeparator="|"/>)</span>
	</c:if>
</sec:authorize>
</h1>
</div>

<c:out value="${d:getPageHTML(projectName, branchName, path)}" escapeXml="false"/>

<sec:authorize access="isAuthenticated()">
	<p class="spacer">
	<a href="<c:url value="/page/edit/${projectName}/${branchName}/${d:toURLPagePath(path)}"/>" class="btn"><i class="icon-edit"></i> <spring:message code="button.editPage"/></a>
	</p>

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
						<c:set var="branches" value="${d:listProjectBranches(projectName)}"/>
						<c:forEach var="branch" items="${branches}">
							<c:if test="${branch ne branchName}">
								<option value="<c:out value="${branch}"/>"><c:out value="${branch}"/></option>
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

	<div class="modal" id="delete-dialog" style="display: none;">
		<div class="modal-header">
			<button class="close" onclick="$('#delete-dialog').modal('hide');">×</button>
			<h3><spring:message code="title.deletePage"/></h3>
		</div>
		<div class="modal-body">
			<spring:message code="deletePageX" arguments="${title}" argumentSeparator="__DUMMY__SEPARATOR__${random}__"/>
		</div>
		<div class="modal-footer">
			<a href="<c:url value="/page/delete/${projectName}/${branchName}/${d:toURLPagePath(path)}"/>" class="btn btn-danger"><spring:message code="button.delete"/></a>
			<a href="javascript:void($('#delete-dialog').modal('hide'));" class="btn"><spring:message code="button.cancel"/></a>
		</div>
	</div>
</sec:authorize>

</dt:page>
