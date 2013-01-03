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

<c:set var="projects" value="${d:listProjects()}"/>
<c:set var="roles" value="${d:listRoles()}"/>

<dt:pageJS>

function updatePasswordStrengthIndicator() {
	require(['zxcvbn'], function(zxcvbn) {
		var result = zxcvbn($('#password1').val(), [ 'documentr' ]);
	
		$('#password1Error').remove();
	
		var indicator = $('#passwordStrengthIndicator');
		if (indicator.length === 0) {
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
	});
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
	$('#userForm input:hidden[name="authorities"]').val(authoritiesStr);
}

function updateAuthorities() {
	updateAuthoritiesInForm();

	var newEls = [];

	var authorities = getAuthoritiesByType('APPLICATION');
	if (authorities.length > 0) {
		newEls.push($('<h4 data-documentr-role="true"><spring:message code="title.roleTarget.application"/></h4>'));
		var ulEl = $('<ul data-documentr-role="true"></ul>');
		for (var i = 0; i < authorities.length; i++) {
			ulEl.append('<li>' + authorities[i].roleName + ' (<a href="javascript:;" onclick="removeRole(\'' + authorities[i].type + '\', \'' + authorities[i].targetId + '\', \'' + authorities[i].roleName + '\'); return false;"><spring:message code="button.remove"/></a>)</li>');
		}
		newEls.push(ulEl);
	}
	
	var authorities = getAuthoritiesByType('PROJECT');
	if (authorities.length > 0) {
		var targetId = null;
		var ulEl = null;
		for (var i = 0; i < authorities.length; i++) {
			if (authorities[i].targetId !== targetId) {
				newEls.push(ulEl);
			
				newEls.push($('<h4 data-documentr-role="true"><spring:message code="title.roleTarget.project"/>: ' + authorities[i].targetId + '</h4>'));
				ulEl = $('<ul data-documentr-role="true"></ul>');
			}
			ulEl.append('<li>' + authorities[i].roleName + ' (<a href="javascript:;" onclick="removeRole(\'' + authorities[i].type + '\', \'' + authorities[i].targetId + '\', \'' + authorities[i].roleName + '\'); return false;"><spring:message code="button.remove"/></a>)</li>');
			
			targetId = authorities[i].targetId;
		}
		newEls.push(ulEl);
	}
	
	var authorities = getAuthoritiesByType('BRANCH');
	if (authorities.length > 0) {
		var targetId = null;
		var ulEl = null;
		for (var i = 0; i < authorities.length; i++) {
			if (authorities[i].targetId !== targetId) {
				newEls.push(ulEl);
			
				newEls.push($('<h4 data-documentr-role="true"><spring:message code="title.roleTarget.branch"/>: ' + authorities[i].targetId + '</h4>'));
				ulEl = $('<ul data-documentr-role="true"></ul>');
			}
			ulEl.append('<li>' + authorities[i].roleName + ' (<a href="javascript:;" onclick="removeRole(\'' + authorities[i].type + '\', \'' + authorities[i].targetId + '\', \'' + authorities[i].roleName + '\'); return false;"><spring:message code="button.remove"/></a>)</li>');
			
			targetId = authorities[i].targetId;
		}
		newEls.push(ulEl);
	}
	
	if (newEls.length === 0) {
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
	var result = [];
	for (var i = 0; i < authorities.length; i++) {
		if (authorities[i].type === type) {
			result.push(authorities[i]);
		}
	}
	return result;
}

function showAddRoleDialog() {
	require(['documentr/dialog'], function() {
		<c:choose>
			<c:when test="${!empty roles}">
				$('#addRoleForm').each(function() {
					this.reset();
				});
				$('#addRoleDialog').showModal();
			</c:when>
			<c:otherwise>
				window.alert('<spring:message code="noRolesConfigured"/>');
			</c:otherwise>
		</c:choose>
	});
}

function addRole() {
	var formEl = $('#addRoleForm');
	var type = formEl.find('input:radio:checked').val();
	var targetId;
	if (type === 'APPLICATION') {
		targetId = 'application';
	} else if (type === 'PROJECT') {
		targetId = formEl.find('select[name="projectName"]').val();
	} else if (type === 'BRANCH') {
		targetId = formEl.find('select[name="branchName"]').val();
	}
	var roleName = formEl.find('select[name="roleName"]').val();

	$('#addRoleDialog').hideModal();

	authorities.push(createAuthority(type, targetId, roleName));
	<%-- TODO: sort authorities again like UserStore does --%>
	
	updateAuthorities();
	dirty = true;
}

function removeRole(type, targetId, roleName) {
	var newAuthorities = [];
	for (var i = 0; i < authorities.length; i++) {
		if ((authorities[i].type !== type) ||
			(authorities[i].targetId !== targetId) ||
			(authorities[i].roleName !== roleName)) {
		
			newAuthorities.push(authorities[i]);
		}
	}
	authorities = newAuthorities;
	updateAuthorities();
	dirty = true;
}

<c:if test="${(!empty userForm.originalLoginName) and (userForm.originalLoginName ne '_anonymous')}">
function showDeleteDialog() {
	require(['documentr/dialog'], function(dialog) {
		<sec:authorize access="isAdmin('${userForm.originalLoginName}')">
			dialog.openMessageDialog('<spring:message code="title.deleteUser"/>',
				"<spring:message code="cannotDeleteUserXBecauseIsAdmin" arguments="__DUMMY__"/>".replace(/__DUMMY__/, '<c:out value="${userForm.originalLoginName}"/>'), [
				{
					text: '<spring:message code="button.close"/>',
					cancel: true
				}
			]);
		</sec:authorize>
		<sec:authorize access="!isAdmin('${userForm.originalLoginName}')">
			dialog.openMessageDialog('<spring:message code="title.deleteUser"/>',
				"<spring:message code="deleteUserX" arguments="__DUMMY__"/>".replace(/__DUMMY__/, '<c:out value="${userForm.originalLoginName}"/>'), [
				{
					text: '<spring:message code="button.delete"/>',
					href: '<c:url value="/user/delete/${userForm.originalLoginName}"/>',
					type: 'danger'
				},
				{
					text: '<spring:message code="button.cancel"/>',
					cancel: true
				}
			]);
		</sec:authorize>
	});
}
</c:if>

function clearDirty() {
	dirty = false;
}

var authorities = [];
var dirty = false;

<c:if test="${!empty userForm.originalLoginName}">
	<c:set var="authorities" value="${d:getUserAuthorities(userForm.originalLoginName)}"/>
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
	
	<c:if test="${userForm.originalLoginName eq '_anonymous'}">
		var userFormEl = $('#userForm');
		userFormEl.find('input[name="password1"]').attr('disabled', 'true');
		userFormEl.find('input[name="password2"]').attr('disabled', 'true');
		userFormEl.find('input[name="email"]').attr('disabled', 'true');
		userFormEl.find('input[name="disabled"]').attr('disabled', 'true');
	</c:if>
	
	$(window).bind('beforeunload', function() {
		if (dirty) {
			return '<spring:message code="confirmLeavePage"/>';
		}
	});
	
	$('#userForm input').on('keypress change', function() {
		dirty = true;
	});
});

