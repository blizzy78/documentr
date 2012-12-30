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

<sec:authorize access="isAuthenticated() and hasPagePermission(#projectName, #branchName, #pagePath, VIEW)">

<dt:pageJS>

<sec:authorize access="hasPagePermission(#projectName, #branchName, #pagePath, EDIT_PAGE)">

var jqXHRs = [];

function showDeleteDialog(name) {
	require(['documentr/dialog'], function(dialog) {
		var text = "<spring:message code="deleteAttachmentX" arguments=" "/>".replace(/' '/, '\'' + name + '\'');
		dialog.openMessageDialog('<spring:message code="title.deleteAttachment"/>', text, [
			{
				text: '<spring:message code="button.delete"/>',
				type: 'danger',
				href: '<c:url value="/attachment/delete/${projectName}/${branchName}/${d:toUrlPagePath(pagePath)}/"/>' + name
			},
			{
				text: '<spring:message code="button.cancel"/>',
				cancel: true
			}
		]);
	});
}

function cancelUpload() {
	$('#upload-dialog .modal-footer a').setButtonDisabled(true);
	$.each(jqXHRs, function(idx, jqXHR) {
		jqXHR.abort();
	});
	jqXHRs = [];
}

$(function() {
	require(['jquery.fileupload'], function() {
		$('input[type="file"]').fileupload({
			url: '<c:url value="/attachment/saveViaJson/${projectName}/${branchName}/${d:toUrlPagePath(pagePath)}/json"/>',
			dataType: 'json',
			add: function(e, data) {
				var jqXHR = data.submit();
				jqXHRs.push(jqXHR);
			},
			progressall: function(e, data) {
				require(['documentr/dialog'], function() {
					$('#upload-dialog').showModal();
					var percent = parseInt(data.loaded / data.total * 100, 10);
					$('#upload-dialog .progress .bar').css('width', percent + '%');
				});
			},
			always: function() {
				require(['documentr/dialog'], function() {
					$('#upload-dialog').hideModal();
					$('#upload-dialog .modal-footer a').setButtonDisabled(false);
					$('#upload-dialog .progress .bar').css('width', '0%');
				});
			},
			done: function(e, data) {
				window.location.reload();
			}
		});
	});
});

</sec:authorize>

</dt:pageJS>

<c:set var="pagePathUrl" value="${d:toUrlPagePath(pagePath)}"/>
<dt:breadcrumbs>
	<li><a href="<c:url value="/projects"/>"><spring:message code="title.projects"/></a> <span class="divider">/</span></li>
	<li><a href="<c:url value="/project/${projectName}"/>"><c:out value="${projectName}"/></a> <span class="divider">/</span></li>
	<li><a href="<c:url value="/page/${projectName}/${branchName}/home"/>"><c:out value="${branchName}"/></a> <span class="divider">/</span></li>
	<c:set var="hierarchy" value="${d:getPagePathHierarchy(projectName, branchName, pagePath)}"/>
	<c:forEach var="entry" items="${hierarchy}" varStatus="status">
		<c:if test="${!status.first}">
			<li><a href="<c:url value="/page/${projectName}/${branchName}/${d:toUrlPagePath(entry)}"/>"><c:out value="${d:getPageTitle(projectName, branchName, entry)}"/></a> <span class="divider">/</span></li>
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
					<th><spring:message code="title.actions"/></th>
				</tr>
			</thead>
			<tbody>
				<c:forEach var="attachment" items="${attachments}">
					<c:set var="metadata" value="${d:getAttachmentMetadata(projectName, branchName, pagePath, attachment)}"/>
					<tr>
						<td><a href="<c:url value="/attachment/${projectName}/${branchName}/${d:toUrlPagePath(pagePath)}/${attachment}"/>"><c:out value="${attachment}"/></a></td>
						<td><c:out value="${d:formatSize(metadata.size)}"/></td>
						<td><c:out value="${metadata.lastEditedBy}"/></td>
						<td><fmt:formatDate value="${metadata.lastEdited}" type="both" dateStyle="MEDIUM" timeStyle="SHORT"/></td>
						<td>
							<a href="<c:url value="/attachment/${projectName}/${branchName}/${d:toUrlPagePath(pagePath)}/${attachment}"><c:param name="download" value="true"/></c:url>" class="btn btn-mini" rel="nofollow"><spring:message code="button.download"/></a>
							<sec:authorize access="hasPagePermission(#projectName, #branchName, #pagePath, EDIT_PAGE)">
								<a href="javascript:void(showDeleteDialog('${attachment}'));" class="btn btn-mini"><spring:message code="button.delete"/>...</a>
							</sec:authorize>
						</td>
					</tr>
				</c:forEach>
			</tbody>
		</table>
	</c:when>
	<c:otherwise>
		<p><spring:message code="noAttachmentsFound"/></p>
	</c:otherwise>
</c:choose>

<p><spring:message code="dragFilesOntoPageToUpload"/></p>

<sec:authorize access="hasPagePermission(#projectName, #branchName, #pagePath, EDIT_PAGE)">
	<p>
	<a href="<c:url value="/attachment/create/${projectName}/${branchName}/${d:toUrlPagePath(pagePath)}"/>" class="btn"><i class="icon-plus"></i> <spring:message code="button.addAttachment"/></a>
	</p>

	<input type="file" name="file" style="display: none;"/>

	<div class="modal" id="upload-dialog" style="display: none;">
		<div class="modal-header">
			<button class="close" onclick="cancelUpload();">&#x00D7</button>
			<h3><spring:message code="title.upload"/></h3>
		</div>
		<div class="modal-body">
			<div class="progress">
				<div class="bar"></div>
			</div>
		</div>
		<div class="modal-footer">
			<a href="javascript:void(cancelUpload());" class="btn"><spring:message code="button.cancel"/></a>
		</div>
	</div>
</sec:authorize>

</dt:page>

</sec:authorize>
