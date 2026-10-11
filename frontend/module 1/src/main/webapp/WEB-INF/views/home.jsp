<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="pageTitle" value="Report and track civic issues in Jaipur"/>
<%@ include file="fragments/head.jspf" %><%@ include file="fragments/header.jspf" %>
<main id="main">
 <section class="hero"><div class="container hero-inner">
  <div class="hero-copy">
   <span class="eyebrow"><cp:icon name="sparkles"/>${ui['Jaipur 311 · Citizen services']}</span>
   <h1 class="hero-title">${ui['Report it. Track it.']} <em>${ui['Get it fixed.']}</em></h1>
   <p class="hero-lead">${ui['Potholes, dark streetlights, overflowing garbage or sewer leaks anywhere in Jaipur — report with a photo and a map pin. Your complaint goes to the right department and you can follow every step until you confirm the repair.']}</p>
   <div class="hero-cta"><a class="btn btn-accent btn-lg" href="${ctx}/citizen/reports/new"><cp:icon name="plus"/>${ui['Report an issue']}</a><a class="btn btn-outline btn-lg" href="${ctx}/community"><cp:icon name="map"/>${ui['View community map']}</a></div>
   <form class="track-box" method="get" action="${ctx}/track"><label for="hero-ref"><cp:icon name="scan-search"/>${ui['Track your complaint']}</label><div class="track-box-row"><input id="hero-ref" name="ref" maxlength="40" required placeholder="CP-2026-XXXXXXXXXXXX" autocomplete="off"><button class="btn btn-primary" type="submit">${ui['Track']}<cp:icon name="arrow-right"/></button></div><p class="help mt-1">${ui['No sign-in needed. Enter the reference number from your acknowledgement.']}</p></form>
   <div class="hero-badges"><span><cp:icon name="circle-check"/>${ui['Anonymous reporting']}</span><span><cp:icon name="circle-check"/>${ui['Hindi and English']}</span><span><cp:icon name="circle-check"/>${ui['Photo evidence before and after']}</span></div>
  </div>
  <div class="hero-visual"><%@ include file="fragments/hero-art.jspf" %></div>
 </div></section>

 <c:if test="${not empty publicStats}"><section class="stats-band" aria-label="${ui['City-wide complaint figures']}"><div class="container stats-grid">
  <div class="stat"><strong><c:out value="${publicStats.total}"/></strong><span>${ui['Complaints registered']}</span></div>
  <div class="stat"><strong><c:out value="${publicStats.resolved}"/></strong><span>${ui['Resolved']} (<c:out value="${publicStats.resolvedPercent}"/>%)</span></div>
  <div class="stat"><strong><c:out value="${publicStats.inProgress}"/></strong><span>${ui['Being worked on']}</span></div>
  <div class="stat"><strong><c:out value="${publicStats.awaiting}"/></strong><span>${ui['Awaiting review']}</span></div>
  <div class="stat"><strong><c:out value="${publicStats.averageHours}"/></strong><span>${ui['Average hours to verified fix']}</span></div>
  <div class="stat"><strong><c:out value="${publicStats.zones}"/></strong><span>${ui['City zones covered']}</span></div>
 </div></section></c:if>

 <section class="section" id="services"><div class="container">
  <div class="section-head"><span class="eyebrow">${ui['Service catalogue']}</span><h2>${ui['What would you like to report?']}</h2><p>${ui['Choose a service. Each one is routed to its department with a target resolution time.']}</p></div>
  <div class="service-grid">
   <c:forEach items="${serviceCategories}" var="sc"><a class="service-tile" href="${ctx}/citizen/reports/new?category=${sc.id}"><span class="cat-tile"><cp:icon name="cat-${sc.code}"/></span><span><strong>${ui[sc.name]}</strong><small><cp:icon name="timer"/>${ui['Target']}: <c:out value="${sc.slaAmount}"/> ${ui[sc.slaUnit]}</small></span></a></c:forEach>
  </div>
 </div></section>

 <section class="section section-alt" id="how-it-works"><div class="container">
  <div class="section-head"><span class="eyebrow">${ui['How it works']}</span><h2>${ui['From your phone to a verified repair']}</h2><p>${ui['Every complaint follows the same accountable path, with a public timeline.']}</p></div>
  <ol class="process">
   <li class="process-step"><span class="num">01</span><span class="cat-tile"><cp:icon name="camera"/></span><h3>${ui['Report with a photo']}</h3><p>${ui['Pick a service, add a photo and drop a pin anywhere in Jaipur. Report anonymously if you prefer.']}</p></li>
   <li class="process-step"><span class="num">02</span><span class="cat-tile"><cp:icon name="building-2"/></span><h3>${ui['Routed to the department']}</h3><p>${ui['An administrator reviews it and assigns an officer from the responsible department.']}</p></li>
   <li class="process-step"><span class="num">03</span><span class="cat-tile"><cp:icon name="hard-hat"/></span><h3>${ui['Work is tracked']}</h3><p>${ui['The officer posts updates and uploads an after photo when the repair is done.']}</p></li>
   <li class="process-step"><span class="num">04</span><span class="cat-tile"><cp:icon name="badge-check"/></span><h3>${ui['You verify the fix']}</h3><p>${ui['Compare before and after. Close the complaint, or reopen it if more work is needed.']}</p></li>
  </ol>
 </div></section>

 <section class="section" id="anonymous"><div class="container">
  <div class="callout"><span class="cat-tile"><cp:icon name="venetian-mask"/></span><div><h3>${ui['Report without revealing who you are']}</h3><p>${ui['Turn on anonymous reporting and your name, email and account are never attached to the complaint. Only a one-way encrypted fingerprint (HMAC-SHA256) is stored, so you can still track, verify or delete it — but officers and administrators cannot see who reported it.']}</p></div><a class="btn btn-light" href="${ctx}/citizen/reports/new"><cp:icon name="shield-check"/>${ui['Report anonymously']}</a></div>
 </div></section>

 <section class="section section-alt" id="zones"><div class="container">
  <div class="section-head"><span class="eyebrow">${ui['Zones served']}</span><h2>${ui['Across every part of Jaipur']}</h2><p>${ui['From the Walled City to Jagatpura — search more than 100 localities by name in English or Hindi.']}</p></div>
  <div class="zone-cloud"><c:forEach items="${zones}" var="z"><a class="zone-chip" href="${ctx}/community?latitude=${z.latitude}&amp;longitude=${z.longitude}&amp;radius=5"><cp:icon name="map-pin"/><c:out value="${language eq 'hi' ? z.hindi : z.name}"/><small><c:out value="${z.places.size()}"/> ${ui['localities']}</small></a></c:forEach></div>
 </div></section>
</main>
<%@ include file="fragments/footer.jspf" %>
