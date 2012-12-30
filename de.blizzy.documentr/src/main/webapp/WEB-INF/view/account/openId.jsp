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
<%@ taglib prefix="dt" tagdir="/WEB-INF/tags" %>

<sec:authorize access="isAuthenticated()">

<dt:pageJS>

function showAddDialog() {
	require(['documentr/dialog'], function() {
		$('#add-openid-dialog').showModal();
	});
}

function showRemoveDialog(openId) {
	require(['documentr/dialog'], function(dialog) {
		dialog.openMessageDialog('<spring:message code="title.removeOpenId"/>',
			"<spring:message code="removeOpenIdX" arguments=" "/>".replace(/' '/, '\'' + openId + '\''), [
			{
				text: '<spring:message code="button.remove"/>',
				href: '<c:url value="/account/removeOpenId"/>?openId=' + encodeURIComponent(openId),
				type: 'primary'
			},
			{
				text: '<spring:message code="button.cancel"/>',
				cancel: true
			}
		]);
	});
}

</dt:pageJS>

<dt:breadcrumbs>
	<li class="active"><spring:message code="title.userAccount"/></li>
</dt:breadcrumbs>

<dt:pageTitle><spring:message code="title.userAccount"/></dt:pageTitle>

<dt:page>

<div class="page-header"><h1><spring:message code="title.userAccount"/></h1></div>

<ul class="nav nav-tabs">
	<li><a href="<c:url value="/account/myAccount"/>"><spring:message code="title.accountData"/></a></li>
	<li class="active"><a href="<c:url value="/account/openId"/>"><spring:message code="title.openId"/></a></li>
</ul>

<h2><spring:message code="title.linkedOpenIds"/></h2>

<c:set var="openIds" value="${d:listMyOpenIds()}"/>
<c:choose>
	<c:when test="${!empty openIds}">
		<ul>
			<c:forEach var="openId" items="${openIds}">
				<li><c:out value="${openId.delegateId}"/> (<a href="javascript:void(showRemoveDialog('${openId.delegateId}'));"><spring:message code="button.remove"/></a>)</li>
			</c:forEach>
		</ul>
	</c:when>
	<c:otherwise>
		<p>
		<spring:message code="noOpenIdsFound"/>
		</p>
	</c:otherwise>
</c:choose>

<p>
<a href="javascript:void(showAddDialog());" class="btn"><i class="icon-plus"></i> <spring:message code="button.addOpenId"/></a>
</p>

<div class="modal" id="add-openid-dialog" style="display: none;">
	<div class="modal-header">
		<button class="close" onclick="$('#add-openid-dialog').hideModal();">&#x00D7</button>
		<h3><spring:message code="title.addOpenId"/></h3>
	</div>
	<div class="modal-body">
		<form id="add-openid-form" action="<c:url value="/accountOpenId/save"/>" method="POST" class="form-horizontal">
			<fieldset>
				<div class="control-group">
					<label class="control-label"><spring:message code="label.openId"/>:</label>
					<div class="controls">
						<input type="text" name="openId" class="input-xlarge"/>
					</div>
				</div>
			</fieldset>
		</form>
	</div>
	<div class="modal-footer">
		<a href="javascript:void($('#add-openid-form').submit());" class="btn btn-primary"><spring:message code="button.next"/></a>
		<a href="javascript:void($('#add-openid-dialog').hideModal());" class="btn"><spring:message code="button.cancel"/></a>
	</div>
</div>

</dt:page>

</sec:authorize>
