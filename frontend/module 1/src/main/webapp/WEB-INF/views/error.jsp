<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="pageTitle" value="Please try again"/>
<%@ include file="fragments/head.jspf" %><%@ include file="fragments/header.jspf" %>
<main id="main" class="error-shell container"><span class="eyebrow">${ui['CIVICPULSE ·']} <c:out value="${errorCode}"/></span><h1>${ui[errorTitle]}</h1><p>${ui[errorMessage]}</p><a class="button button-primary" href="<c:url value='/'/>">${ui['Back to home →']}</a></main><%@ include file="fragments/footer.jspf" %>
