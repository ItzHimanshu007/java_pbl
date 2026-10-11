<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="pageTitle" value="Create your account"/>
<%@ include file="../fragments/head.jspf" %><%@ include file="../fragments/header.jspf" %>
<main id="main" class="auth-shell container"><aside class="auth-story"><span class="eyebrow">${ui['START WITH YOUR VOICE']}</span><h1>${ui['A better place']}<br>${ui['starts with']}<br><em>${ui['you.']}</em></h1><p>${ui['Join CivicPulse and help bring attention to the shared spaces around you.']}</p><div class="story-card"><span class="small-icon" aria-hidden="true">✓</span><strong>${ui['A citizen account, made simple.']}</strong><p>${ui['Your email stays private. Your account gives you a personal workspace.']}</p></div></aside>
<section class="auth-card" aria-labelledby="register-title"><span class="eyebrow">${ui['MAKE YOURSELF AT HOME']}</span><h2 id="register-title">${ui['Create a citizen account']}</h2><p class="muted">${ui['A few details to get started.']}</p>
<form:form method="post" action="${pageContext.request.contextPath}/register" modelAttribute="registrationForm" cssClass="civic-form">
  <c:if test="${not empty requestScope['org.springframework.validation.BindingResult.registrationForm'].allErrors}"><div class="notice notice-error" role="alert"><c:forEach items="${requestScope['org.springframework.validation.BindingResult.registrationForm'].allErrors}" var="error"><p><c:out value="${ui[error.defaultMessage]}"/></p></c:forEach></div></c:if>
  <label for="displayName">${ui['Your name']}</label><form:input path="displayName" id="displayName" autocomplete="name" required="required" maxlength="80" placeholder="${ui['Your full name']}"/>
  <label for="email">${ui['Email address']}</label><form:input path="email" id="email" type="email" autocomplete="username" required="required" maxlength="190" placeholder="${ui['you@example.com']}"/>
  <label for="password">${ui['Password']}</label><div class="password-field"><input id="password" name="password" type="password" autocomplete="new-password" required minlength="10" maxlength="72" aria-describedby="password-help"><button class="password-toggle" type="button" data-password-target="password" aria-label="${ui['Show password']}">${ui['Show']}</button></div><span class="field-help" id="password-help">${ui['Use at least 10 characters.']}</span>
  <label for="confirmPassword">${ui['Confirm password']}</label><input id="confirmPassword" name="confirmPassword" type="password" autocomplete="new-password" required minlength="10" maxlength="72">
  <button class="button button-primary button-full" type="submit">${ui['Create account']} <span aria-hidden="true">→</span></button>
</form:form><p class="form-bottom">${ui['Already have an account?']} <a href="<c:url value='/login'/>">${ui['Sign in']}</a></p>
</section></main><%@ include file="../fragments/footer.jspf" %>
