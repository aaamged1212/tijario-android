# Offline Known Limitations (Android)

This document notes features and limits of the current mobile offline release.

## Known Limitations

1. **Plan Expiry Role Changes**: Plan switches (e.g. Pro to Free downgrade) require network connectivity to query usage limits from the Next.js server and adjust mobile offline leases.
2. **AI Assistance Offline**: Generative AI options are disabled when the device is offline, prompting the user with an "Offline" notification.
3. **Database Migration Test Requirements**: Connective instrumentation tests require a physical device or active emulator with `DatabaseMigrationTest` classes configured to verify raw database version transitions.
