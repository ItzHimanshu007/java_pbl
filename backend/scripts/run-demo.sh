#!/bin/sh
# Builds the WAR and starts CivicPulse with the in-memory H2 demo profile.
# Demo records are recreated on every start. Do not use this profile in production.
set -e
cd "$(dirname "$0")/../.."
./mvnw -q -DskipTests package
exec java -jar backend/target/civicpulse.war --spring.profiles.active=demo "$@"
