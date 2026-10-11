<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="pageTitle" value="Community issue"/>
<%@ include file="../fragments/head.jspf" %><%@ include file="../fragments/header.jspf" %>
<main id="main">
 <section class="page-head"><div class="container">
  <nav class="breadcrumbs" aria-label="${ui['Breadcrumb']}"><a href="${ctx}/feed">${ui['Nearby feed']}</a><cp:icon name="chevron-right"/><span><c:out value="${summary.reference}"/></span></nav>
  <div class="page-head-row"><div><span class="eyebrow"><cp:icon name="cat-${summary.categoryCode}"/>${ui[summary.category]}</span><h1 class="mt-1"><c:out value="${summary.title}"/></h1><div class="chips mt-2"><span class="chip status status-${summary.status}">${ui[summary.status]}</span><span class="chip priority-${summary.priority}"><cp:icon name="flag"/>${ui[summary.priority]} ${ui['priority']}</span><span class="chip badge-zone"><c:out value="${ui[summary.zone]}"/></span></div></div>
  <c:if test="${summary.mine or isAdmin}"><div class="page-actions"><a class="btn btn-light" href="${ctx}/reports/${summary.id}"><cp:icon name="file-text"/>${ui['Open full report']}</a></div></c:if></div>
 </div></section>
 <div class="container page-body stack">
  <section class="card card-pad"><c:set var="stepIndex" value="${summary.status eq 'REPORTED' ? 0 : summary.status eq 'ASSIGNED' ? 1 : summary.status eq 'IN_PROGRESS' ? 2 : summary.status eq 'RESOLVED' ? 3 : 4}"/><c:set var="stepStatus" value="${summary.status}"/><%@ include file="/WEB-INF/views/fragments/stepper.jspf" %></section>
  <div class="layout-main-side">
   <div class="stack">
    <section class="card"><div class="card-head"><h2 class="card-title"><cp:icon name="info"/>${ui['Current progress']}</h2><strong>${summary.progress}%</strong></div><div class="card-body"><progress class="progress" max="100" value="${summary.progress}" aria-label="${ui['Work progress']}"></progress><p class="help mt-1">${ui['Progress shows the current stage of work, not an estimate of time remaining.']}</p>
     <dl class="facts mt-3"><div class="wide"><dt>${ui['Location']}</dt><dd><cp:icon name="map-pin"/><c:out value="${summary.address}"/></dd></div><div><dt>${ui['Citizens reporting this issue']}</dt><dd><cp:icon name="users"/>${summary.repeatCount}</dd></div><div><dt>${ui['Supporters']}</dt><dd><cp:icon name="thumbs-up"/>${summary.supports}</dd></div><div><dt>${ui['Reported']}</dt><dd><time datetime="${summary.createdAt}">${summary.createdAt}</time></dd></div></dl></div></section>
    <section class="card"><div class="card-head"><h2 class="card-title"><cp:icon name="clock"/>${ui['Work timeline']}</h2></div><div class="card-body"><ol class="timeline"><c:forEach items="${updates}" var="update"><li class="tl-item"><span class="tl-dot"><cp:icon name="check"/></span><div class="tl-head"><strong>${ui['event.' += update.action]}</strong><span class="chip status status-${update.status}">${ui[update.status]}</span></div><small><time datetime="${update.createdAt}">${update.createdAt}</time></small></li></c:forEach></ol></div></section>
   </div>
   <aside class="stack"><section class="card"><div class="card-head"><h2 class="card-title"><cp:icon name="map-pin"/>${ui['Location']}</h2></div><div class="card-body"><div class="map map-sm" data-mini-map data-latitude="${summary.latitude}" data-longitude="${summary.longitude}" data-code="${summary.categoryCode}" data-status="${summary.status}" data-priority="${summary.priority}" aria-label="${ui['Report location map']}"></div></div></section>
    <section class="card card-pad"><h2 class="card-title"><cp:icon name="flag"/>${ui['How priority works']}</h2><p class="help mt-2">${ui['Repeated reports linked to the same issue raise its priority. Each citizen counts once. Nearby reports are only suggestions until someone confirms they describe the same issue.']}</p></section></aside>
  </div>
 </div>
</main><%@ include file="../fragments/footer.jspf" %>
