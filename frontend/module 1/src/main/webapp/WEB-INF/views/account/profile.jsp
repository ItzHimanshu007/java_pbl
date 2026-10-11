<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="pageTitle" value="Your account"/>
<%@ include file="../fragments/head.jspf" %><%@ include file="../fragments/header.jspf" %>
<main id="main">
 <section class="page-head"><div class="container"><div class="page-head-row"><div><span class="eyebrow">${ui['Account']}</span><h1>${ui['Your account']}</h1><p>${ui['Personal details, your home area and the way CivicPulse looks.']}</p></div></div></div></section>
 <div class="container page-body stack">
  <c:if test="${param.saved != null}"><div class="alert alert-success" role="status"><cp:icon name="circle-check"/><p>${ui['Your changes have been saved.']}</p></div></c:if>
  <div class="layout-main-side">
   <form:form method="post" action="${ctx}/account" modelAttribute="profileForm" cssClass="stack">
    <c:if test="${not empty requestScope['org.springframework.validation.BindingResult.profileForm'].allErrors}"><div class="alert alert-error" role="alert"><cp:icon name="circle-alert"/><div><c:forEach items="${requestScope['org.springframework.validation.BindingResult.profileForm'].allErrors}" var="error"><p><c:out value="${ui[error.defaultMessage]}"/></p></c:forEach></div></div></c:if>
    <section class="card"><div class="card-head"><h2 class="card-title"><cp:icon name="user"/>${ui['Personal information']}</h2></div><div class="card-body form"><p class="help">${ui['Additional details are optional. You can add them whenever you like.']}</p><div class="field-row">
     <div class="field"><label for="displayName">${ui['Display name']}</label><form:input path="displayName" id="displayName" required="required" maxlength="80" autocomplete="name"/></div>
     <div class="field"><label for="dateOfBirth">${ui['Date of birth']}</label><form:input path="dateOfBirth" id="dateOfBirth" type="date" autocomplete="bday"/></div>
     <div class="field"><label for="phone">${ui['Phone number']}</label><form:input path="phone" id="phone" type="tel" maxlength="24" autocomplete="tel" placeholder="+91 98765 43210"/></div>
     <div class="field"><label for="neighbourhood">${ui['Neighbourhood / ward']}</label><form:input path="neighbourhood" id="neighbourhood" maxlength="100"/></div></div></div></section>
    <section class="card" data-address-widget data-lat-input="homeLatitude" data-lon-input="homeLongitude"><div class="card-head"><h2 class="card-title"><cp:icon name="house"/>${ui['Home area']}</h2></div><div class="card-body form"><p class="help">${ui['Choose your home area to personalise the nearby feed.']}</p>
     <div class="field"><label for="address">${ui['Address or landmark']}</label><div class="address-search"><form:input path="address" id="address" maxlength="250" autocomplete="off" aria-autocomplete="list" aria-controls="address-suggestions" aria-expanded="false" data-address="true" placeholder="${ui['Search a Jaipur locality or address']}"/><div id="address-suggestions" class="address-suggestions" role="listbox" hidden></div></div></div>
     <form:hidden path="homeLatitude" id="homeLatitude"/><form:hidden path="homeLongitude" id="homeLongitude"/>
     <div class="field-row"><div class="field"><label for="city">${ui['City']}</label><form:input path="city" id="city" maxlength="80" autocomplete="address-level2" placeholder="${ui['Jaipur']}"/></div><div class="field"><label for="postalCode">${ui['PIN code']}</label><form:input path="postalCode" id="postalCode" maxlength="16" autocomplete="postal-code" placeholder="302001"/></div></div>
     <div class="location-actions"><button type="button" class="btn btn-outline btn-sm" data-locate><cp:icon name="locate-fixed"/>${ui['Use my location']}</button><button type="button" class="btn btn-ghost btn-sm" data-clear-location><cp:icon name="x"/>${ui['Clear home area']}</button><span class="help" data-map-message role="status">${ui['Search an address or select a place on the map.']}</span></div>
     <div class="map map-picker" data-location-picker aria-label="${ui['Choose home area']}"></div></div></section>
    <section class="card"><div class="card-head"><h2 class="card-title"><cp:icon name="sun-moon"/>${ui['Display preferences']}</h2></div><div class="card-body form"><div class="field-row"><div class="field"><label for="theme">${ui['Theme']}</label><form:select path="theme" id="theme"><form:option value="system">${ui['Use device setting']}</form:option><form:option value="light">${ui['Light']}</form:option><form:option value="dark">${ui['Dark']}</form:option></form:select></div><div class="field"><label for="language">${ui['Language']}</label><form:select path="language" id="language"><form:option value="en">English</form:option><form:option value="hi">हिन्दी</form:option></form:select></div></div><p class="help">${ui['These preferences apply across the app on every device after signing in.']}</p></div></section>
    <div class="form-actions"><button class="btn btn-primary btn-lg" type="submit"><cp:icon name="check"/>${ui['Save changes']}</button></div>
   </form:form>
   <aside class="stack sticky"><section class="card card-pad"><div class="row"><span class="avatar avatar-lg" aria-hidden="true"><c:out value="${fn:toUpperCase(fn:substring(displayName,0,1))}"/></span><div><h2><c:out value="${displayName}"/></h2><span class="chip">${ui[roleLabel]}</span></div></div>
    <dl class="facts mt-3"><div class="wide"><dt>${ui['Email address']}</dt><dd><c:out value="${accountEmail}"/></dd></div><div class="wide"><dt>${ui['Member since']}</dt><dd><time datetime="${memberSince}" data-date-only><c:out value="${memberSince}"/></time></dd></div></dl>
    <div class="alert alert-info mt-3"><cp:icon name="lock"/><p>${ui['Your contact details and date of birth are private. They are never shown in the community feed.']}</p></div></section></aside>
  </div>
 </div>
</main><%@ include file="../fragments/footer.jspf" %>
