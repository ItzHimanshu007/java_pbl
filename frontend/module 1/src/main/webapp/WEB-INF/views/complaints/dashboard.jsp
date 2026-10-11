<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="pageTitle" value="Your citizen workspace"/>
<%@ include file="../fragments/head.jspf" %><%@ include file="../fragments/header.jspf" %>
<main id="main">
 <section class="page-head"><div class="container"><div class="page-head-row"><div><span class="eyebrow">${ui['Citizen workspace']}</span><h1>${ui['Hello,']} <c:out value="${displayName}"/></h1><p>${ui['Keep a clear view of the issues you have reported.']}</p></div><div class="page-actions"><a class="btn btn-accent" href="${ctx}/citizen/reports/new"><cp:icon name="plus"/>${ui['Report an issue']}</a></div></div></div></section>
 <div class="container page-body stack"><%@ include file="fragments/metrics.jspf" %><section class="card"><div class="card-head"><h2 class="card-title"><cp:icon name="clipboard-list"/>${ui['Your latest reports']}</h2><a class="link" href="${ctx}/citizen/reports">${ui['View all']}<cp:icon name="arrow-right"/></a></div><%@ include file="fragments/report-list.jspf" %></section></div>
</main>
<%@ include file="../fragments/footer.jspf" %>
