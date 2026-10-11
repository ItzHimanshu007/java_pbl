<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="pageTitle" value="Your reports"/>
<%@ include file="../fragments/head.jspf" %><%@ include file="../fragments/header.jspf" %>
<main id="main">
 <section class="page-head"><div class="container">
  <nav class="breadcrumbs" aria-label="${ui['Breadcrumb']}"><a href="${ctx}/feed">${ui['Nearby feed']}</a><cp:icon name="chevron-right"/><span>${ui['My reports']}</span></nav>
  <div class="page-head-row"><div><span class="eyebrow">${ui['Report. Track. Resolve.']}</span><h1>${ui['Your reports']}</h1><p>${ui['Track your complaints, review repair evidence and confirm completed work.']}</p></div>
  <div class="page-actions"><a class="btn btn-light" href="${ctx}/insights/export.csv"><cp:icon name="download"/>${ui['Download CSV']}</a><a class="btn btn-accent" href="${ctx}/citizen/reports/new"><cp:icon name="plus"/>${ui['Report an issue']}</a></div></div>
 </div></section>
 <div class="container page-body stack">
  <c:if test="${param.deleted != null}"><div class="alert alert-success" role="status"><cp:icon name="circle-check"/><p>${ui['Report deleted. You can restore it from Deleted reports.']}</p></div></c:if>
  <c:if test="${param.restored != null}"><div class="alert alert-success" role="status"><cp:icon name="circle-check"/><p>${ui['Report restored.']}</p></div></c:if>
  <%@ include file="fragments/metrics.jspf" %>
  <div>
   <nav class="tabs" aria-label="${ui['Report views']}"><a class="tab ${trash ? '' : 'is-active'}" href="${ctx}/citizen/reports" ${trash ? '' : 'aria-current="page"'}><cp:icon name="list"/>${ui['Active reports']}</a><a class="tab ${trash ? 'is-active' : ''}" href="${ctx}/citizen/reports?trash=true" ${trash ? 'aria-current="page"' : ''}><cp:icon name="x"/>${ui['Deleted reports']}</a></nav>
   <c:choose><c:when test="${trash}">
    <section class="card"><div class="report-list">
     <c:if test="${empty reports}"><div class="empty"><cp:icon name="clipboard-list"/><h3>${ui['No deleted reports.']}</h3><p>${ui['Reports you delete can be restored from here.']}</p></div></c:if>
     <c:forEach items="${reports}" var="r"><div class="report-item"><span class="cat-tile"><cp:icon name="cat-${r.categoryCode}"/></span><div class="report-main"><span class="ref"><c:out value="${r.reference}"/></span><h3 class="report-title"><c:out value="${r.title}"/></h3><p class="report-sub"><cp:icon name="map-pin"/><span><c:out value="${r.address}"/></span></p></div><form method="post" action="${ctx}/citizen/reports/${r.id}/restore"><input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}"><button class="btn btn-outline btn-sm" type="submit"><cp:icon name="arrow-left"/>${ui['Restore report']}</button></form></div></c:forEach>
    </div><%@ include file="fragments/pagination.jspf" %></section>
   </c:when><c:otherwise>
    <div class="stack"><%@ include file="fragments/filters.jspf" %><section class="card"><%@ include file="fragments/report-list.jspf" %><%@ include file="fragments/pagination.jspf" %></section></div>
   </c:otherwise></c:choose>
  </div>
 </div>
</main>
<%@ include file="../fragments/footer.jspf" %>
