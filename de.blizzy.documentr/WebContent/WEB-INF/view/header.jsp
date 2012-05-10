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
<link rel="stylesheet" href="<c:url value="/css/bootstrap-2.0.3.min.css"/>" media="all"/>
<link rel="stylesheet" href="<c:url value="/css/bootstrap-responsive-2.0.3.min.css"/>" media="all"/>
<link rel="stylesheet" href="<c:url value="/css/prettify.css"/>" media="all"/>
<link rel="stylesheet" href="<c:url value="/css/styles.css"/>" media="all"/>
<link rel="stylesheet" href="<c:url value="/css/macros.css"/>" media="all"/>
<script type="text/javascript" src="<c:url value="/js/jquery-1.7.2.min.js"/>"></script>
<script type="text/javascript" src="<c:url value="/js/jquery-ui-1.8.19.custom.min.js"/>"></script>
<script type="text/javascript" src="<c:url value="/js/bootstrap-modal.js"/>"></script>
<script type="text/javascript" src="<c:url value="/js/bootstrap-dropdown.js"/>"></script>
<script type="text/javascript" src="<c:url value="/js/google-code-prettify/prettify.js"/>"></script>

<script type="text/javascript">

$(function() {
	$.ajaxSetup({
		cache: false
	});
	prettyPrint();
});

</script>

<c:if test="${!empty requestScope._headerJS}">
<script type="text/javascript">
<c:out value="${requestScope._headerJS}" escapeXml="false"/>
</script>
</c:if>

</head>

<body id="#top">
