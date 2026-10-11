<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="pageTitle" value="Report insights"/>
<%@ include file="../fragments/head.jspf" %>
<%@ include file="../fragments/header.jspf" %>
<main id="main" class="container app-main">


<div class="page-heading"><div><span class="eyebrow">${ui['PROGRESS YOU CAN SEE']}</span><h1>${ui['Report insights']}</h1><p>${ui[scopeLabel]} ${ui['· Live counts from the report records.']}</p></div><a class="button button-primary" href="<c:url value='/insights/export.csv'/>">${ui['Download CSV ↓']}</a></div>
<%@ include file="../complaints/fragments/metrics.jspf" %>
<div class="two-columns"><section class="surface"><h2>${ui['Reports by category']}</h2><div class="category-counts"><c:forEach items="${categoryTotals}" var="row"><div><span><c:out value="${ui[row.key]}"/></span><strong><c:out value="${row.value}"/></strong></div></c:forEach></div></section><section class="surface"><span class="eyebrow">${ui['FROM REPORT TO CITIZEN CONFIRMATION']}</span><h2>${ui['Average time to verified closure']}</h2><p class="big-number"><c:out value="${averageHours}"/> <span>${ui['hours']}</span></p><p class="muted">${ui['Includes only reports the citizen has confirmed as fixed. A dash means no verified closures yet.']}</p></section></div>
<p class="field-help">${ui['Export includes up to 1,000 recent reports you can access.']}</p>

</main>
<%@ include file="../fragments/footer.jspf" %>