</dt:pageJS>

<dt:breadcrumbs>
	<li><a href="<c:url value="/users"/>"><spring:message code="title.users"/></a> <span class="divider">/</span></li>
	<li class="active"><spring:message code="title.editUser"/></li>
</dt:breadcrumbs>

<dt:pageTitle><spring:message code="title.editUser"/></dt:pageTitle>

<dt:page>

<div class="page-header"><h1><spring:message code="title.accountManagement"/></h1></div>

<ul class="nav nav-tabs">
	<li class="active"><a href="<c:url value="/users"/>"><spring:message code="title.users"/></a></li>
	<li><a href="<c:url value="/roles"/>"><spring:message code="title.roles"/></a></li>
</ul>

<h2><spring:message code="title.editUser"/></h2>

<c:set var="action"><c:url value="/user/save"/></c:set>
<form:form commandName="userForm" action="${action}" method="POST" cssClass="well form-horizontal" onsubmit="clearDirty(); return true;">
	<ul id="userDetailsTabs" class="nav nav-tabs">
		<li class="active"><a href="#userDetails"><spring:message code="title.details"/></a></li>
		<li><a href="#userPermissions"><spring:message code="title.roles"/></a></li>
	</ul>

	<div class="tab-content">
		<div class="tab-pane active" id="userDetails">
			<fieldset>
				<input type="hidden" name="originalLoginName" value="${userForm.originalLoginName}"/>
			
				<c:set var="errorText"><form:errors path="loginName"/></c:set>
				<div class="control-group <c:if test="${!empty errorText}">error</c:if>">
					<form:label path="loginName" cssClass="control-label"><spring:message code="label.loginName"/>:</form:label>
					<c:choose>
						<c:when test="${userForm.originalLoginName eq '_anonymous'}">
							<form:hidden path="loginName"/>
							<input type="text" value="(<spring:message code="label.anonymousUser"/>)" class="input-xlarge disabled" disabled="true"/>
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
				<button onclick="showAddRoleDialog(); return false;" class="btn"><spring:message code="button.addRole"/></button>
			</p>
		</div>
	</div>

	<div class="form-actions">
		<input type="submit" class="btn btn-primary" value="<spring:message code="button.save"/>"/>
		<c:if test="${(!empty userForm.originalLoginName) and (userForm.originalLoginName ne '_anonymous')}">
			<button onclick="showDeleteDialog(); return false;" class="btn btn-warning"><spring:message code="button.delete"/></button>
		</c:if>
		<a href="<c:url value="/users"/>" class="btn"><spring:message code="button.cancel"/></a>
	</div>
</form:form>

<div class="modal" id="addRoleDialog" style="display: none;">
	<div class="modal-header">
		<button class="close" onclick="$('#addRoleDialog').hideModal();">&#x00D7</button>
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
						<select name="projectName" onmousedown="$('#addRoleForm input:radio[value=&quot;PROJECT&quot;]').each(function() { this.checked = 'checked'; });">
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
						<select name="branchName" onmousedown="$('#addRoleForm input:radio[value=&quot;BRANCH&quot;]').each(function() { this.checked = 'checked'; });">
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
		<button onclick="addRole(); return false;" class="btn btn-primary"><spring:message code="button.add"/></button>
		<button onclick="$('#addRoleDialog').hideModal(); return false;" class="btn"><spring:message code="button.cancel"/></button>
	</div>
</div>

</dt:page>

</sec:authorize>
