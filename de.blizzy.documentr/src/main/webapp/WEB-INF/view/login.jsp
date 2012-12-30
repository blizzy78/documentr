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
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@ taglib prefix="dt" tagdir="/WEB-INF/tags" %>

<dt:breadcrumbs>
	<li class="active"><spring:message code="title.login"/></li>
</dt:breadcrumbs>

<dt:pageTitle><spring:message code="title.login"/></dt:pageTitle>

<dt:page>

<center>

<c:if test="${!empty errorMessage}">
	<p class="text-error">
		<c:out value="${errorMessage}"/>
	</p>
</c:if>

<form action="<c:url value="/j_spring_security_check"/>" method="POST" class="well form-horizontal loginForm">
	<fieldset>
		<div class="control-group">
			<label class="control-label" for="j_username"><spring:message code="label.loginName"/>:</label>
			<input type="text" name="j_username" class="input-xlarge"/>
		</div>
		<div class="control-group">
			<label class="control-label" for="j_password"><spring:message code="label.password"/>:</label>
			<input type="password" name="j_password" class="input-xlarge"/>
		</div>
		<div class="control-group">
			<label class="checkbox" for="_spring_security_remember_me">
				<input type="checkbox" name="_spring_security_remember_me" value="true"/>
				<spring:message code="label.rememberMe"/>
			</label>
		</div>
		<div class="form-actions">
			<input type="submit" value="<spring:message code="button.login"/>" class="btn btn-primary"/>
		</div>
	</fieldset>
</form>

<form action="<c:url value="/j_spring_openid_security_check"/>" method="POST" class="well form-horizontal loginForm">
	<fieldset>
		<div class="control-group">
			<label class="control-label" for="openid_identifier"><spring:message code="label.openId"/>:</label>
			<input type="text" name="openid_identifier" class="input-xlarge"/>
		</div>
		<div class="control-group">
			<label class="checkbox" for="_spring_security_remember_me">
				<input type="checkbox" name="_spring_security_remember_me" value="true"/>
				<spring:message code="label.rememberMe"/>
			</label>
		</div>
		<div class="form-actions">
			<input type="submit" value="<spring:message code="button.login"/>" class="btn btn-primary"/>
		</div>
	</fieldset>
</form>

</center>

</dt:page>
