<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<!DOCTYPE html>
<html>

<head>
<meta http-equiv="content-type" content="text/html; charset=UTF-8"/>
<link rel="stylesheet" href="<c:url value="/css/bootstrap-2.0.3.min.css"/>" media="all"/>
<link rel="stylesheet" href="<c:url value="/css/bootstrap-responsive-2.0.3.min.css"/>" media="all"/>
<link rel="stylesheet" href="<c:url value="/css/styles.css"/>" media="all"/>
</head>

<body id="#top">

<div class="navbar navbar-fixed-top">
	<div class="navbar-inner">
		<div class="container">
			<ul class="nav">
				<li><a href="<c:url value="/projects"/>"><spring:message code="button.projects"/></a></li>
			</ul>
		</div>
	</div>
</div>

<div class="container">
