<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@ taglib prefix="sec" uri="http://www.springframework.org/security/tags" %>
<%@ taglib prefix="d" uri="http://documentr.org/tld/documentr" %>
<jsp:include page="/WEB-INF/view/header.jsp"/>

<c:set var="projects" value="${d:listProjects()}"/>
<c:choose>
	<c:when test="${!empty projects}">
		<ul class="breadcrumb">
			<li class="active"><spring:message code="title.projects"/></li>
		</ul>
		
		<div class="page-header"><h1><spring:message code="title.projects"/></h1></div>

		<ul>
		<c:forEach var="project" items="${projects}">
			<li><a href="<c:url value="/project/${project}"/>"><c:out value="${project}"/></a></li>
		</c:forEach>
		</ul>

		<sec:authorize access="isAuthenticated()">
			<p>
			<a href="<c:url value="/project/create"/>" class="btn"><i class="icon-plus"></i> <spring:message code="button.createProject"/></a>
			</p>
		</sec:authorize>
	</c:when>
	<c:otherwise>
		<div class="row">
			<div class="hero-unit span8 offset2 hero-single">
				<h1>documentr</h1>
				<p><spring:message code="welcomeToDocumentr" htmlEscape="false"/></p>
				<p>
					<sec:authorize access="isAnonymous()">
						<a href="<c:url value="/access/login"/>" class="btn btn-primary btn-large"><spring:message code="button.login"/></a>
					</sec:authorize>
					<sec:authorize access="isAuthenticated()">
						<a href="<c:url value="/project/create"/>" class="btn btn-primary btn-large"><spring:message code="button.createFirstProject"/></a>
					</sec:authorize>
				</p>
			</div>
		</div>
	</c:otherwise>
</c:choose>

<jsp:include page="/WEB-INF/view/footer.jsp"/>
