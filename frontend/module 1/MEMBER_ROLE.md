# Module 1: Citizen and community frontend developer

Member 2 · Actual name: [fill] · Repository path: `frontend/module 1/`

Public pages, authentication forms, shared navigation, citizen reporting, private report details, location picker and community map.

The owner supplies a runnable foundation and your drop-in payload. Review and integrate your assigned code under your own account.

Development ownership and application login roles are different: the app has Citizen, Officer and Administrator logins. Work with the other members on shared contracts. The foundation contains prepared core security/shared-UI files inside modules 3/1, so teammates do not replace the owner’s baseline on import. Module 3 maintains core backend code after integration; module 1 maintains shared frontend code after integration.

1. **Public/authentication pages and responsive shared layout** — `src/main/webapp/WEB-INF/views/{home.jsp,error.jsp,auth,account,fragments}`.
2. **Citizen forms, lists, details and timeline** — `src/main/webapp/WEB-INF/views/complaints; src/main/resources/static/assets/css`.
3. **Leaflet location picker, nearby suggestions and community map** — `src/main/webapp/WEB-INF/views/insights/community.jsp; src/main/resources/static/assets/{js/maps.js,vendor/leaflet}`.
4. **Browser review, accessibility fixes and module handover** — `SETUP_FOR_AGENT.md, MODULE_EXPLANATION.md, reports and actual reviewed fixes`.

Understand and demonstrate your module. Reports describe actual review, integration, fixes, tests, learning and commits, including coding-agent assistance. Future activity is not prefilled.

Release 1.1 responsibilities, contribution groups and weekly planning: see `RELEASE_1.1_CONTEXT.md`. For the implemented features, `backend/docs/RELEASE_1.1.md` is authoritative.
