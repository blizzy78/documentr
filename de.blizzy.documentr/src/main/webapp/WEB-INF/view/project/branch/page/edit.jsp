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
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@ taglib prefix="sec" uri="http://www.springframework.org/security/tags" %>
<%@ taglib prefix="d" uri="http://documentr.org/tld/documentr" %>
<%@ taglib prefix="dt" tagdir="/WEB-INF/tags" %>

<sec:authorize access="(#pageForm.path == null) ?
	hasBranchPermission(#pageForm.projectName, #pageForm.branchName, 'EDIT_PAGE') :
	hasPagePermission(#pageForm.projectName, #pageForm.branchName, #pageForm.path, 'EDIT_PAGE')">

<c:choose>
	<c:when test="${!empty pageForm.path}"><c:set var="hierarchyPagePath" value="${pageForm.path}"/></c:when>
	<c:when test="${!empty pageForm.parentPagePath}"><c:set var="hierarchyPagePath" value="${pageForm.parentPagePath}"/></c:when>
	<c:otherwise><c:set var="hierarchyPagePath" value="home"/></c:otherwise>
</c:choose>

<dt:headerJS>

<c:if test="${empty pageForm.path}">
$(function() {
	var el = $('#pageForm').find('#title');
	el.blur(function() {
		var pinPathButton = $('#pinPathButton');
		if (!pinPathButton.hasClass('active')) {
			var fieldset = $('#pathFieldset');
			fieldset.removeClass('warning').removeClass('error');
			$('#pathExistsWarning').remove();
	
			var value = el.val();
			if (value.length > 0) {
				$.ajax({
					url: '<c:url value="/page/generateName/${pageForm.projectName}/${pageForm.branchName}/${d:toURLPagePath(hierarchyPagePath)}/json"/>',
					type: 'POST',
					dataType: 'json',
					data: {
						title: value
					},
					success: function(result) {
						$('#pageForm').find('#path').val(result.path);
						if (result.exists) {
							fieldset.addClass('warning');
							fieldset.append($('<span id="pathExistsWarning" class="help-inline">' +
								'<spring:message code="page.path.exists"/></span>'));
						}
						$('#pinPathButton').removeClass('disabled').removeClass('active');
					}
				});
			}
		}
	});
});
</c:if>

function showPreview() {
	var textEl = $('#pageForm').find('#text');
	$.ajax({
		url: '<c:url value="/page/markdownToHTML/${pageForm.projectName}/${pageForm.branchName}/json"/>',
		type: 'POST',
		dataType: 'json',
		data: {
			pagePath: '<c:out value="${hierarchyPagePath}"/>',
			markdown: textEl.val()
		},
		success: function(result) {
			$('#previewText').html(result.html);
			$('#preview').showModal({
				backdrop: true,
				keyboard: true
			});
			prettyPrint();
		}
	});
}

</dt:headerJS>

<dt:breadcrumbs>
	<li><a href="<c:url value="/projects"/>"><spring:message code="title.projects"/></a> <span class="divider">/</span></li>
	<li><a href="<c:url value="/project/${pageForm.projectName}"/>"><c:out value="${pageForm.projectName}"/></a> <span class="divider">/</span></li>
	<li><a href="<c:url value="/page/${pageForm.projectName}/${pageForm.branchName}/home"/>"><c:out value="${pageForm.branchName}"/></a> <span class="divider">/</span></li>
	<c:set var="hierarchy" value="${d:getPagePathHierarchy(pageForm.projectName, pageForm.branchName, hierarchyPagePath)}"/>
	<c:forEach var="entry" items="${hierarchy}" varStatus="status">
		<c:if test="${!status.first}">
			<li><a href="<c:url value="/page/${pageForm.projectName}/${pageForm.branchName}/${d:toURLPagePath(entry)}"/>"><c:out value="${d:getPageTitle(pageForm.projectName, pageForm.branchName, entry)}"/></a> <span class="divider">/</span></li>
		</c:if>
	</c:forEach>
	<li class="active"><spring:message code="title.editPage"/></li>
</dt:breadcrumbs>

<dt:pageTitle><spring:message code="title.editPage"/></dt:pageTitle>

<dt:page>

<div class="page-header"><h1><spring:message code="title.editPage"/></h1></div>

<p>
<c:set var="action"><c:url value="/page/save/${pageForm.projectName}/${pageForm.branchName}"/></c:set>
<form:form commandName="pageForm" action="${action}" method="POST" cssClass="well form-horizontal">
	<fieldset>
		<form:hidden path="parentPagePath"/>
		<c:set var="errorText"><form:errors path="title"/></c:set>
		<div class="control-group <c:if test="${!empty errorText}">error</c:if>">
			<form:label path="title" cssClass="control-label"><spring:message code="label.title"/>:</form:label>
			<form:input path="title" cssClass="input-xlarge"/>
			<c:if test="${!empty errorText}"><span class="help-inline"><c:out value="${errorText}" escapeXml="false"/></span></c:if>
		</div>
		<div id="pathFieldset" class="control-group">
			<form:hidden path="path"/>
			<form:label path="path" cssClass="control-label"><spring:message code="label.pathGeneratedAutomatically"/>:</form:label>
			<form:input path="path" cssClass="input-xlarge disabled" disabled="true"/>
			<c:if test="${empty pageForm.path}">
				<a id="pinPathButton" class="btn disabled" data-toggle="button" href="javascript:;" title="<spring:message code="button.pinPath"/>"><i class="icon-lock"></i></a>
			</c:if>
		</div>
		<div class="control-group">
			<form:label path="text" cssClass="control-label"><spring:message code="label.contents"/>:</form:label>
			<form:textarea path="text" cssClass="span11 code" rows="20"/>
			<a href="javascript:showPreview();" class="btn" title="<spring:message code="button.showPreview"/>"><i class="icon-eye-open"></i></a>
		</div>
		<div class="control-group">
			<form:label path="viewRestrictionRole" cssClass="control-label"><spring:message code="label.visibleForRole"/>:</form:label>
			<form:select path="viewRestrictionRole">
				<form:option value="">(<spring:message code="everyone"/>)</form:option>
				<c:set var="roles" value="${d:listRoles()}"/>
				<form:options items="${roles}"/>
			</form:select>
		</div>
		<div class="form-actions">
			<input type="submit" class="btn btn-primary" value="<spring:message code="button.save"/>"/>
			<a href="<c:url value="/page/${pageForm.projectName}/${pageForm.branchName}/${d:toURLPagePath(hierarchyPagePath)}"/>" class="btn"><spring:message code="button.cancel"/></a>
		</div>
	</fieldset>
</form:form>
</p>

<div class="modal modal-wide" id="preview" style="display: none;">
	<div class="modal-header">
		<button class="close" onclick="$('#preview').hideModal();">×</button>
		<h3><spring:message code="title.pagePreview"/></h3>
	</div>
	<div class="modal-body" id="previewText"></div>
	<div class="modal-footer">
		<a href="javascript:void($('#preview').hideModal());" class="btn"><spring:message code="button.close"/></a>
	</div>
</div>

</dt:page>

</sec:authorize>
