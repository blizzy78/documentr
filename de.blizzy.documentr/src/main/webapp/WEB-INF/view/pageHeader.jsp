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
<%@ taglib prefix="sec" uri="http://www.springframework.org/security/tags" %>

<!DOCTYPE html>
<html>

<head>
<meta http-equiv="content-type" content="text/html; charset=UTF-8"/>

<c:if test="${!empty requestScope._pageTitle}">
	<title><c:out value="${requestScope._pageTitle}" escapeXml="false"/> &ndash; documentr</title>
</c:if>

<meta name="viewport" content="width=device-width, initial-scale=1.0">

<jsp:include page="/WEB-INF/view/requireConfig.jsp"/>

<link rel="stylesheet" href="<c:url value="/css/documentr.css"/>" media="all"/>

<script type="text/javascript" src="<c:url value="/js/require-2.1.2.js"/>"></script>
<script type="text/javascript" src="<c:url value="/js/jquery-1.8.3.min.js"/>"></script>
<script type="text/javascript" src="<c:url value="/js/bootstrap-2.2.2.min.js"/>"></script>
<script type="text/javascript" src="<c:url value="/js/documentr.js"/>"></script>

<c:if test="${!empty requestScope._headerJSFiles}">
	<c:forTokens var="uri" items="${requestScope._headerJSFiles}" delims="|">
		<script type="text/javascript" src="<c:url value="${uri}"/>"></script>
	</c:forTokens> 
</c:if>

<c:out value="${requestScope._headerHTML}" escapeXml="false"/>

</head>

<body id="#top">
