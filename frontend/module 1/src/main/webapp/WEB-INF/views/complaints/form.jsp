<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="pageTitle" value="Report an issue"/>
<%@ include file="../fragments/head.jspf" %><%@ include file="../fragments/header.jspf" %>
<main id="main">
 <section class="page-head"><div class="container">
  <nav class="breadcrumbs" aria-label="${ui['Breadcrumb']}"><a href="${ctx}/citizen/reports">${ui['My reports']}</a><cp:icon name="chevron-right"/><span>${ui['New complaint']}</span></nav>
  <div class="page-head-row"><div><span class="eyebrow">${ui['New complaint']}</span><h1>${ui['Report a civic issue']}</h1><p>${ui['Four quick steps. Your complaint is sent to the responsible department with a target resolution time.']}</p></div></div>
 </div></section>
 <div class="container page-body">
 <form:form method="post" action="${ctx}/citizen/reports" modelAttribute="complaintForm" enctype="multipart/form-data" cssClass="report-layout" data-report-form="true">
  <div class="card">
   <c:if test="${not empty requestScope['org.springframework.validation.BindingResult.complaintForm'].allErrors}"><div class="form-section"><div class="alert alert-error" role="alert"><cp:icon name="circle-alert"/><div><c:forEach items="${requestScope['org.springframework.validation.BindingResult.complaintForm'].allErrors}" var="error"><p><c:out value="${ui[error.defaultMessage]}"/></p></c:forEach></div></div></div></c:if>

   <fieldset class="form-section form" id="step-service"><legend class="visually-hidden">${ui['Choose the service']}</legend><div class="section-title" aria-hidden="true"><span class="num">1</span><div><h2>${ui['Choose the service']}</h2><p>${ui['Pick the option that best matches the problem.']}</p></div></div>
    <div class="category-grid" role="radiogroup" aria-label="${ui['Service']}">
     <c:forEach items="${categories}" var="k"><label class="category-option"><input type="radio" name="categoryId" value="${k.id}" required ${complaintForm.categoryId eq k.id ? 'checked' : ''} data-code="${k.code}" data-name="${ui[k.name]}" data-sla="${k.slaAmount} ${ui[k.slaUnit]}"><cp:icon name="circle-check" cls="check"/><span class="cat-tile"><cp:icon name="cat-${k.code}"/></span><span class="name">${ui[k.name]}</span><small><cp:icon name="timer"/>${k.slaAmount} ${ui[k.slaUnit]}</small></label></c:forEach>
    </div>
   </fieldset>

   <fieldset class="form-section form" id="step-details"><legend class="visually-hidden">${ui['Describe the problem']}</legend><div class="section-title" aria-hidden="true"><span class="num">2</span><div><h2>${ui['Describe the problem']}</h2><p>${ui['A clear title and a photo help the department act faster.']}</p></div></div>
    <div class="field"><div class="label-row"><label for="title">${ui['Short title']} <span class="req">*</span></label><span class="counter" data-counter-for="title"></span></div><form:input path="title" id="title" required="required" minlength="5" maxlength="120" placeholder="${ui['e.g. Pothole near the park entrance']}" data-counter="120"/><div class="suggestions" data-suggestions aria-label="${ui['Quick titles']}"></div></div>
    <div class="field"><div class="label-row"><label for="description">${ui['What needs attention?']} <span class="req">*</span></label><span class="counter" data-counter-for="description"></span></div><form:textarea path="description" id="description" rows="5" required="required" minlength="15" maxlength="2000" data-counter="2000" placeholder="${ui['Describe the problem, how long it has been there and any safety risk.']}"/></div>
    <div class="field" data-photo-widget><span class="label">${ui['Issue photograph']} <span class="req">*</span></span>
     <div class="photo-box"><cp:icon name="camera" cls="icon-lg"/><p class="help">${ui['PNG or JPEG, up to 5 MB. Photos are re-encoded to remove hidden metadata.']}</p>
      <label class="visually-hidden" for="photo">${ui['Choose a photograph']}</label><input class="mt-2" type="file" id="photo" name="photo" accept="image/png,image/jpeg" required data-photo-input>
      <div class="photo-actions"><button type="button" class="btn btn-outline btn-sm" data-camera><cp:icon name="camera"/>${ui['Take a photograph']}</button><button type="button" class="btn btn-ghost btn-sm" data-remove-photo hidden><cp:icon name="x"/>${ui['Remove photo']}</button></div>
      <figure class="photo-preview" hidden><img data-photo-preview alt="${ui['Selected issue photograph']}"><figcaption data-photo-caption></figcaption></figure>
      <p class="help mt-1" data-photo-message role="status"></p>
     </div>
     <input type="file" accept="image/*" capture="environment" data-native-camera hidden>
     <dialog class="camera-dialog"><h2>${ui['Take a photograph']}</h2><video autoplay playsinline muted></video><p class="help" data-camera-message role="status"></p><div class="photo-actions"><button type="button" class="btn btn-primary" data-shutter><cp:icon name="camera"/>${ui['Use photo']}</button><button type="button" class="btn btn-outline" data-close-camera>${ui['Cancel']}</button></div></dialog>
    </div>
   </fieldset>

   <fieldset class="form-section form" id="step-location" data-address-widget data-lat-input="latitude" data-lon-input="longitude" data-report-location><legend class="visually-hidden">${ui['Mark the location']}</legend><div class="section-title" aria-hidden="true"><span class="num">3</span><div><h2>${ui['Mark the location']}</h2><p>${ui['Choose a locality, search an address, use your location or tap the map. Drag the pin to adjust.']}</p></div></div>
    <div class="field-row">
     <div class="field"><label for="locality">${ui['Jaipur locality']}</label><select id="locality" data-locality-select><option value="">${ui['Select a locality…']}</option><c:forEach items="${zones}" var="z"><optgroup label="${language eq 'hi' ? z.hindi : z.name}"><c:forEach items="${z.places}" var="p"><option value="${p.latitude},${p.longitude}" data-label="${language eq 'hi' ? (p.hindi += ', ' += z.hindi += ', जयपुर') : (p.name += ', ' += z.name += ', Jaipur')}">${language eq 'hi' ? p.hindi : p.name}</option></c:forEach></optgroup></c:forEach></select></div>
     <div class="field"><label for="address">${ui['Address or landmark']} <span class="req">*</span></label><div class="address-search"><form:input path="address" id="address" required="required" minlength="5" maxlength="250" autocomplete="off" aria-autocomplete="list" aria-controls="address-suggestions" aria-expanded="false" data-address="true" placeholder="${ui['Street, area or landmark']}"/><div id="address-suggestions" class="address-suggestions" role="listbox" hidden></div></div></div>
    </div>
    <form:hidden path="latitude" id="latitude"/><form:hidden path="longitude" id="longitude"/><form:hidden path="relatedReportId" id="relatedReportId"/>
    <div class="location-actions"><button type="button" class="btn btn-outline btn-sm" data-locate><cp:icon name="locate-fixed"/>${ui['Use my location']}</button><span class="help" data-map-message role="status">${ui['Select a locality or mark the place on the map.']}</span></div>
    <div class="map map-picker" data-location-picker aria-label="${ui['Issue location map']}"></div>
    <p class="help">${ui['Map data © OpenStreetMap contributors. Address search uses Photon with an offline list of Jaipur localities.']}</p>
    <div class="nearby-panel" data-nearby aria-live="polite"></div>
   </fieldset>

   <fieldset class="form-section form" id="step-privacy"><legend class="visually-hidden">${ui['Your privacy']}</legend><div class="section-title" aria-hidden="true"><span class="num">4</span><div><h2>${ui['Your privacy']}</h2><p>${ui['Choose whether your name appears on this complaint.']}</p></div></div>
    <label class="anon-toggle" for="anonymous"><span class="switch"><input type="checkbox" id="anonymous" name="anonymous" value="true" ${complaintForm.anonymous ? 'checked' : ''} data-anonymous><span></span></span><span><strong><cp:icon name="venetian-mask"/>${ui['Report anonymously']}</strong><p>${ui['Your name, email and account are not stored with this complaint. Only a one-way encrypted fingerprint (HMAC-SHA256) links it to you, so you can still track, verify or delete it. Officers and administrators will see “Anonymous citizen”.']}</p></span></label>
   </fieldset>
  </div>

  <aside class="stack sticky">
   <section class="card summary-card"><div class="card-head"><h2 class="card-title"><cp:icon name="list-checks"/>${ui['Review before submitting']}</h2></div>
    <div class="card-body"><ul class="checklist">
     <li data-check="service"><span class="tick"><cp:icon name="check"/></span><span>${ui['Service']}<span class="value" data-value>${ui['Not selected']}</span></span></li>
     <li data-check="details"><span class="tick"><cp:icon name="check"/></span><span>${ui['Title and description']}<span class="value" data-value>${ui['Not added']}</span></span></li>
     <li data-check="photo"><span class="tick"><cp:icon name="check"/></span><span>${ui['Photograph']}<span class="value" data-value>${ui['Not added']}</span></span></li>
     <li data-check="location"><span class="tick"><cp:icon name="check"/></span><span>${ui['Location']}<span class="value" data-value>${ui['Not selected']}</span></span></li>
     <li data-check="identity" class="is-done"><span class="tick"><cp:icon name="check"/></span><span>${ui['Identity']}<span class="value" data-value data-named="${ui['Your name is visible to staff']}" data-anon="${ui['Anonymous — identity hashed']}">${ui['Your name is visible to staff']}</span></span></li>
    </ul>
    <div class="sla-note mt-3" data-sla-note hidden><cp:icon name="timer"/><span>${ui['Target resolution time:']} <strong data-sla-value></strong></span></div></div>
    <div class="card-foot"><button class="btn btn-accent btn-lg btn-block" type="submit"><cp:icon name="send"/>${ui['Submit complaint']}</button><p class="help mt-2">${ui['Submitted reports cannot be edited; you can delete and restore them from My reports.']}</p></div>
   </section>
  </aside>
 </form:form>
 </div>
</main>
<%@ include file="../fragments/footer.jspf" %>
