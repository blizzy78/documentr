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
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@ taglib prefix="sec" uri="http://www.springframework.org/security/tags" %>
<%@ taglib prefix="d" uri="http://documentr.org/tld/documentr" %>
<%@ taglib prefix="dt" tagdir="/WEB-INF/tags" %>

<sec:authorize access="hasPagePermission(#projectName, #branchName, #pagePath, EDIT_PAGE)">

<c:set var="pagePathUrl" value="${d:toUrlPagePath(pagePath)}"/>

<dt:pageJS>

var jqXHRs = [];

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
			url: '<c:url value="/attachment/saveViaJson/${projectName}/${branchName}/${pagePathUrl}/json"/>',
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
				window.location.href = '<c:url value="/attachment/list/${projectName}/${branchName}/${pagePathUrl}"/>';
			}
		});
	});
});

</dt:pageJS>

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
	<li class="active"><spring:message code="title.editAttachment"/></li>
</dt:breadcrumbs>

<dt:pageTitle><spring:message code="title.editAttachment"/></dt:pageTitle>

<dt:page>

<div class="page-header"><h1><spring:message code="title.editAttachment"/></h1></div>

<form action="<c:url value="/attachment/save/${projectName}/${branchName}/${pagePathUrl}"/>"
	method="post" enctype="multipart/form-data" class="well form-inline">

	<fieldset>
		<input type="hidden" name="projectName" value="<c:out value="${projectName}"/>"/>
		<input type="hidden" name="branchName" value="<c:out value="${branchName}"/>"/>
		<input type="hidden" name="pagePath" value="<c:out value="${pagePath}"/>"/>
	
		<div class="control-group">
			<label class="control-label">File:</label>
			<input type="file" name="file" class="input-file"/>
		</div>
		<div class="form-actions">
			<input type="submit" class="btn btn-primary" value="<spring:message code="button.save"/>"/>
			<a href="<c:url value="/page/${projectName}/${branchName}/${pagePathUrl}"/>" class="btn"><spring:message code="button.cancel"/></a>
		</div>
	</fieldset>	
</form>

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

</dt:page>

</sec:authorize>
