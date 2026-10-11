<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="pageTitle" value="Sign in"/>
<%@ include file="../fragments/head.jspf" %><%@ include file="../fragments/header.jspf" %>
<main id="main" class="auth-wrap">
 <aside class="auth-aside">
  <span class="eyebrow">${ui['Jaipur 311 · Citizen services']}</span>
  <h1>${ui['Welcome back to CivicPulse Jaipur']}</h1>
  <p>${ui['One account for citizens, field officers and administrators.']}</p>
  <ul class="benefits">
   <li><cp:icon name="clipboard-list"/><div><strong>${ui['Follow every complaint']}</strong>${ui['See the department, officer and each update in one timeline.']}</div></li>
   <li><cp:icon name="badge-check"/><div><strong>${ui['Verify repairs yourself']}</strong>${ui['Compare before and after photos and close the complaint only when it is fixed.']}</div></li>
   <li><cp:icon name="venetian-mask"/><div><strong>${ui['Report anonymously']}</strong>${ui['Your identity is replaced by a one-way encrypted fingerprint.']}</div></li>
  </ul>
 </aside>
 <section class="auth-main"><div class="auth-card card card-pad" aria-labelledby="login-title">
  <h2 id="login-title">${ui['Sign in']}</h2><p class="lead">${ui['Enter the email and password for your account.']}</p>
  <div class="stack-sm">
   <c:if test="${registrationCompleted}"><div class="alert alert-success" role="status"><cp:icon name="circle-check"/><p>${ui['Your citizen account is ready. Sign in to continue.']}</p></div></c:if>
   <c:if test="${param.loggedOut != null}"><div class="alert alert-success" role="status"><cp:icon name="circle-check"/><p>${ui['You have signed out.']}</p></div></c:if>
   <c:if test="${param.expired != null}"><div class="alert alert-info" role="status"><cp:icon name="info"/><p>${ui['Sign in to access your workspace.']}</p></div></c:if>
   <c:if test="${locationWelcome}"><div class="alert alert-info"><cp:icon name="locate-fixed"/><div><p><strong>${ui['Find issues near you']}</strong></p><p>${ui['Allow location access to select the place when you take a photograph. You can also choose an address manually.']}</p><button type="button" class="btn btn-outline btn-sm mt-1" data-welcome-location>${ui['Enable location']}</button><p class="help" data-welcome-message role="status"></p></div></div></c:if>
  </div>
  <form:form method="post" action="${ctx}/login" modelAttribute="loginForm" cssClass="form mt-3">
   <c:if test="${not empty requestScope['org.springframework.validation.BindingResult.loginForm'].allErrors}"><div class="alert alert-error" role="alert"><cp:icon name="circle-alert"/><div><c:forEach items="${requestScope['org.springframework.validation.BindingResult.loginForm'].allErrors}" var="error"><p><c:out value="${ui[error.defaultMessage]}"/></p></c:forEach></div></div></c:if>
   <div class="field"><label for="email">${ui['Email address']}</label><form:input path="email" id="email" type="email" autocomplete="username" required="required" maxlength="190" placeholder="${ui['you@example.com']}"/></div>
   <div class="field"><label for="password">${ui['Password']}</label><div class="password-field"><input id="password" name="password" type="password" autocomplete="current-password" required maxlength="72"><button type="button" class="password-toggle" data-password-target="password" aria-label="${ui['Show password']}">${ui['Show']}</button></div></div>
   <button class="btn btn-primary btn-lg btn-block" type="submit"><cp:icon name="log-in"/>${ui['Sign in']}</button>
  </form:form>
  <p class="auth-switch">${ui['New here?']} <a href="${ctx}/register">${ui['Create a citizen account']}</a></p>
  <c:if test="${demoMode}"><div class="demo-box"><h3><cp:icon name="sparkles"/>${ui['Demo accounts']}</h3><p class="help">${ui['Fill the form with a sample account. Password for all:']} <code>CivicDemo!2026</code></p><div class="row"><button type="button" class="btn btn-outline btn-sm" data-demo-email="citizen@civicpulse.demo"><cp:icon name="user"/>${ui['Citizen']}</button><button type="button" class="btn btn-outline btn-sm" data-demo-email="officer@civicpulse.demo"><cp:icon name="hard-hat"/>${ui['Officer']}</button><button type="button" class="btn btn-outline btn-sm" data-demo-email="admin@civicpulse.demo"><cp:icon name="landmark"/>${ui['Administrator']}</button></div></div></c:if>
 </div></section>
</main><%@ include file="../fragments/footer.jspf" %>
