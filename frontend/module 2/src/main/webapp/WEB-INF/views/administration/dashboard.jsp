<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="pageTitle" value="Administrator workspace"/>
<%@ include file="../fragments/head.jspf" %>
<%@ include file="../fragments/header.jspf" %>
<main id="main" class="container app-main">


<div class="page-heading"><div><span class="eyebrow">${ui['A CLEARER VIEW OF YOUR COMMUNITY']}</span><h1>${ui['Administrator workspace']}</h1><p>${ui['Review reports, route them to a department and assign officers.']}</p></div><a class="button button-primary" href="<c:url value='/admin/officers'/>">${ui['Manage officers →']}</a></div>
<%@ include file="../complaints/fragments/metrics.jspf" %>
<section class="surface"><div class="section-bar"><h2>${ui['Awaiting your review']}</h2><a class="text-link" href="<c:url value='/admin/reports'/>?status=REPORTED">${ui['View queue →']}</a></div><%@ include file="../complaints/fragments/report-list.jspf" %></section>

</main>
<%@ include file="../fragments/footer.jspf" %>
