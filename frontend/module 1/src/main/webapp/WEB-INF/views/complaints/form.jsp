<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="pageTitle" value="Report an issue"/>
<%@ include file="../fragments/head.jspf" %><%@ include file="../fragments/header.jspf" %>
<main id="main" class="container app-main">
<a class="text-link" href="<c:url value='/citizen/reports'/>">${ui['← My reports']}</a>
<div class="page-heading"><div><span class="eyebrow">${ui['NEW REPORT']}</span><h1>${ui['Report an issue']}</h1><p>${ui['Add a photograph and mark the place that needs attention.']}</p></div></div>
<form:form method="post" action="${pageContext.request.contextPath}/citizen/reports" modelAttribute="complaintForm" enctype="multipart/form-data" cssClass="civic-form surface report-form" data-report-form="true">
 <c:if test="${not empty requestScope['org.springframework.validation.BindingResult.complaintForm'].allErrors}"><div class="notice notice-error" role="alert"><c:forEach items="${requestScope['org.springframework.validation.BindingResult.complaintForm'].allErrors}" var="error"><p><c:out value="${ui[error.defaultMessage]}"/></p></c:forEach></div></c:if>
 <div class="two-columns"><section><h2>${ui['Issue details']}</h2><label for="title">${ui['Short title']}</label><form:input path="title" id="title" required="required" minlength="5" maxlength="120" placeholder="${ui['e.g. Pothole near the park entrance']}"/>
 <label for="categoryId">${ui['Category']}</label><form:select path="categoryId" id="categoryId" required="required"><form:option value="">${ui['Choose a category']}</form:option><c:forEach items="${categories}" var="category"><form:option value="${category.id}">${ui[category.name]}</form:option></c:forEach></form:select>
 <label for="description">${ui['What needs attention?']}</label><form:textarea path="description" id="description" rows="5" required="required" minlength="15" maxlength="2000"/>
 <div data-photo-widget><label for="photo">${ui['Issue photograph']}</label><input type="file" id="photo" name="photo" accept="image/png,image/jpeg" required data-photo-input>
 <div class="photo-actions"><button type="button" class="button button-small button-outline" data-camera>${ui['Take a photograph']}</button><button type="button" class="button button-small button-outline" data-remove-photo hidden>${ui['Remove photo']}</button></div>
 <figure class="photo-preview" hidden><img data-photo-preview alt="${ui['Selected issue photograph']}"><figcaption data-photo-caption></figcaption></figure><p class="field-help" data-photo-message role="status">${ui['PNG or JPEG, up to 5 MB.']}</p>
 <input type="file" accept="image/*" capture="environment" data-native-camera hidden>
 <dialog class="camera-dialog"><h2>${ui['Take a photograph']}</h2><video autoplay playsinline muted></video><p class="field-help" data-camera-message role="status"></p><div class="photo-actions"><button type="button" class="button button-primary" data-shutter>${ui['Use photo']}</button><button type="button" class="button button-outline" data-close-camera>${ui['Cancel']}</button></div></dialog></div>
 </section><section data-address-widget data-lat-input="latitude" data-lon-input="longitude" data-report-location>
 <h2>${ui['Issue location']}</h2><label for="address">${ui['Address or landmark']}</label><div class="address-search"><form:input path="address" id="address" required="required" minlength="5" maxlength="250" autocomplete="off" aria-autocomplete="list" aria-controls="address-suggestions" aria-expanded="false" data-address="true" placeholder="${ui['Start typing a street, area or landmark']}"/><div id="address-suggestions" class="address-suggestions" role="listbox" hidden></div></div>
 <form:hidden path="latitude" id="latitude"/><form:hidden path="longitude" id="longitude"/><form:hidden path="relatedReportId" id="relatedReportId"/>
 <div class="location-actions"><button type="button" class="button button-small button-outline" data-locate>${ui['Use my location']}</button></div><p class="field-help" data-map-message role="status">${ui['Select an address suggestion or mark the place on the map.']}</p>
 <div class="location-map" data-location-picker aria-label="${ui['Issue location']}"></div><p class="field-help">${ui['Address search uses OpenStreetMap data via Photon.']}</p>
 <div class="nearby-panel" data-nearby aria-live="polite"></div>
 </section></div>
 <div class="report-submit"><p class="field-help">${ui['Check the photograph and location before submitting. Submitted reports cannot be edited; you can delete and restore them from My Reports.']}</p><button class="button button-primary" type="submit">${ui['Submit report']}</button></div>
</form:form>
</main><%@ include file="../fragments/footer.jspf" %>
