<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="pageTitle" value="Your reports"/>
<%@ include file="../fragments/head.jspf" %>
<%@ include file="../fragments/header.jspf" %>
<main id="main" class="container app-main">


<div class="page-heading"><div><span class="eyebrow">${ui['REPORT. TRACK. RESOLVE.']}</span><h1>${ui['Your reports']}</h1><p>${ui['Track your reports, review repair evidence and confirm completed work.']}</p></div><a class="button button-primary" href="<c:url value='/citizen/reports/new'/>">${ui['Report an issue ↗']}</a></div>
<c:if test="${param.deleted != null}"><div class="notice notice-success" role="status">${ui['Report deleted. You can restore it from Deleted reports.']}</div></c:if>
<c:if test="${param.restored != null}"><div class="notice notice-success" role="status">${ui['Report restored.']}</div></c:if>
<%@ include file="fragments/metrics.jspf" %>
<div class="section-bar"><a class="text-link" href="<c:url value='/citizen/reports'/>">${ui['Active reports']}</a><a class="text-link" href="<c:url value='/citizen/reports?trash=true'/>">${ui['Deleted reports']}</a></div>
<c:choose><c:when test="${trash}"><section class="surface"><h2>${ui['Deleted reports']}</h2><c:if test="${empty reports}"><p>${ui['No deleted reports.']}</p></c:if><c:forEach items="${reports}" var="r"><div class="report-row"><div><strong><c:out value="${r.title}"/></strong><p><c:out value="${r.reference}"/></p></div><form method="post" action="<c:url value='/citizen/reports/${r.id}/restore'/>"><input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}"><button class="button button-small button-outline" type="submit">${ui['Restore report']}</button></form></div></c:forEach></section><%@ include file="fragments/pagination.jspf" %></c:when><c:otherwise>
<%@ include file="fragments/filters.jspf" %><section class="surface"><%@ include file="fragments/report-list.jspf" %></section><%@ include file="fragments/pagination.jspf" %></c:otherwise></c:choose>

</main>
<%@ include file="../fragments/footer.jspf" %>
