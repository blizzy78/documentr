<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@ taglib prefix="dt" tagdir="/WEB-INF/tags" %>

<dt:page>

<div class="page-header"><h1><spring:message code="title.login"/></h1></div>

<form action="<c:url value="/j_spring_security_check"/>" method="POST" class="well">
	<fieldset class="control-group">
		<label><spring:message code="label.loginName"/>:</label>
		<input type="text" name="j_username"/>
	</fieldset>
	<fieldset class="control-group">
		<label><spring:message code="label.password"/>:</label>
		<input type="password" name="j_password"/>
	</fieldset>
	<fieldset class="control-group">
		<input type="submit" value="<spring:message code="button.login"/>" class="btn btn-primary"/>
	</fieldset>
</form>

</dt:page>
