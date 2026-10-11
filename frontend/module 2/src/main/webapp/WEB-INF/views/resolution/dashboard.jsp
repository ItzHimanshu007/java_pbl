<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="pageTitle" value="Officer workspace"/>
<%@ include file="../fragments/head.jspf" %><%@ include file="../fragments/header.jspf" %>
<main id="main">
 <section class="page-head"><div class="container"><div class="page-head-row"><div><span class="eyebrow">${ui['Field officer workspace']}</span><h1>${ui['Hello,']} <c:out value="${displayName}"/></h1><p>${ui['Your assigned work, progress and repairs in one place.']}</p></div><div class="page-actions"><a class="btn btn-accent" href="${ctx}/officer/reports"><cp:icon name="hard-hat"/>${ui['Open my work']}</a></div></div></div></section>
 <div class="container page-body stack">
  <section class="kpi-row" aria-label="${ui['Report counts']}">
   <a class="kpi" href="${ctx}/officer/reports?status=ASSIGNED"><span class="kpi-icon tone-info"><cp:icon name="user-round"/></span><div><div class="kpi-value">${totals['ASSIGNED']}</div><div class="kpi-label">${ui['New assignments']}</div></div></a>
   <a class="kpi" href="${ctx}/officer/reports?status=IN_PROGRESS"><span class="kpi-icon tone-orange"><cp:icon name="hard-hat"/></span><div><div class="kpi-value">${totals['IN_PROGRESS']}</div><div class="kpi-label">${ui['In progress']}</div></div></a>
   <a class="kpi" href="${ctx}/officer/reports?status=RESOLVED"><span class="kpi-icon tone-violet"><cp:icon name="check"/></span><div><div class="kpi-value">${totals['RESOLVED']}</div><div class="kpi-label">${ui['Awaiting citizen check']}</div></div></a>
   <div class="kpi"><span class="kpi-icon tone-bad"><cp:icon name="triangle-alert"/></span><div><div class="kpi-value">${overdueCount}</div><div class="kpi-label">${ui['Past target time']}</div></div></div>
   <a class="kpi" href="${ctx}/officer/reports?status=CLOSED"><span class="kpi-icon tone-ok"><cp:icon name="badge-check"/></span><div><div class="kpi-value">${totals['CLOSED']}</div><div class="kpi-label">${ui['Verified closed']}</div></div></a>
   <div class="kpi"><span class="kpi-icon"><cp:icon name="timer"/></span><div><div class="kpi-value">${averageHours}<small>${ui['hrs']}</small></div><div class="kpi-label">${ui['Average time to verified fix']}</div></div></div>
  </section>
  <div class="grid-2">
   <section class="card"><div class="card-head"><h2 class="card-title"><cp:icon name="triangle-alert"/>${ui['Needs attention first']}</h2><span class="chip badge-overdue">${overdueCount}</span></div><c:set var="latestReports" value="${reports}"/><c:set var="reports" value="${overdueReports}"/><%@ include file="../complaints/fragments/report-list.jspf" %></section>
   <section class="card"><div class="card-head"><h2 class="card-title"><cp:icon name="clipboard-list"/>${ui['Your latest assignments']}</h2><a class="link" href="${ctx}/officer/reports">${ui['View all']}<cp:icon name="arrow-right"/></a></div><c:set var="reports" value="${latestReports}"/><%@ include file="../complaints/fragments/report-list.jspf" %></section>
  </div>
 </div>
</main><%@ include file="../fragments/footer.jspf" %>
