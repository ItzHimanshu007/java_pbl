<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="pageTitle" value="Report insights"/>
<%@ include file="../fragments/head.jspf" %><%@ include file="../fragments/header.jspf" %>
<main id="main">
 <section class="page-head"><div class="container"><div class="page-head-row"><div><span class="eyebrow">${ui['Progress you can see']}</span><h1>${ui['Report insights']}</h1><p>${ui[scopeLabel]} · ${ui['Live counts from the report records.']}</p></div><div class="page-actions"><a class="btn btn-light" href="${ctx}/insights/export.csv"><cp:icon name="download"/>${ui['Download CSV']}</a></div></div></div></section>
 <div class="container page-body stack">
  <%@ include file="../complaints/fragments/metrics.jspf" %>
  <div class="grid-3">
   <div class="kpi"><span class="kpi-icon tone-violet"><cp:icon name="timer"/></span><div><div class="kpi-value">${averageHours}<small>${ui['hrs']}</small></div><div class="kpi-label">${ui['Average time to verified closure']}</div></div></div>
   <div class="kpi"><span class="kpi-icon tone-bad"><cp:icon name="triangle-alert"/></span><div><div class="kpi-value">${overdueCount}</div><div class="kpi-label">${ui['Open complaints past target time']}</div></div></div>
   <div class="kpi"><span class="kpi-icon tone-pink"><cp:icon name="map-pin"/></span><div><div class="kpi-value">${zoneRows.size()}</div><div class="kpi-label">${ui['Zones with complaints']}</div></div></div>
  </div>
  <div class="grid-2">
   <section class="card"><div class="card-head"><h2 class="card-title"><cp:icon name="chart-column"/>${ui['Reports by service']}</h2></div><div class="card-body"><c:set var="barRows" value="${categoryRows}"/><c:set var="barKind" value="category"/><%@ include file="../fragments/bars.jspf" %></div></section>
   <section class="card"><div class="card-head"><h2 class="card-title"><cp:icon name="map-pin"/>${ui['Reports by zone']}</h2></div><div class="card-body"><c:set var="barRows" value="${zoneRows}"/><c:set var="barKind" value="zone"/><%@ include file="../fragments/bars.jspf" %></div></section>
  </div>
  <c:if test="${not empty departmentLoad}"><section class="card"><div class="card-head"><h2 class="card-title"><cp:icon name="building-2"/>${ui['Department workload']}</h2></div><%@ include file="../fragments/department-load.jspf" %></section></c:if>
  <p class="help">${ui['Average time includes only reports the citizen has confirmed as fixed. Export includes up to 1,000 recent reports you can access.']}</p>
 </div>
</main><%@ include file="../fragments/footer.jspf" %>
