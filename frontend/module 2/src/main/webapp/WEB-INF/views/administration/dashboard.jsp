<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="pageTitle" value="Administrator workspace"/>
<%@ include file="../fragments/head.jspf" %><%@ include file="../fragments/header.jspf" %>
<main id="main">
 <section class="page-head"><div class="container"><div class="page-head-row"><div><span class="eyebrow">${ui['Administrator control room']}</span><h1>${ui['City complaints overview']}</h1><p>${ui['Review new complaints, route them to departments and watch target resolution times across Jaipur.']}</p></div>
  <div class="page-actions"><a class="btn btn-light" href="${ctx}/insights/export.csv"><cp:icon name="download"/>${ui['Download CSV']}</a><a class="btn btn-on-dark" href="${ctx}/admin/officers"><cp:icon name="users"/>${ui['Officers']}</a><a class="btn btn-accent" href="${ctx}/admin/reports?status=REPORTED"><cp:icon name="clipboard-list"/>${ui['Review queue']}</a></div></div></div></section>
 <div class="container page-body stack">
  <section class="kpi-row" aria-label="${ui['Report counts']}">
   <a class="kpi" href="${ctx}/admin/reports"><span class="kpi-icon"><cp:icon name="file-text"/></span><div><div class="kpi-value">${totals['REPORTED'] + totals['ASSIGNED'] + totals['IN_PROGRESS'] + totals['RESOLVED'] + totals['CLOSED'] + totals['REJECTED']}</div><div class="kpi-label">${ui['All complaints']}</div></div></a>
   <a class="kpi" href="${ctx}/admin/reports?status=REPORTED"><span class="kpi-icon tone-warn"><cp:icon name="clock"/></span><div><div class="kpi-value">${totals['REPORTED']}</div><div class="kpi-label">${ui['Awaiting review']}</div></div></a>
   <a class="kpi" href="${ctx}/admin/reports?status=IN_PROGRESS"><span class="kpi-icon tone-orange"><cp:icon name="hard-hat"/></span><div><div class="kpi-value">${totals['ASSIGNED'] + totals['IN_PROGRESS']}</div><div class="kpi-label">${ui['Assigned or in progress']}</div></div></a>
   <div class="kpi"><span class="kpi-icon tone-bad"><cp:icon name="triangle-alert"/></span><div><div class="kpi-value">${overdueCount}</div><div class="kpi-label">${ui['Past target time']}</div></div></div>
   <a class="kpi" href="${ctx}/admin/reports?status=CLOSED"><span class="kpi-icon tone-ok"><cp:icon name="badge-check"/></span><div><div class="kpi-value">${totals['CLOSED']}</div><div class="kpi-label">${ui['Verified closed']}</div></div></a>
   <div class="kpi"><span class="kpi-icon tone-violet"><cp:icon name="timer"/></span><div><div class="kpi-value">${averageHours}<small>${ui['hrs']}</small></div><div class="kpi-label">${ui['Average time to verified fix']}</div></div></div>
   <a class="kpi" href="${ctx}/admin/officers"><span class="kpi-icon tone-pink"><cp:icon name="users"/></span><div><div class="kpi-value">${officerCount}</div><div class="kpi-label">${ui['Active officers']}</div></div></a>
  </section>
  <div class="layout-main-side">
   <div class="stack">
    <section class="card"><div class="card-head"><h2 class="card-title"><cp:icon name="clipboard-list"/>${ui['Awaiting your review']}</h2><a class="link" href="${ctx}/admin/reports?status=REPORTED">${ui['View queue']}<cp:icon name="arrow-right"/></a></div><%@ include file="../complaints/fragments/report-list.jspf" %></section>
    <section class="card"><div class="card-head"><h2 class="card-title"><cp:icon name="building-2"/>${ui['Department workload']}</h2></div><%@ include file="../fragments/department-load.jspf" %></section>
    <section class="card"><div class="card-head"><h2 class="card-title"><cp:icon name="triangle-alert"/>${ui['Past target resolution time']}</h2><span class="chip badge-overdue">${overdueCount}</span></div><c:set var="reports" value="${overdueReports}"/><%@ include file="../complaints/fragments/report-list.jspf" %></section>
   </div>
   <aside class="stack">
    <section class="card"><div class="card-head"><h2 class="card-title"><cp:icon name="map-pin"/>${ui['Complaints by zone']}</h2></div><div class="card-body"><c:set var="barRows" value="${zoneRows}"/><c:set var="barKind" value="zone"/><%@ include file="../fragments/bars.jspf" %></div></section>
    <section class="card"><div class="card-head"><h2 class="card-title"><cp:icon name="chart-column"/>${ui['Top services']}</h2><a class="link" href="${ctx}/insights">${ui['Insights']}<cp:icon name="arrow-right"/></a></div><div class="card-body"><c:set var="barRows" value="${categoryRows}"/><c:set var="barKind" value="category"/><%@ include file="../fragments/bars.jspf" %></div></section>
   </aside>
  </div>
 </div>
</main><%@ include file="../fragments/footer.jspf" %>
