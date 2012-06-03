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
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@ taglib prefix="sec" uri="http://www.springframework.org/security/tags" %>
<%@ taglib prefix="d" uri="http://documentr.org/tld/documentr" %>
<%@ taglib prefix="dt" tagdir="/WEB-INF/tags" %>

<sec:authorize access="hasApplicationPermission('ADMIN')">

<c:set var="projects" value="${d:listProjects()}"/>
<c:set var="roles" value="${d:listRoles()}"/>

<dt:headerJSFile uri="/js/zxcvbn-20120527.js"/>

<dt:headerJS>

function updatePasswordStrengthIndicator() {
	var result = zxcvbn($('#password1').val(), [ 'documentr' ]);

	$('#password1Error').remove();

	var indicator = $('#passwordStrengthIndicator');
	if (indicator.length == 0) {
		indicator = $('<span class="help-inline" id="passwordStrengthIndicator"><div class="progress password-strength-indicator"><div class="bar"></div></div></span>');
		$('#password1Fieldset').append(indicator);
	}

	indicator.find('.bar').width(((result.score + 1) * 20) + '%');
	indicator.removeClass('progress-success').removeClass('progress-warning').removeClass('progress-danger');
	if (result.score <= 1) {
		indicator.addClass('progress-danger');
	} else if (result.score <= 3) {
		indicator.addClass('progress-warning');
	} else {
		indicator.addClass('progress-success');
	}
}

function createAuthority(type, targetId, roleName) {
	return {
		type: type,
		targetId: targetId,
		roleName: roleName
	};
}

function updateAuthoritiesInForm() {
	var authoritiesStr = '';
	for (var i = 0; i < authorities.length; i++) {
		if (i > 0) {
			authoritiesStr = authoritiesStr + '|';
		}
		authoritiesStr = authoritiesStr + authorities[i].type + ':' + authorities[i].targetId + ':' + authorities[i].roleName;
	}
	$('#userForm').find('input:hidden[name="authorities"]').val(authoritiesStr);
}

function updateAuthorities() {
	updateAuthoritiesInForm();

	var newEls = new Array();

	var authorities = getAuthoritiesByType('APPLICATION');
	if (authorities.length > 0) {
		newEls.push($('<h4 data-documentr-role="true"><spring:message code="title.roleTarget.application"/></h4>'));
		var ulEl = $('<ul data-documentr-role="true"></ul>');
		for (var i = 0; i < authorities.length; i++) {
			ulEl.append('<li>' + authorities[i].roleName + ' (<a href="javascript:void(removeRole(\'' + authorities[i].type + '\', \'' + authorities[i].targetId + '\', \'' + authorities[i].roleName + '\'));"><spring:message code="button.remove"/></a>)</li>');
		}
		newEls.push(ulEl);
	}
	
	var authorities = getAuthoritiesByType('PROJECT');
	if (authorities.length > 0) {
		var targetId = null;
		var ulEl = null;
		for (var i = 0; i < authorities.length; i++) {
			if (authorities[i].targetId != targetId) {
				newEls.push(ulEl);
			
				newEls.push($('<h4 data-documentr-role="true"><spring:message code="title.roleTarget.project"/>: ' + authorities[i].targetId + '</h4>'));
				ulEl = $('<ul data-documentr-role="true"></ul>');
			}
			ulEl.append('<li>' + authorities[i].roleName + ' (<a href="javascript:void(removeRole(\'' + authorities[i].type + '\', \'' + authorities[i].targetId + '\', \'' + authorities[i].roleName + '\'));"><spring:message code="button.remove"/></a>)</li>');
			
			targetId = authorities[i].targetId;
		}
		newEls.push(ulEl);
	}
	
	var authorities = getAuthoritiesByType('BRANCH');
	if (authorities.length > 0) {
		var targetId = null;
		var ulEl = null;
		for (var i = 0; i < authorities.length; i++) {
			if (authorities[i].targetId != targetId) {
				newEls.push(ulEl);
			
				newEls.push($('<h4 data-documentr-role="true"><spring:message code="title.roleTarget.branch"/>: ' + authorities[i].targetId + '</h4>'));
				ulEl = $('<ul data-documentr-role="true"></ul>');
			}
			ulEl.append('<li>' + authorities[i].roleName + ' (<a href="javascript:void(removeRole(\'' + authorities[i].type + '\', \'' + authorities[i].targetId + '\', \'' + authorities[i].roleName + '\'));"><spring:message code="button.remove"/></a>)</li>');
			
			targetId = authorities[i].targetId;
		}
		newEls.push(ulEl);
	}
	
	if (newEls.length == 0) {
		newEls.push($('<p data-documentr-role="true"><spring:message code="noRolesAssigned"/></p>'));
	}
	
	var permissionsEl = $('#userPermissions');

	$('[data-documentr-role]').remove();

	var restEls = permissionsEl.contents();
	restEls.detach();
	for (var i = 0; i < newEls.length; i++) {
		permissionsEl.append(newEls[i]);
	}
	permissionsEl.append(restEls);
}

