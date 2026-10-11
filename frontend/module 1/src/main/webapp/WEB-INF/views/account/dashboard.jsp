<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="pageTitle" value="Your workspace"/>
<%@ include file="../fragments/head.jspf" %><%@ include file="../fragments/header.jspf" %>
<main id="main">
 <section class="page-head"><div class="container"><span class="eyebrow">${ui['Your workspace']}</span><h1>${ui['Hello,']} <c:out value="${displayName}"/></h1><p>${ui['You’re signed in to your']} ${ui[roleLabel]} ${ui['account.']}</p></div></section>
 <div class="container page-body"><div class="grid-2"><section class="card card-pad"><h2>${ui['Connected to your community.']}</h2><p class="muted mt-1">${ui['CivicPulse brings reporting, accountable progress and repair verification into one shared journey.']}</p><a class="link mt-2" href="${ctx}/account">${ui['Manage your account']}<cp:icon name="arrow-right"/></a></section>
 <section class="card card-pad"><h2>${ui['Your account']}</h2><dl class="facts mt-2"><div><dt>${ui['Name']}</dt><dd><c:out value="${displayName}"/></dd></div><div><dt>${ui['Role']}</dt><dd>${ui[roleLabel]}</dd></div><div class="wide"><dt>${ui['Email']}</dt><dd><c:out value="${accountEmail}"/></dd></div></dl></section></div></div>
</main><%@ include file="../fragments/footer.jspf" %>
