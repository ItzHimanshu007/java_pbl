<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="pageTitle" value="Your citizen workspace"/>
<%@ include file="../fragments/head.jspf" %>
<%@ include file="../fragments/header.jspf" %>
<main id="main" class="container app-main">


<div class="page-heading"><div><span class="eyebrow">${ui['YOUR COMMUNITY. YOUR VOICE.']}</span><h1>${ui['Hello,']} <c:out value="${displayName}"/>.</h1><p>${ui['Keep a clear view of the issues you have reported.']}</p></div><a class="button button-primary" href="<c:url value='/citizen/reports/new'/>">${ui['Report an issue ↗']}</a></div>
<%@ include file="fragments/metrics.jspf" %>
<section class="surface"><div class="section-bar"><h2>${ui['Your latest reports']}</h2><a class="text-link" href="<c:url value='/citizen/reports'/>">${ui['View all →']}</a></div><%@ include file="fragments/report-list.jspf" %></section>

</main>
<%@ include file="../fragments/footer.jspf" %>
