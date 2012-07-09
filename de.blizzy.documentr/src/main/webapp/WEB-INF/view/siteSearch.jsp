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

<c:if test="${empty requestScope._showSiteSearch}"><c:set var="styleInvisible" value="display: none;"/></c:if>
<div id="site-search" class="site-search" style="${styleInvisible}">
	<form class="form-search pull-right" action="<c:url value="/search/page"/>" method="GET">
		<input type="text" name="q" class="search-query input-xlarge" value="${requestScope._showSiteSearch}"
			placeholder="<spring:message code="searchTermsPlaceholder"/>"/>
	</form>
</div>
