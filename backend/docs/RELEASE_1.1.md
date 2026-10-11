# Release 1.1 implementation context

## Account, theme and language

V500 adds an optional one-to-one account profile. ProfileDao locks the user row and saves the display name and profile together. Server validation checks past birth dates, lengths, language/theme values and paired coordinates. The authenticated account determines the profile owner; submitted user IDs/roles cannot select another account. Preferences are stored in the database; small cookies carry language/theme to signed-out pages. JSP and JavaScript share English/Hindi dictionaries. Citizen-entered titles, addresses, names and notes are retained in their original language.

The expanded profile follows the account/home/preferences pattern of [SeeClickFix](https://www.civicplus.help/seeclickfix/docs/account-preferences), with the requested optional birth date/contact/address fields. No unnecessary notification or external identity service was added.

## Location and photos

`/locations/search` and `/locations/reverse` authenticate requests and call a fixed configured Photon endpoint. Queries and coordinates are validated. Responses are bounded, cached for five minutes and throttled globally to one new request per second; failures return a generic error. See [Photon documentation](https://github.com/komoot/photon/blob/master/docs/api-v1.md) and its public-server usage notes. Public service access has no availability guarantee.

The address widget invalidates a previous address-selected point when typing changes it, while preserving an explicitly placed map pin for a manual landmark. Selecting a suggestion, clicking/dragging a pin or granting location access updates the map and internal fields. Native/in-page camera capture previews the selected image and starts a fresh GPS request; submission waits for that request. Denial clears the old location and lets the citizen choose a new point. Gallery upload previews the file but does not infer a photo's location from EXIF. The server accepts PNG/JPEG up to 5 MB, decodes/re-encodes evidence and enforces private access to stored images.

## Report integrity and deletion

V600 adds `deleted_at` and `issue_root_id`; old migration files are unchanged. GET/POST edit endpoints return 409 for owners. Deletion records a timeline event and removes the report from public/staff queries, evidence reads, counts and exports. Restoration is owner-only and restores the previous workflow state and evidence. POST operations require CSRF. This is recoverable deletion, not permanent data erasure.

## Feed and map priority

Citizens see other citizens' grouped issues in Feed, optionally within 1–50 km of a selected/home area (default 5 km). Personal status counts belong in My Reports. A report can explicitly link to an existing active issue only within 150 m and in the same category. Proximity within 250 m suggests possible matches; it does not prove identity. Distinct non-deleted, non-rejected reporters count once per root issue. The thresholds are high >=5, medium 2–4, low 1; closed issues are low. Feed ranking puts active issues first, then reporter count, support count and newest ID. Map colour and pin count reflect that ranking. The displayed issue circle is a visual 75 m area marker, not a surveyed boundary or an accuracy claim.

Public progress shows status/action/time only; descriptions, staff notes, private evidence, contact details and actor identities are not shared. Percentages represent stages (0, 25, 50, 85, 100), not measured physical completion or an ETA. The community query reads up to 1,000 recent reports, grouped views show up to 200; this is a small-project scope, not city-scale analytics.

## Ownership and handover

All five module folders include release responsibilities and report-planning context. Cross-workflow CitizenExperienceIntegrationTest belongs to backend module 4 because it calls ResolutionService. The base includes security/accounts/locations and shared map/photo assets needed by Account. Feature reporting/insights and staff modules arrive through the four checked add-ons. The 1.1 kit is a matched fresh snapshot; importing it into a modified/1.0 foundation requires an explicit reviewed merge rather than bypassing checksums.

Use real contributor identities and actual commit hashes. Twelve-week forms are planning/report templates, not prewritten claims of work. No commits or pushes have been fabricated.

## Demo and licensing

The demo alone seeds four owner reports plus nine fictional nearby citizen reports. Explicit links illustrate high/medium/low priorities. Real photo files and their CC BY-SA attribution are in backend module 3's `src/main/resources/demo/photos/ATTRIBUTION.md`; the two sample locations are different and the detail view says so. Normal MySQL startup does not seed fictional citizens/reports. Removing the demo banner does not make the demo profile suitable for deployment.
