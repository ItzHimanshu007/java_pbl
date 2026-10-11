# Dependency and build notes

Java 17; Spring Boot 3.5.16 (Security/MVC/JDBC/validation/OAuth2 JWT support); embedded Tomcat/Jasper; JSP/JSTL 3; MySQL connector; Flyway; H2 local demo/test database; Spring/JUnit security/integration testing; Maven wrapper 3.9.11. The root build packages one executable WAR.

Bootstrap CSS and Leaflet 1.9.4 JS/CSS are vendored in frontend module 1 with their license headers/license file. Map tiles come from attributed OpenStreetMap standard tiles; follow the [tile policy](https://operations.osmfoundation.org/policies/tiles/) for hosted use. No bulk/offline tile download or map-service API key is included.

Build Helper Maven Plugin 3.6.2 adds the second backend Java/test source roots, following its [official usage guide](https://www.mojohaus.org/build-helper-maven-plugin/usage.html). Maven WAR Plugin merges the second frontend JSP root through [external web resources](https://maven.apache.org/plugins/maven-war-plugin/examples/adding-filtering-webresources.html). This is build configuration, not an additional runtime framework or service.

Leaflet source/reference: https://leafletjs.com/reference-1.9.4.html. Java uses `jakarta.*` servlet/validation APIs consistently. Dependencies are pinned directly or by the Boot parent. First build needs network access; subsequent builds can use an already-populated Maven cache.
