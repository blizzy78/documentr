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
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@ taglib prefix="sec" uri="http://www.springframework.org/security/tags" %>

<!DOCTYPE html>
<html>

<head>
<meta http-equiv="content-type" content="text/html; charset=UTF-8"/>

<c:if test="${!empty requestScope._pageTitle}">
	<title><c:out value="${requestScope._pageTitle}" escapeXml="false"/> &ndash; documentr</title>
</c:if>

<meta name="viewport" content="width=device-width, initial-scale=1.0">

<link rel="stylesheet" href="<c:url value="/css/bootstrap-2.0.4.min.css"/>" media="all"/>
<link rel="stylesheet" href="<c:url value="/css/bootstrap-responsive-2.0.4.min.css"/>" media="all"/>
<link rel="stylesheet" href="<c:url value="/css/prettify-20110601.css"/>" media="all"/>
<link rel="stylesheet" href="<c:url value="/css/lightbox-2.51.css.jsp"/>" media="all"/>
<link rel="stylesheet" href="<c:url value="/css/documentr.css"/>" media="all"/>

<script type="text/javascript" src="<c:url value="/js/jquery-1.7.2.min.js"/>"></script>
<script type="text/javascript" src="<c:url value="/js/jquery-ui-1.8.20.custom.min.js"/>"></script>
<script type="text/javascript" src="<c:url value="/js/bootstrap-button-2.0.4.js"/>"></script>
<script type="text/javascript" src="<c:url value="/js/bootstrap-dropdown-2.0.4.js"/>"></script>
<script type="text/javascript" src="<c:url value="/js/bootstrap-modal-2.0.4.js"/>"></script>
<script type="text/javascript" src="<c:url value="/js/bootstrap-tab-2.0.4.js"/>"></script>
<script type="text/javascript" src="<c:url value="/js/lightbox-2.51.js.jsp"/>"></script>
<script type="text/javascript" src="<c:url value="/js/google-code-prettify-20110601/prettify.js"/>"></script>
<script type="text/javascript" src="<c:url value="/js/jquery.jstree-pre-1.0-fix-1/jquery.jstree-pre-1.0-fix-1.min.js"/>"></script>
<script type="text/javascript" src="<c:url value="/js/diff_match_patch-20120106.js"/>"></script>
<script type="text/javascript" src="<c:url value="/js/documentr.js"/>"></script>

<c:if test="${!empty requestScope._headerJSFiles}">
	<c:forTokens var="uri" items="${requestScope._headerJSFiles}" delims="|">
		<script type="text/javascript" src="<c:url value="${uri}"/>"></script>
	</c:forTokens> 
</c:if>

<script type="text/javascript">

<sec:authorize access="isAuthenticated()">

documentr.pageTreeOptions = {
	applicationUrl: '<c:url value="/pageTree/application/json"/>',
	projectUrl: '<c:url value="/pageTree/project/_PROJECTNAME_/json"/>',
	branchUrl: '<c:url value="/pageTree/branch/_PROJECTNAME_/_BRANCHNAME_/json"/>',
	pageUrl: '<c:url value="/pageTree/page/_PROJECTNAME_/_BRANCHNAME_/_PAGEPATH_/json"/>',
	projectTitle: "<spring:message code="label.projectX" arguments="_PROJECTNAME_"/>",
	branchTitle: "<spring:message code="label.branchX" arguments="_BRANCHNAME_"/>"
};

</sec:authorize>

$(function() {
	$.ajaxSetup({
		cache: false
	});
	
	prettyPrint();
});

<c:out value="${requestScope._headerJS}" escapeXml="false"/>

</script>

</head>

<body id="#top">
