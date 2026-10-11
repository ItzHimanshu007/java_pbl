<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="pageTitle" value="Create your account"/>
<%@ include file="../fragments/head.jspf" %><%@ include file="../fragments/header.jspf" %>
<main id="main" class="auth-wrap">
 <aside class="auth-aside">
  <span class="eyebrow">${ui['Join your city']}</span>
  <h1>${ui['A better Jaipur starts with one report']}</h1>
  <p>${ui['Create a free citizen account to report issues, support your neighbours and verify repairs.']}</p>
  <ul class="benefits">
   <li><cp:icon name="map-pinned"/><div><strong>${ui['Report from anywhere in Jaipur']}</strong>${ui['Search more than 100 localities or use your current location.']}</div></li>
   <li><cp:icon name="thumbs-up"/><div><strong>${ui['Support issues near you']}</strong>${ui['Linked reports from more citizens raise an issue’s priority.']}</div></li>
   <li><cp:icon name="lock"/><div><strong>${ui['Your details stay private']}</strong>${ui['Your email, phone and address are never shown to other citizens.']}</div></li>
  </ul>
 </aside>
 <section class="auth-main"><div class="auth-card card card-pad" aria-labelledby="register-title">
  <h2 id="register-title">${ui['Create a citizen account']}</h2><p class="lead">${ui['A few details to get started.']}</p>
  <form:form method="post" action="${ctx}/register" modelAttribute="registrationForm" cssClass="form">
   <c:if test="${not empty requestScope['org.springframework.validation.BindingResult.registrationForm'].allErrors}"><div class="alert alert-error" role="alert"><cp:icon name="circle-alert"/><div><c:forEach items="${requestScope['org.springframework.validation.BindingResult.registrationForm'].allErrors}" var="error"><p><c:out value="${ui[error.defaultMessage]}"/></p></c:forEach></div></div></c:if>
   <div class="field"><label for="displayName">${ui['Your name']}</label><form:input path="displayName" id="displayName" autocomplete="name" required="required" maxlength="80" placeholder="${ui['Your full name']}"/></div>
   <div class="field"><label for="email">${ui['Email address']}</label><form:input path="email" id="email" type="email" autocomplete="username" required="required" maxlength="190" placeholder="${ui['you@example.com']}"/></div>
   <div class="field"><label for="password">${ui['Password']}</label><div class="password-field"><input id="password" name="password" type="password" autocomplete="new-password" required minlength="10" maxlength="72" aria-describedby="password-help"><button class="password-toggle" type="button" data-password-target="password" aria-label="${ui['Show password']}">${ui['Show']}</button></div><span class="help" id="password-help">${ui['Use at least 10 characters.']}</span></div>
   <div class="field"><label for="confirmPassword">${ui['Confirm password']}</label><input id="confirmPassword" name="confirmPassword" type="password" autocomplete="new-password" required minlength="10" maxlength="72"></div>
   <button class="btn btn-primary btn-lg btn-block" type="submit"><cp:icon name="user-plus"/>${ui['Create account']}</button>
  </form:form>
  <p class="auth-switch">${ui['Already have an account?']} <a href="${ctx}/login">${ui['Sign in']}</a></p>
 </div></section>
</main><%@ include file="../fragments/footer.jspf" %>
