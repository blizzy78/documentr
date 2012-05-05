<%@ tag pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<c:set var="_breadcrumbs" scope="request">
<nav>
	<div class="breadcrumb">
		<div class="container">
			<ul>
				<jsp:doBody/>
				<jsp:include page="/WEB-INF/view/breadcrumbsToolbar.jsp"/>
			</ul>
		</div>
	</div>
</nav>
</c:set>
