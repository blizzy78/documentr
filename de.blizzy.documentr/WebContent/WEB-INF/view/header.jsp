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
