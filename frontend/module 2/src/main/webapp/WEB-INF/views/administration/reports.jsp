<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="pageTitle" value="All community reports"/>
<%@ include file="../fragments/head.jspf" %>
<%@ include file="../fragments/header.jspf" %>
<main id="main" class="container app-main">


<div class="page-heading"><div><span class="eyebrow">${ui['REVIEW AND ROUTING']}</span><h1>${ui[ui['All community reports']]}</h1><p>${ui['Open a report to review, reject, assign or reassign it.']}</p></div></div><%@ include file="../complaints/fragments/filters.jspf" %><section class="surface"><%@ include file="../complaints/fragments/report-list.jspf" %></section><%@ include file="../complaints/fragments/pagination.jspf" %>

</main>
<%@ include file="../fragments/footer.jspf" %>
