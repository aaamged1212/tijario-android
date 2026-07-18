# Local-First Release Checklist

## Code gates

- Android JVM tests, AndroidTest assembly, lint, debug, and release assembly pass separately.
- Web tests, TypeScript, lint, and production build pass.
- `git diff --check` passes in both repositories.
- Secret scan finds no production credentials.
- Both tracked trees are clean; `.agents/` remains local-only and uncommitted.

## Database/API gates

- Approved database backup exists.
- Three Local-First migrations are applied in documented order.
- SQL post-deploy checklist passes.
- Backup key and entitlement signing variables are configured in the approved secret store.
- Compatible APIs are deployed and no-store/auth boundaries verified.
- One internal account passes device, lease, event idempotency, and no-refund checks.

## Android/external gates

- Android public entitlement key map contains active/rotation keys.
- Google Drive OAuth uses least privilege and production package/signing identity.
- Physical-phone checklist passes in Arabic/English and light/dark mode.
- Offline create/edit/delete/restore/export and restart behavior pass.
- Real backup/restore verifies all structured data, assets, and PDFs.
- Drive disconnect does not affect local access.

## Rollout

1. Keep legacy cloud tables intact.
2. Enable only controlled internal accounts first.
3. Monitor device registration, lease issuance, event rejection, and backup errors.
4. Expand gradually with a documented rollback owner.
5. Do not advertise production readiness until all external/device gates pass.

Current status: code-complete for local static/build validation only; production rollout is blocked.
