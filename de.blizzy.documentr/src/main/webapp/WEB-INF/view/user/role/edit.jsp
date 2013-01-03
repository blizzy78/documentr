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
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@ taglib prefix="sec" uri="http://www.springframework.org/security/tags" %>
<%@ taglib prefix="d" uri="http://documentr.org/tld/documentr" %>
<%@ taglib prefix="dt" tagdir="/WEB-INF/tags" %>

<sec:authorize access="hasApplicationPermission(ADMIN)">

<dt:pageJS>

var dirty = false;

function updatePermissions() {
	var controlsBox = $('#permissionControls');
	var adminCheckbox = controlsBox.find('input:checkbox:checked[value="ADMIN"]');
	var nonAdminCheckboxes = controlsBox.find('input:checkbox[value!="ADMIN"]');
	if (adminCheckbox.length === 0) {
		nonAdminCheckboxes.removeAttr('disabled');
	} else {
		nonAdminCheckboxes.attr('disabled', 'disabled');
	}
}

<c:if test="${!empty roleForm.originalName}">
function showDeleteDialog() {
	<c:set var="lastAdminRole" value="${false}"/>
	<sec:authorize access="isLastAdminRole('${roleForm.originalName}')">
		<c:set var="lastAdminRole" value="${true}"/>
	</sec:authorize>
	require(['documentr/dialog'], function(dialog) {
		<c:choose>
			<c:when test="${lastAdminRole}">
				dialog.openMessageDialog('<spring:message code="title.deleteRole"/>',
					"<spring:message code="cannotDeleteRoleXBecauseIsLastAdmin" arguments="__DUMMY__"/>".replace(/__DUMMY__/, '<c:out value="${roleForm.originalName}"/>'), [
						{
							text: '<spring:message code="button.close"/>',
							cancel: true
						}
					]);
			</c:when>
			<c:otherwise>
				dialog.openMessageDialog('<spring:message code="title.deleteRole"/>',
					"<spring:message code="deleteRoleX" arguments="__DUMMY__"/>".replace(/__DUMMY__/, '<c:out value="${roleForm.originalName}"/>'), [
						{
							text: '<spring:message code="button.delete"/>',
							href: '<c:url value="/role/delete/${roleForm.originalName}"/>',
							type: 'danger'
						},
						{
							text: '<spring:message code="button.cancel"/>',
							cancel: true
						}
					]);
			</c:otherwise>
		</c:choose>
	});
}
</c:if>

function clearDirty() {
	dirty = false;
}

$(function() {
	updatePermissions();
	
	$(window).bind('beforeunload', function() {
		if (dirty) {
			return '<spring:message code="confirmLeavePage"/>';
		}
	});
	
	$('#roleForm input').on('keypress change', function() {
		dirty = true;
	});
});

</dt:pageJS>

<dt:breadcrumbs>
	<li><a href="<c:url value="/roles"/>"><spring:message code="title.roles"/></a> <span class="divider">/</span></li>
	<li class="active"><spring:message code="title.editRole"/></li>
</dt:breadcrumbs>

<dt:pageTitle><spring:message code="title.editRole"/></dt:pageTitle>

<dt:page>

<div class="page-header"><h1><spring:message code="title.accountManagement"/></h1></div>

<ul class="nav nav-tabs">
	<li><a href="<c:url value="/users"/>"><spring:message code="title.users"/></a></li>
	<li class="active"><a href="<c:url value="/roles"/>"><spring:message code="title.roles"/></a></li>
</ul>

<h2><spring:message code="title.editRole"/></h2>

<c:set var="action"><c:url value="/role/save"/></c:set>
<form:form commandName="roleForm" action="${action}" method="POST" cssClass="well form-horizontal" onsubmit="clearDirty(); return true;">
	<fieldset>
		<input type="hidden" name="originalName" value="<c:out value="${roleForm.originalName}"/>"/>
	
		<c:set var="errorText"><form:errors path="name"/></c:set>
		<div class="control-group <c:if test="${!empty errorText}">error</c:if>">
			<form:label path="name" cssClass="control-label"><spring:message code="label.roleName"/>:</form:label>
			<div class="controls">
				<form:input path="name" cssClass="input-xlarge"/>
				<c:if test="${!empty errorText}"><span class="help-inline"><c:out value="${errorText}" escapeXml="false"/></span></c:if>
			</div>
		</div>
		<div class="control-group">
			<form:label path="permissions" cssClass="control-label"><spring:message code="label.permissions"/>:</form:label>
			<div id="permissionControls" class="controls">
				<label class="checkbox">
					<form:checkbox path="permissions" value="ADMIN" onchange="updatePermissions()"/>
					<strong><spring:message code="permission.admin.name"/></strong>
					<p class="help-block"><spring:message code="permission.admin.description.html" htmlEscape="false"/></p>
				</label>
				<label class="checkbox">
					<form:checkbox path="permissions" value="VIEW"/>
					<strong><spring:message code="permission.view.name"/></strong>
					<p class="help-block"><spring:message code="permission.view.description.html" htmlEscape="false"/></p>
				</label>
				<label class="checkbox">
					<form:checkbox path="permissions" value="EDIT_PROJECT"/>
					<strong><spring:message code="permission.editProject.name"/></strong>
					<p class="help-block"><spring:message code="permission.editProject.description.html" htmlEscape="false"/></p>
				</label>
				<label class="checkbox">
					<form:checkbox path="permissions" value="EDIT_BRANCH"/>
					<strong><spring:message code="permission.editBranch.name"/></strong>
					<p class="help-block"><spring:message code="permission.editBranch.description.html" htmlEscape="false"/></p>
				</label>
				<label class="checkbox">
					<form:checkbox path="permissions" value="EDIT_PAGE"/>
					<strong><spring:message code="permission.editPage.name"/></strong>
					<p class="help-block"><spring:message code="permission.editPage.description.html" htmlEscape="false"/></p>
				</label>
			</div>
		</div>
		<div class="form-actions">
			<input type="submit" class="btn btn-primary" value="<spring:message code="button.save"/>"/>
			<c:if test="${!empty roleForm.originalName}">
				<button onclick="showDeleteDialog(); return false;" class="btn btn-warning"><spring:message code="button.delete"/></button>
			</c:if>
			<a href="<c:url value="/roles"/>" class="btn"><spring:message code="button.cancel"/></a>
		</div>
	</fieldset>
</form:form>

</dt:page>

</sec:authorize>
