<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="pageTitle" value="Officer workspace"/>
<%@ include file="../fragments/head.jspf" %>
<%@ include file="../fragments/header.jspf" %>
<main id="main" class="container app-main">


<div class="page-heading"><div><span class="eyebrow">${ui['MAKE THE NEXT UPDATE COUNT']}</span><h1>${ui['Hello,']} <c:out value="${displayName}"/>.</h1><p>${ui['Your assigned work, progress and repairs in one place.']}</p></div><a class="button button-primary" href="<c:url value='/officer/reports'/>">${ui['Open my work →']}</a></div><%@ include file="../complaints/fragments/metrics.jspf" %><section class="surface"><div class="section-bar"><h2>${ui['Your latest assignments']}</h2></div><%@ include file="../complaints/fragments/report-list.jspf" %></section>

</main>
<%@ include file="../fragments/footer.jspf" %>