function getAuthoritiesByType(type) {
	var result = new Array();
	for (var i = 0; i < authorities.length; i++) {
		if (authorities[i].type == type) {
			result.push(authorities[i]);
		}
	}
	return result;
}

function showAddRoleDialog() {
	<c:choose>
		<c:when test="${!empty roles}">
			$('#addRoleForm').each(function() {
				this.reset();
			});
			$('#addRoleDialog').showModal({backdrop: true, keyboard: true});
		</c:when>
		<c:otherwise>
			window.alert('<spring:message code="noRolesConfigured"/>');
		</c:otherwise>
	</c:choose>
}

function addRole() {
	var formEl = $('#addRoleForm');
	var type = formEl.find('input:radio:checked').val();
	var targetId;
	if (type == 'APPLICATION') {
		targetId = 'application';
	} else if (type == 'PROJECT') {
		targetId = formEl.find('select[name="projectName"]').val();
	} else if (type == 'BRANCH') {
		targetId = formEl.find('select[name="branchName"]').val();
	}
	var roleName = formEl.find('select[name="roleName"]').val();

	$('#addRoleDialog').modal('hide');

	authorities.push(createAuthority(type, targetId, roleName));
	<%-- TODO: sort authorities again like UserStore does --%>
	
	updateAuthorities();
}

function removeRole(type, targetId, roleName) {
	var newAuthorities = new Array();
	for (var i = 0; i < authorities.length; i++) {
		if ((authorities[i].type != type) ||
			(authorities[i].targetId != targetId) ||
			(authorities[i].roleName != roleName)) {
		
			newAuthorities.push(authorities[i]);
		}
	}
	authorities = newAuthorities;
	updateAuthorities();
}

var authorities = new Array();

<c:if test="${!empty userForm.loginName}">
	<c:set var="authorities" value="${d:getUserAuthorities(userForm.loginName)}"/>
	<c:forEach var="rga" items="${authorities}">
		authorities.push(createAuthority('<c:out value="${rga.target.type}"/>', '<c:out value="${rga.target.targetId}"/>', '<c:out value="${rga.roleName}"/>'));
	</c:forEach>
</c:if>

$(function() {
	$('#userDetailsTabs a').click(function(e) {
		e.preventDefault();
		$(this).tab('show');
	});
	
	updateAuthorities();
	
	<c:if test="${empty projects}">
		var addRoleFormEl = $('#addRoleForm');
		addRoleFormEl.find('input:radio[value="PROJECT"]').attr('disabled', 'true');
		addRoleFormEl.find('select[name="projectName"]').attr('disabled', 'true');
		addRoleFormEl.find('input:radio[value="BRANCH"]').attr('disabled', 'true');
		addRoleFormEl.find('select[name="branchName"]').attr('disabled', 'true');
	</c:if>
	
	<c:if test="${userForm.loginName eq '_anonymous'}">
		var userFormEl = $('#userForm');
		userFormEl.find('input[name="password1"]').attr('disabled', 'true');
		userFormEl.find('input[name="password2"]').attr('disabled', 'true');
		userFormEl.find('input[name="email"]').attr('disabled', 'true');
		userFormEl.find('input[name="disabled"]').attr('disabled', 'true');
	</c:if>
});

</dt:headerJS>

<dt:breadcrumbs>
	<li><a href="<c:url value="/users"/>"><spring:message code="title.users"/></a> <span class="divider">/</span></li>
	<li class="active"><spring:message code="title.editUser"/></li>
</dt:breadcrumbs>

<dt:page>

<div class="page-header"><h1><spring:message code="title.accountManagement"/></h1></div>

<ul class="nav nav-tabs">
	<li class="active"><a href="<c:url value="/users"/>"><spring:message code="title.users"/></a></li>
	<li><a href="<c:url value="/roles"/>"><spring:message code="title.roles"/></a></li>
</ul>

<h2><spring:message code="title.editUser"/></h2>

