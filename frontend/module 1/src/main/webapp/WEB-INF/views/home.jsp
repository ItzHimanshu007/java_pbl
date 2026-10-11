<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="pageTitle" value="A better neighbourhood starts with you"/>
<%@ include file="fragments/head.jspf" %><%@ include file="fragments/header.jspf" %>
<main id="main">
  <section class="hero container">
    <div class="hero-copy"><span class="eyebrow"><span class="status-dot"></span> ${ui['FOR THE PLACES WE CALL HOME']}</span>
      <h1>${ui['A small report.']}<br>${ui['A']} <em>${ui['better']}</em><br>${ui['neighbourhood.']}</h1>
      <p class="hero-description">${ui['A clearer path from a local problem to a visible repair. Bring your community’s roads, lights and shared spaces into focus.']}</p>
      <div class="hero-actions"><a class="button button-primary" href="<c:url value='/register'/>">${ui['Create your citizen account']} <span aria-hidden="true">↗</span></a><a class="text-link" href="#how-it-works">${ui['See how it works']} <span aria-hidden="true">↓</span></a></div>
      <div class="hero-note"><span class="small-icon" aria-hidden="true">✓</span> ${ui['One place to report, follow progress and verify a repair.']}</div>
    </div>
    <div class="city-panel" aria-label="${ui['Illustration of a neighbourhood with civic issue location pins']}" role="img">
      <div class="city-panel-top"><span class="eyebrow">${ui['OUR SHARED SPACES']}</span><span class="pill pill-light">${ui['A community perspective']}</span></div>
      <svg class="city-art" viewBox="0 0 500 380" aria-hidden="true">
        <defs><pattern id="mapgrid" width="28" height="28" patternUnits="userSpaceOnUse"><path d="M 28 0 L 0 0 0 28" fill="none" stroke="#bcd0bd" stroke-width=".7"/></pattern></defs>
        <rect width="500" height="380" fill="url(#mapgrid)"/>
        <path d="M-20 285L200 174L520 322M74-20L270 390" fill="none" stroke="#fffdf2" stroke-width="48"/>
        <path d="M-20 285L200 174L520 322M74-20L270 390" fill="none" stroke="#cebd98" stroke-width="1.3" stroke-dasharray="7 7"/>
        <g stroke="#164d3c" stroke-width="2" stroke-linejoin="round">
          <path d="M235 70L327 25L407 67L315 113Z" fill="#f7e8b6"/><path d="M235 70V154L315 198V113Z" fill="#e6ca88"/><path d="M315 113V198L407 151V67Z" fill="#d6b374"/>
          <path d="M60 132L121 101L180 131L118 163Z" fill="#fffaf0"/><path d="M60 132V197L118 229V163Z" fill="#9fbc9c"/><path d="M118 163V229L180 197V131Z" fill="#6d9d7f"/>
          <path d="M328 240L384 212L442 243L385 273Z" fill="#fffaf0"/><path d="M328 240V290L385 321V273Z" fill="#d4b596"/><path d="M385 273V321L442 290V243Z" fill="#b98d70"/>
          <path d="M338 95V117M359 84V106M380 73V95M338 137V159M359 126V148M380 115V137" stroke="#f9efd4" stroke-width="9"/>
          <path d="M135 161V184M156 149V172" stroke="#e3f0d7" stroke-width="8"/>
        </g>
        <g fill="#4c8060"><ellipse cx="213" cy="309" rx="35" ry="14" fill="#adc4a0"/><rect x="210" y="265" width="6" height="43" fill="#6c5d42"/><circle cx="213" cy="255" r="25"/><circle cx="196" cy="266" r="17"/><circle cx="230" cy="265" r="18"/>
          <rect x="439" y="152" width="5" height="36" fill="#6c5d42"/><circle cx="441" cy="144" r="23"/>
          <rect x="39" y="300" width="5" height="30" fill="#6c5d42"/><circle cx="41" cy="294" r="19"/></g>
        <g fill="#164d3c" stroke="#fffdf2" stroke-width="3"><path d="M209 94C182 94 181 127 209 149C237 127 236 94 209 94Z"/><path d="M103 237C76 237 75 270 103 292C131 270 130 237 103 237Z"/><path d="M410 167C383 167 382 200 410 222C438 200 437 167 410 167Z"/></g>
        <g fill="#f0d37c"><circle cx="209" cy="114" r="7"/><circle cx="103" cy="257" r="7"/><circle cx="410" cy="187" r="7"/></g>
      </svg>
      <div class="city-caption"><span class="pulse-symbol" aria-hidden="true">⌁</span><div><strong>${ui['Every place has a story.']}</strong><span>${ui['Make the next chapter a little better.']}</span></div></div>
    </div>
  </section>
  <section id="how-it-works" class="process-section"><div class="container"><div class="section-heading"><span class="eyebrow">${ui['A CLEARER PATH FORWARD']}</span><h2>${ui['From noticed to resolved.']}</h2><p>${ui['The complaint journey, with a citizen’s voice at every step.']}</p></div>
    <div class="process-grid"><article class="process-card"><span class="step-number">01</span><h3>${ui['Report']}</h3><p>${ui['Describe the problem, add a photo and pinpoint the location.']}</p></article><article class="process-card"><span class="step-number">02</span><h3>${ui['Track']}</h3><p>${ui['Follow the assignment and work updates in one clear timeline.']}</p></article><article class="process-card"><span class="step-number">03</span><h3>${ui['Resolve']}</h3><p>${ui['Review repair evidence and confirm that the problem is fixed.']}</p></article></div>
  </div></section>
</main><%@ include file="fragments/footer.jspf" %>
