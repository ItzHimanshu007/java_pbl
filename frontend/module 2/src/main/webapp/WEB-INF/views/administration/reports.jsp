<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="pageTitle" value="All community reports"/>
<%@ include file="../fragments/head.jspf" %><%@ include file="../fragments/header.jspf" %>
<main id="main">
 <section class="page-head"><div class="container"><nav class="breadcrumbs" aria-label="${ui['Breadcrumb']}"><a href="${ctx}/admin/dashboard">${ui['Dashboard']}</a><cp:icon name="chevron-right"/><span>${ui['All reports']}</span></nav><div class="page-head-row"><div><span class="eyebrow">${ui['Review and routing']}</span><h1>${ui['All community reports']}</h1><p>${ui['Open a report to review, reject, assign or reassign it.']}</p></div><div class="page-actions"><a class="btn btn-light" href="${ctx}/insights/export.csv?status=${statusFilter}"><cp:icon name="download"/>${ui['Download CSV']}</a></div></div></div></section>
 <div class="container page-body stack"><%@ include file="../complaints/fragments/filters.jspf" %><section class="card"><%@ include file="../complaints/fragments/report-list.jspf" %><%@ include file="../complaints/fragments/pagination.jspf" %></section></div>
</main><%@ include file="../fragments/footer.jspf" %>
