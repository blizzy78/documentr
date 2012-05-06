<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@ taglib prefix="sec" uri="http://www.springframework.org/security/tags" %>
<%@ taglib prefix="d" uri="http://documentr.org/tld/documentr" %>
<%@ taglib prefix="dt" tagdir="/WEB-INF/tags" %>

<sec:authorize access="hasRole('ROLE_ADMIN')">

<dt:breadcrumbs>
	<li class="active"><spring:message code="title.users"/></li>
</dt:breadcrumbs>

<dt:page>

<div class="page-header"><h1><spring:message code="title.users"/></h1></div>

<c:set var="users" value="${d:listUsers()}"/>
<ul>
<c:forEach var="user" items="${users}">
	<li><a href="<c:url value="/user/edit/${user}"/>"><c:out value="${user}"/></a></li>
</c:forEach>
</ul>

<p>
<a href="<c:url value="/user/add"/>" class="btn"><i class="icon-plus"></i> <spring:message code="button.addUser"/></a>
</p>

</dt:page>

</sec:authorize>
