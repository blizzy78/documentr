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
<%@ taglib prefix="d" uri="http://documentr.org/tld/documentr" %>

<footer class="footer">
	<p class="pull-right">
		<a href="#"><spring:message code="button.backToTop"/></a>
	</p>
	
	<p>
	Powered by <a href="http://documentr.org">documentr</a> &ndash;
	Copyright (C) 2012-2013 Maik Schreiber
	</p>
	
	<sec:authorize access="isAuthenticated()">
		<c:set var="version" value="${d:getLatestVersionForUpdate()}"/>
		<c:if test="${!empty version}">
			<div class="alert"><spring:message code="updateAvailableToVersionX" arguments="${version}"/></div>
		</c:if>
	</sec:authorize>
</footer>
