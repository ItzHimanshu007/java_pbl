<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="pageTitle" value="Your assigned work"/>
<%@ include file="../fragments/head.jspf" %><%@ include file="../fragments/header.jspf" %>
<main id="main">
 <section class="page-head"><div class="container"><nav class="breadcrumbs" aria-label="${ui['Breadcrumb']}"><a href="${ctx}/officer/dashboard">${ui['Dashboard']}</a><cp:icon name="chevron-right"/><span>${ui['My work']}</span></nav><div class="page-head-row"><div><span class="eyebrow">${ui['From assignment to repair']}</span><h1>${ui['Your assigned work']}</h1><p>${ui['Open a report to start work, post an update or submit repair evidence.']}</p></div></div></div></section>
 <div class="container page-body stack"><%@ include file="../complaints/fragments/filters.jspf" %><section class="card"><%@ include file="../complaints/fragments/report-list.jspf" %><%@ include file="../complaints/fragments/pagination.jspf" %></section></div>
</main><%@ include file="../fragments/footer.jspf" %>
