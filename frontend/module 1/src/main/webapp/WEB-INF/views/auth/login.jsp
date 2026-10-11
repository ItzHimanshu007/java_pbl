<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="pageTitle" value="Sign in"/>
<%@ include file="../fragments/head.jspf" %><%@ include file="../fragments/header.jspf" %>
<main id="main" class="auth-shell container"><aside class="auth-story"><span class="eyebrow">${ui['YOUR COMMUNITY. YOUR VOICE.']}</span><h1>${ui['Welcome']}<br><em>${ui['back.']}</em></h1><p>${ui['One account. A clearer connection to the places that matter to you.']}</p><div class="story-card"><span class="pulse-symbol" aria-hidden="true">⌁</span><strong>${ui['Report. Track. Resolve.']}</strong><p>${ui['Citizen, officer and administrator accounts use the same sign-in page.']}</p></div></aside>
<section class="auth-card" aria-labelledby="login-title"><span class="eyebrow">${ui['LET’S GET YOU CONNECTED']}</span><h2 id="login-title">${ui['Sign in to CivicPulse']}</h2><p class="muted">${ui['Enter the email and password for your account.']}</p>
<c:if test="${registrationCompleted}"><div class="notice notice-success" role="status">${ui['Your citizen account is ready. Sign in to continue.']}</div></c:if>
<c:if test="${param.loggedOut != null}"><div class="notice notice-success" role="status">${ui['You have signed out.']}</div></c:if>
<c:if test="${param.expired != null}"><div class="notice" role="status">${ui['Sign in to access your workspace.']}</div></c:if>
<c:if test="${locationWelcome}"><section class="welcome-location"><h3>${ui['Find issues near you']}</h3><p>${ui['Allow location access to select the place when you take a photograph. You can also choose an address manually.']}</p><button type="button" class="button button-small button-outline" data-welcome-location>${ui['Enable location']}</button><p class="field-help" data-welcome-message role="status"></p></section></c:if>
<form:form method="post" action="${pageContext.request.contextPath}/login" modelAttribute="loginForm" cssClass="civic-form">
  <c:if test="${not empty requestScope['org.springframework.validation.BindingResult.loginForm'].allErrors}"><div class="notice notice-error" role="alert"><c:forEach items="${requestScope['org.springframework.validation.BindingResult.loginForm'].allErrors}" var="error"><p><c:out value="${ui[error.defaultMessage]}"/></p></c:forEach></div></c:if>
  <label for="email">${ui['Email address']}</label><form:input path="email" id="email" type="email" autocomplete="username" required="required" maxlength="190" placeholder="${ui['you@example.com']}"/>
  <label for="password">${ui['Password']}</label><div class="password-field"><input id="password" name="password" type="password" autocomplete="current-password" required maxlength="72"><button type="button" class="password-toggle" data-password-target="password" aria-label="${ui['Show password']}">${ui['Show']}</button></div>
  <button class="button button-primary button-full" type="submit">${ui['Sign in']} <span aria-hidden="true">→</span></button>
</form:form>
<p class="form-bottom">${ui['New here?']} <a href="<c:url value='/register'/>">${ui['Create a citizen account']}</a></p>

</section></main><%@ include file="../fragments/footer.jspf" %>
