<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="pageTitle" value="${feed ? 'Nearby feed' : 'Community map'}"/>
<%@ include file="../fragments/head.jspf" %><%@ include file="../fragments/header.jspf" %>
<main id="main">
 <section class="page-head"><div class="container"><div class="page-head-row"><div><span class="eyebrow">${ui['Your neighbourhood']}</span><h1>${feed ? ui['Nearby feed'] : ui['Community map']}</h1><p>${feed ? ui['Issues reported by other citizens around you. Support the ones that affect you too.'] : ui['Every active complaint across Jaipur, ranked by how many citizens reported it.']}</p></div>
  <div class="page-actions"><c:if test="${feed}"><a class="btn btn-light" href="${ctx}/community"><cp:icon name="map"/>${ui['Whole city map']}</a></c:if><c:if test="${isCitizen}"><a class="btn btn-accent" href="${ctx}/citizen/reports/new"><cp:icon name="plus"/>${ui['Report an issue']}</a></c:if></div></div></div></section>
 <div class="container page-body stack">
  <c:if test="${param.supported != null}"><div class="alert alert-success" role="status"><cp:icon name="thumbs-up"/><p>${ui['Your support has been recorded.']}</p></div></c:if>
  <section class="card">
   <form method="get" class="area-bar form" data-address-widget data-area-widget data-lat-input="areaLatitude" data-lon-input="areaLongitude" data-has-area="${hasArea}">
    <div class="field field-search"><label for="areaAddress">${ui['Explore an area']}</label><div class="address-search"><input id="areaAddress" data-address autocomplete="off" placeholder="${ui['Search a locality, e.g. Malviya Nagar or जौहरी बाज़ार']}" aria-autocomplete="list" aria-controls="area-suggestions" aria-expanded="false"><div id="area-suggestions" class="address-suggestions" role="listbox" hidden></div></div></div>
    <input type="hidden" id="areaLatitude" name="latitude" value="${areaLatitude}"><input type="hidden" id="areaLongitude" name="longitude" value="${areaLongitude}">
    <div class="field"><label for="radius">${ui['Within']}</label><select id="radius" name="radius"><option value="2" ${areaRadius eq 2 ? 'selected' : ''}>${ui['2 km']}</option><option value="5" ${areaRadius eq 5 ? 'selected' : ''}>${ui['5 km']}</option><option value="10" ${areaRadius eq 10 ? 'selected' : ''}>${ui['10 km']}</option><option value="50" ${areaRadius eq 50 ? 'selected' : ''}>${ui['50 km']}</option></select></div>
    <button type="button" class="btn btn-outline" data-locate><cp:icon name="locate-fixed"/>${ui['Use my location']}</button>
    <button type="submit" class="btn btn-primary"><cp:icon name="search"/>${ui['Update area']}</button>
   </form>
   <div class="card-foot"><div class="zone-scroller" aria-label="${ui['Jump to a zone']}"><a class="zone-chip" href="?radius=${areaRadius}"><cp:icon name="globe"/>${ui['All Jaipur']}</a><c:forEach items="${zones}" var="z"><a class="zone-chip" href="?latitude=${z.latitude}&amp;longitude=${z.longitude}&amp;radius=${areaRadius}"><cp:icon name="map-pin"/><c:out value="${language eq 'hi' ? z.hindi : z.name}"/></a></c:forEach></div><p class="help mt-1" data-map-message role="status"><c:choose><c:when test="${hasArea}">${ui['Showing reports in your selected area.']}</c:when><c:otherwise>${ui['Showing recent reports across Jaipur. Choose an area or zone to focus.']}</c:otherwise></c:choose></p></div>
  </section>
  <div class="feed-layout">
   <section class="feed-list" aria-labelledby="issues-heading">
    <div class="row-between"><h2 id="issues-heading">${ui['Community issues']} <span class="muted text-sm">(<c:out value="${reports.size()}"/>)</span></h2><label class="priority-filter"><span class="muted text-sm">${ui['Priority']}</span><select class="input" data-priority-filter aria-label="${ui['Filter priority']}"><option value="all">${ui['All priorities']}</option><option value="high">${ui['High']}</option><option value="medium">${ui['Medium']}</option><option value="low">${ui['Low']}</option></select></label></div>
    <c:if test="${empty reports}"><section class="card empty"><cp:icon name="map"/><h3>${ui['No reports in this area.']}</h3><p>${ui['Try a wider area or another location.']}</p></section></c:if>
    <c:forEach items="${reports}" var="r"><article id="report-${r.id}" class="card feed-card" data-community-report data-id="${r.id}" data-latitude="${r.latitude}" data-longitude="${r.longitude}" data-title="<c:out value='${r.title}'/>" data-reference="${r.reference}" data-status="${r.status}" data-priority="${r.priority}" data-count="${r.repeatCount}" data-code="${r.categoryCode}" data-category="${ui[r.category]}">
     <div class="feed-card-top"><span class="cat-tile"><cp:icon name="cat-${r.categoryCode}"/></span><div class="report-main">
      <div class="report-meta"><span class="ref"><c:out value="${r.reference}"/></span><span aria-hidden="true">·</span><span>${ui[r.category]}</span><span aria-hidden="true">·</span><time datetime="${r.createdAt}" data-relative>${r.createdAt}</time></div>
      <h3><a href="${ctx}/community/${r.id}"><c:out value="${r.title}"/></a></h3>
      <p class="report-sub"><cp:icon name="map-pin"/><span><c:out value="${r.address}"/></span></p>
      <div class="chips mt-1"><span class="chip priority-${r.priority}"><cp:icon name="flag"/>${ui[r.priority]} ${ui['priority']}</span><span class="chip status status-${r.status}">${ui[r.status]}</span><span class="chip badge-zone"><c:out value="${ui[r.zone]}"/></span><c:if test="${r.mine}"><span class="chip">${ui['Your report']}</span></c:if></div>
     </div></div>
     <div class="feed-card-foot"><div class="feed-progress"><span>${ui['Progress']}</span><progress class="progress" max="100" value="${r.progress}" aria-label="${ui['Work progress']}">${r.progress}%</progress><strong>${r.progress}%</strong></div>
      <span class="chip"><cp:icon name="users"/>${r.repeatCount} ${ui['citizens reported']}</span>
      <c:if test="${isCitizen and not r.mine and r.status ne 'CLOSED'}"><form method="post" action="${ctx}/citizen/community/${r.id}/support"><input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}"><button class="btn btn-outline btn-sm" type="submit" ${r.supported ? 'disabled' : ''}><cp:icon name="thumbs-up"/>${r.supported ? ui['Supported'] : ui['Support']} · ${r.supports}</button></form></c:if>
      <a class="link" href="${ctx}/community/${r.id}">${ui['View progress']}<cp:icon name="arrow-right"/></a></div>
    </article></c:forEach>
    <button type="button" class="btn btn-outline btn-block" data-show-more hidden><cp:icon name="chevron-down"/><span></span></button>
   </section>
   <aside class="feed-map"><section class="card card-pad"><div class="map map-lg" data-community-map data-latitude="${areaLatitude}" data-longitude="${areaLongitude}" data-radius="${areaRadius}" aria-label="${ui['Community reports map']}"></div>
    <div class="map-legend"><span><i class="legend-dot high"></i>${ui['High · 5+ citizens']}</span><span><i class="legend-dot medium"></i>${ui['Medium · 2–4 citizens']}</span><span><i class="legend-dot low"></i>${ui['Low · 1 citizen']}</span><span><i class="legend-dot closed"></i>${ui['Verified closed']}</span></div>
    <p class="help mt-1" data-community-message>${ui['Pin colour shows priority and the icon shows the service. Numbers count citizens reporting the same issue.']}</p></section></aside>
  </div>
 </div>
</main><%@ include file="../fragments/footer.jspf" %>
