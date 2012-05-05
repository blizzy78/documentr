<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@ taglib prefix="sec" uri="http://www.springframework.org/security/tags" %>

<li class="btn-toolbar pull-right">
	<sec:authorize access="isAnonymous()">
		<div class="btn-group">
			<a href="<c:url value="/access/login"/>" class="btn btn-mini"><i class="icon-lock"></i> <spring:message code="button.login"/></a>
		</div>
	</sec:authorize>
	<sec:authorize access="isAuthenticated()">
		<sec:authorize access="hasRole('ROLE_ADMIN')">
			<div class="btn-group">
				<a class="btn btn-mini dropdown-toggle" data-toggle="dropdown" href="#"><i class="icon-cog"></i> <spring:message code="button.administration"/> <span class="caret"></span></a>
				<ul class="dropdown-menu">
					<li><a href="#"><i class="icon-download-alt"></i> <spring:message code="button.addAttachment"/></a></li>
				</ul>
			</div>
		</sec:authorize>
		<div class="btn-group">
			<c:set var="loginName"><sec:authentication property="principal.username"/></c:set>
			<a href="<c:url value="/j_spring_security_logout"/>" class="btn btn-mini"><i class="icon-off"></i> <spring:message code="button.logoutUserX" arguments="${loginName}"/></a>
		</div>
	</sec:authorize>
</li>
