<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="pageTitle" value="Please try again"/>
<%@ include file="fragments/head.jspf" %><%@ include file="fragments/header.jspf" %>
<main id="main" class="container error-page"><span class="error-code"><c:out value="${errorCode}"/></span><h1>${ui[errorTitle]}</h1><p class="muted">${ui[errorMessage]}</p><div class="row"><a class="btn btn-primary" href="${ctx}/"><cp:icon name="house"/>${ui['Back to home']}</a><c:if test="${signedIn}"><a class="btn btn-outline" href="${ctx}${dashboardPath}"><cp:icon name="gauge"/>${ui['Your workspace']}</a></c:if></div></main>
<%@ include file="fragments/footer.jspf" %>
