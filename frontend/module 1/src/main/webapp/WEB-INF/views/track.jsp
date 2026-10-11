<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="pageTitle" value="Track complaint"/>
<%@ include file="fragments/head.jspf" %><%@ include file="fragments/header.jspf" %>
<main id="main">
 <section class="page-head"><div class="container">
  <nav class="breadcrumbs" aria-label="${ui['Breadcrumb']}"><a href="${ctx}/">${ui['Home']}</a><cp:icon name="chevron-right"/><span>${ui['Track complaint']}</span></nav>
  <div class="track-hero"><h1>${ui['Track your complaint']}</h1><p>${ui['Enter the reference number shown on your acknowledgement. No sign-in is needed.']}</p>
   <form class="track-form" method="get" action="${ctx}/track"><label class="visually-hidden" for="ref">${ui['Complaint reference number']}</label><input id="ref" name="ref" required maxlength="40" value="<c:out value='${ref}'/>" placeholder="CP-2026-XXXXXXXXXXXX" autocomplete="off"><button class="btn btn-accent btn-lg" type="submit"><cp:icon name="search"/>${ui['Track']}</button></form>
  </div>
 </div></section>
 <div class="container page-body">
  <c:if test="${notFound}"><div class="alert alert-error" role="alert"><cp:icon name="circle-alert"/><div><p><strong>${ui['No complaint found with this reference number.']}</strong></p><p>${ui['Check the number on your acknowledgement and try again.']}</p></div></div></c:if>
  <c:if test="${empty report and not notFound}"><div class="grid-3">
   <div class="card card-pad"><span class="cat-tile"><cp:icon name="file-text"/></span><h2 class="mt-2">${ui['Where is my reference?']}</h2><p class="muted mt-1">${ui['It is shown right after you submit a complaint and on every page of the complaint, for example CP-2026-1A2B3C4D5E6F.']}</p></div>
   <div class="card card-pad"><span class="cat-tile"><cp:icon name="lock"/></span><h2 class="mt-2">${ui['What is shown here?']}</h2><p class="muted mt-1">${ui['Only the public progress: service, department, area, status and dates. Names, contact details, notes and photos stay private.']}</p></div>
   <div class="card card-pad"><span class="cat-tile"><cp:icon name="circle-user"/></span><h2 class="mt-2">${ui['Want the full details?']}</h2><p class="muted mt-1">${ui['Sign in to see photos, officer updates and to verify the repair.']}</p><a class="link mt-2" href="${ctx}/login">${ui['Sign in']}<cp:icon name="arrow-right"/></a></div>
  </div></c:if>
  <c:if test="${not empty report}">
   <div class="layout-main-side">
    <div class="stack">
     <article class="card">
      <div class="card-head"><div class="detail-title"><span class="cat-tile cat-tile-lg"><cp:icon name="cat-${report.categoryCode}"/></span><div><span class="ref"><c:out value="${report.reference}"/></span><h2 class="mt-1"><c:out value="${report.title}"/></h2><p class="muted text-sm">${ui[report.category]} · ${ui[report.department]}</p></div></div><span class="chip status status-${report.status}">${ui[report.status]}</span></div>
      <div class="card-body"><c:set var="stepIndex" value="${report.step}"/><c:set var="stepStatus" value="${report.status}"/><%@ include file="/WEB-INF/views/fragments/stepper.jspf" %></div>
      <div class="card-body"><dl class="facts">
       <div><dt>${ui['Zone']}</dt><dd><cp:icon name="map-pin"/><c:out value="${ui[zone]}"/></dd></div>
       <div><dt>${ui['Submitted']}</dt><dd><time datetime="${report.createdAt}">${report.createdAt}</time></dd></div>
       <div class="wide"><dt>${ui['Location']}</dt><dd><c:out value="${report.address}"/></dd></div>
       <div><dt>${ui['Target resolution']}</dt><dd><span class="due ${report.overdue ? 'is-overdue' : ''}"><cp:icon name="timer"/><time datetime="${report.dueAt}" data-date-only>${report.dueAt}</time></span><c:if test="${report.overdue}"><span class="chip badge-overdue"><cp:icon name="triangle-alert"/>${ui['Overdue']}</span></c:if></dd></div>
       <div><dt>${ui['Last update']}</dt><dd><time datetime="${report.updatedAt}" data-relative>${report.updatedAt}</time></dd></div>
      </dl></div>
     </article>
    </div>
    <aside class="stack">
     <section class="card"><div class="card-head"><h2 class="card-title"><cp:icon name="clock"/>${ui['Public timeline']}</h2></div><div class="card-body"><ol class="timeline"><c:forEach items="${updates}" var="u"><li class="tl-item"><span class="tl-dot"><cp:icon name="check"/></span><div class="tl-head"><strong>${ui['event.' += u.action]}</strong><span class="chip status status-${u.status}">${ui[u.status]}</span></div><small><time datetime="${u.createdAt}">${u.createdAt}</time></small></li></c:forEach></ol></div><div class="card-foot help"><cp:icon name="lock"/> ${ui['Names, contact details and officer notes are not shown on this public page.']}</div></section>
    </aside>
   </div>
  </c:if>
 </div>
</main>
<%@ include file="fragments/footer.jspf" %>
