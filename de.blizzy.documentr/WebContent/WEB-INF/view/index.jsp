<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@ taglib prefix="d" uri="http://documentr.org/tld/documentr" %>
<jsp:include page="/WEB-INF/view/header.jsp"/>

<ul class="breadcrumb">
	<li class="active"><spring:message code="title.projects"/></li>
</ul>

<div class="page-header"><h1><spring:message code="title.projects"/></h1></div>

<p>
<c:set var="projects" value="${d:listProjects()}"/>
<c:choose>
	<c:when test="${!empty projects}">
		<c:forEach var="project" items="${projects}">
			<a href="<c:url value="/project/${project}"/>"><c:out value="${project}"/></a><br/>
		</c:forEach>
	</c:when>
	<c:otherwise>
		<spring:message code="projects.notFound"/>
	</c:otherwise>
</c:choose>
</p>

<p>
<a href="<c:url value="/project/create"/>" class="btn"><i class="icon-plus"></i> <spring:message code="button.createProject"/></a>
</p>

<jsp:include page="/WEB-INF/view/footer.jsp"/>
