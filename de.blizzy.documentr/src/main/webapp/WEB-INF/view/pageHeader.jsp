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
<meta name="viewport" content="width=device-width, initial-scale=1.0">
<link rel="stylesheet" href="<c:url value="/css/css.jsp"/>" media="all"/>
<script type="text/javascript" src="<c:url value="/js/jquery-1.7.2.min.js"/>"></script>
<script type="text/javascript" src="<c:url value="/js/jquery-ui-1.8.20.custom.min.js"/>"></script>
<script type="text/javascript" src="<c:url value="/js/bootstrap-modal-2.0.4.js"/>"></script>
<script type="text/javascript" src="<c:url value="/js/bootstrap-dropdown-2.0.4.js"/>"></script>
<script type="text/javascript" src="<c:url value="/js/bootstrap-button-2.0.4.js"/>"></script>
<script type="text/javascript" src="<c:url value="/js/bootstrap-tab-2.0.4.js"/>"></script>
<script type="text/javascript" src="<c:url value="/js/google-code-prettify-20110601/prettify.js"/>"></script>
<script type="text/javascript" src="<c:url value="/js/lightbox-2.51.js.jsp"/>"></script>

<c:if test="${!empty requestScope._headerJSFiles}">
	<c:forTokens var="uri" items="${requestScope._headerJSFiles}" delims="|">
		<script type="text/javascript" src="<c:url value="${uri}"/>"></script>
	</c:forTokens> 
</c:if>

<script type="text/javascript">

$.fn.extend({
	showModal: function(options) {
		this.modal(options);
		this.position({
			my: 'center center',
			at: 'center center',
			of: window
		});
	},
	
	hideModal: function() {
		this.modal('hide');
	}
});

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
