<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@ taglib prefix="sec" uri="http://www.springframework.org/security/tags" %>

<li class="btn-toolbar pull-right">
	<sec:authorize access="isAnonymous()">
		<a href="<c:url value="/access/login"/>" class="btn btn-mini"><i class="icon-lock"></i> <spring:message code="button.login"/></a>
	</sec:authorize>
	<sec:authorize access="isAuthenticated()">
		<a href="<c:url value="/j_spring_security_logout"/>" class="btn btn-mini"><i class="icon-off"></i> <spring:message code="button.logout"/></a>
	</sec:authorize>
</li>
