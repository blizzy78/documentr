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

<sec:authorize access="hasPagePermission(#projectName, #branchName, #path, 'VIEW')">

<dt:headerJS>

<sec:authorize access="hasPagePermission(#projectName, #branchName, #pagePath, 'EDIT_PAGE')">

function showDeleteDialog(name) {
	var text = "<spring:message code="deleteAttachmentX" arguments=" "/>".replace(/' '/, '\'' + name + '\'');
	documentr.openMessageDialog('<spring:message code="title.deleteAttachment"/>', text, [
		{
			text: '<spring:message code="button.delete"/>',
			type: 'danger',
			href: '<c:url value="/attachment/delete/${projectName}/${branchName}/${d:toURLPagePath(pagePath)}/"/>' + name
		},
		{
			text: '<spring:message code="button.cancel"/>',
			cancel: true
		}
	]);
}

</sec:authorize>

</dt:headerJS>

<c:set var="pagePathUrl" value="${d:toURLPagePath(pagePath)}"/>
<dt:breadcrumbs>
	<li><a href="<c:url value="/projects"/>"><spring:message code="title.projects"/></a> <span class="divider">/</span></li>
	<li><a href="<c:url value="/project/${projectName}"/>"><c:out value="${projectName}"/></a> <span class="divider">/</span></li>
	<li><a href="<c:url value="/page/${projectName}/${branchName}/home"/>"><c:out value="${branchName}"/></a> <span class="divider">/</span></li>
	<c:set var="hierarchy" value="${d:getPagePathHierarchy(projectName, branchName, pagePath)}"/>
	<c:forEach var="entry" items="${hierarchy}" varStatus="status">
		<c:if test="${!status.first}">
			<li><a href="<c:url value="/page/${projectName}/${branchName}/${d:toURLPagePath(entry)}"/>"><c:out value="${d:getPageTitle(projectName, branchName, entry)}"/></a> <span class="divider">/</span></li>
		</c:if>
	</c:forEach>
	<li class="active"><spring:message code="title.attachments"/></li>
</dt:breadcrumbs>

<dt:pageTitle><spring:message code="title.attachments"/></dt:pageTitle>

<dt:page>

<div class="page-header"><h1><spring:message code="title.attachments"/></h1></div>

<c:set var="attachments" value="${d:listPageAttachments(projectName, branchName, pagePath)}"/>
<c:choose>
	<c:when test="${!empty attachments}">
		<table class="table table-documentr table-bordered table-striped">
			<thead>
				<tr>
					<th><spring:message code="title.fileName"/></th>
					<th><spring:message code="title.size"/></th>
					<th colspan="2"><spring:message code="title.lastEdit"/></th>
					<sec:authorize access="hasPagePermission(#projectName, #branchName, #pagePath, 'EDIT_PAGE')">
						<th><spring:message code="title.administration"/></th>
					</sec:authorize>
				</tr>
			</thead>
			<tbody>
				<c:forEach var="attachment" items="${attachments}">
					<c:set var="metadata" value="${d:getAttachmentMetadata(projectName, branchName, pagePath, attachment)}"/>
					<tr>
						<td><c:out value="${attachment}"/></td>
						<td><c:out value="${d:formatSize(metadata.size)}"/></td>
						<td><c:out value="${metadata.lastEditedBy}"/></td>
						<td><fmt:formatDate value="${metadata.lastEdited}" type="both" dateStyle="MEDIUM" timeStyle="SHORT"/></td>
						<sec:authorize access="hasPagePermission(#projectName, #branchName, #pagePath, 'EDIT_PAGE')">
							<td><a href="javascript:void(showDeleteDialog('${attachment}'));" class="btn btn-mini"><spring:message code="button.delete"/>...</a>
						</sec:authorize>
					</tr>
				</c:forEach>
			</tbody>
		</table>
	</c:when>
	<c:otherwise>
		<p><spring:message code="noAttachmentsFound"/></p>
	</c:otherwise>
</c:choose>

<sec:authorize access="hasPagePermission(#projectName, #branchName, #pagePath, 'EDIT_PAGE')">
	<p>
	<a href="<c:url value="/attachment/create/${projectName}/${branchName}/${d:toURLPagePath(pagePath)}"/>" class="btn"><i class="icon-plus"></i> <spring:message code="button.addAttachment"/></a>
	</p>
</sec:authorize>

</dt:page>

</sec:authorize>
