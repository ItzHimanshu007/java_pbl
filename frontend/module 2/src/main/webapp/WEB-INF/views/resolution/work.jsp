<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="pageTitle" value="Your assigned work"/>
<%@ include file="../fragments/head.jspf" %>
<%@ include file="../fragments/header.jspf" %>
<main id="main" class="container app-main">


<div class="page-heading"><div><span class="eyebrow">${ui['FROM ASSIGNMENT TO REPAIR']}</span><h1>${ui['Your assigned work']}</h1><p>${ui['Open a report to start work, post an update or submit repair evidence.']}</p></div></div><%@ include file="../complaints/fragments/filters.jspf" %><section class="surface"><%@ include file="../complaints/fragments/report-list.jspf" %></section><%@ include file="../complaints/fragments/pagination.jspf" %>

</main>
<%@ include file="../fragments/footer.jspf" %>