<p>
<c:set var="action"><c:url value="/user/save"/></c:set>
<form:form commandName="userForm" action="${action}" method="POST" cssClass="well form-horizontal">
	<ul id="userDetailsTabs" class="nav nav-tabs">
		<li class="active"><a href="#userDetails"><spring:message code="title.details"/></a></li>
		<li><a href="#userPermissions"><spring:message code="title.roles"/></a></li>
	</ul>

	<div class="tab-content">
		<div class="tab-pane active" id="userDetails">
			<fieldset>
				<c:set var="errorText"><form:errors path="loginName"/></c:set>
				<div class="control-group <c:if test="${!empty errorText}">error</c:if>">
					<form:label path="loginName" cssClass="control-label"><spring:message code="label.loginName"/>:</form:label>
					<c:choose>
						<c:when test="${(!empty userForm.loginName) && (empty errorText)}">
							<form:hidden path="loginName"/>
							<form:input path="loginName" cssClass="input-xlarge disabled" disabled="true"/>
						</c:when>
						<c:otherwise>
							<form:input path="loginName" cssClass="input-xlarge"/>
						</c:otherwise>
					</c:choose>
					<c:if test="${!empty errorText}"><span class="help-inline"><c:out value="${errorText}" escapeXml="false"/></span></c:if>
				</div>
				<c:set var="errorText1"><form:errors path="password1"/></c:set>
				<c:set var="errorText2"><form:errors path="password2"/></c:set>
				<div id="password1Fieldset" class="control-group <c:if test="${!empty errorText1 or !empty errorText2}">error</c:if>">
					<form:label path="password1" cssClass="control-label"><spring:message code="label.password"/>:</form:label>
					<form:password path="password1" cssClass="input-xlarge" autocomplete="off" onkeyup="updatePasswordStrengthIndicator()"/>
					<c:if test="${!empty errorText1}"><span class="help-inline" id="password1Error"><c:out value="${errorText1}" escapeXml="false"/></span></c:if>
				</div>
				<div class="control-group <c:if test="${!empty errorText1 or !empty errorText2}">error</c:if>">
					<form:label path="password2" cssClass="control-label"><spring:message code="label.repeatPassword"/>:</form:label>
					<form:password path="password2" cssClass="input-xlarge" autocomplete="off"/>
					<c:if test="${!empty errorText2}"><span class="help-inline"><c:out value="${errorText2}" escapeXml="false"/></span></c:if>
				</div>
				<c:set var="errorText"><form:errors path="email"/></c:set>
				<div class="control-group <c:if test="${!empty errorText}">error</c:if>">
					<form:label path="email" cssClass="control-label"><spring:message code="label.email"/>:</form:label>
					<form:input path="email" cssClass="input-xlarge"/>
					<c:if test="${!empty errorText}"><span class="help-inline"><c:out value="${errorText}" escapeXml="false"/></span></c:if>
				</div>
				<div class="control-group">
					<form:label path="disabled" cssClass="checkbox">
						<form:checkbox path="disabled"/>
						<spring:message code="label.accountDisabled"/>
					</form:label>
				</div>
			</fieldset>
		</div>
		
		<div class="tab-pane" id="userPermissions">
			<input type="hidden" name="authorities"/>
			<p>
				<a href="javascript:void(showAddRoleDialog());" class="btn"><spring:message code="button.addRole"/></a>
			</p>
		</div>
	</div>

	<div class="form-actions">
		<input type="submit" class="btn btn-primary" value="<spring:message code="button.save"/>"/>
		<a href="<c:url value="/users"/>" class="btn"><spring:message code="button.cancel"/></a>
	</div>
</form:form>
</p>

<div class="modal" id="addRoleDialog" style="display: none;">
	<div class="modal-header">
		<button class="close" onclick="$('#addRoleDialog').modal('hide');">×</button>
		<h3><spring:message code="title.addRole"/></h3>
	</div>
	<div class="modal-body">
		<form id="addRoleForm" class="form-horizontal">
			<fieldset class="control-group">
				<label class="control-label"><spring:message code="label.targetObject"/>:</label>
				<div class="controls">
					<label class="radio">
						<input type="radio" name="targetType" value="APPLICATION" checked="checked"/>
						<spring:message code="label.roleTarget.application"/>
					</label>
					<span class="radio-label">
						<label class="inline">
							<input type="radio" name="targetType" value="PROJECT"/>
							<spring:message code="label.roleTarget.project"/>:
						</label>
						<select name="projectName" onmousedown="$('#addRoleForm').find('input:radio[value=&quot;PROJECT&quot;]').each(function() { this.checked = 'checked'; });">
							<c:forEach var="project" items="${projects}">
								<option value="<c:out value="${project}"/>"><c:out value="${project}"/></option>
							</c:forEach>
						</select>
					</span>
					<span class="radio-label">
						<label class="inline">
							<input type="radio" name="targetType" value="BRANCH"/>
							<spring:message code="label.roleTarget.branch"/>:
						</label>
						<select name="branchName" onmousedown="$('#addRoleForm').find('input:radio[value=&quot;BRANCH&quot;]').each(function() { this.checked = 'checked'; });">
							<c:forEach var="project" items="${projects}">
								<c:set var="branches" value="${d:listProjectBranches(project)}"/>
								<c:forEach var="branch" items="${branches}">
									<option value="<c:out value="${project}/${branch}"/>"><c:out value="${project}/${branch}"/></option>
								</c:forEach>
							</c:forEach>
						</select>
					</span>
				</div>
				
				<label class="control-label"><spring:message code="label.role"/>:</label>
				<div class="controls">
					<select name="roleName">
						<c:forEach var="role" items="${roles}">
							<option value="<c:out value="${role}"/>"><c:out value="${role}"/></option>
						</c:forEach>
					</select>
				</div>
			</fieldset>
		</form>
	</div>
	<div class="modal-footer">
		<a href="javascript:void(addRole());" class="btn btn-primary"><spring:message code="button.add"/></a>
		<a href="javascript:void($('#addRoleDialog').modal('hide'));" class="btn"><spring:message code="button.cancel"/></a>
	</div>
</div>

</dt:page>

</sec:authorize>
