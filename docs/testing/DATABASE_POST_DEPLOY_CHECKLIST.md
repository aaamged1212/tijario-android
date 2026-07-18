# Local-First Database Post-Deploy Checklist

Run only after explicit migration approval. Queries are read-only.

```sql
select code, document_limit_scope, offline_entitlement_days,
       offline_credit_batch_size, max_primary_devices, backup_frequency
from public.plans
where code in ('free', 'starter', 'pro', 'business')
order by rank;

select data_mode, count(*)
from public.user_plan
group by data_mode;

select user_id, count(*) filter (where is_primary and status = 'active') as active_primary_devices
from public.device_registrations
group by user_id
having count(*) filter (where is_primary and status = 'active') > 1;

select user_id, operation_id, count(*)
from public.document_creation_events
group by user_id, operation_id
having count(*) > 1;

select user_id, document_id, count(*)
from public.document_creation_events
group by user_id, document_id
having count(*) > 1;

select user_id, lifetime_documents_created, billing_cycle_documents_created,
       current_period_key, version
from public.account_usage_totals
order by updated_at desc
limit 100;

select status, count(*)
from public.offline_quota_leases_v2
group by status;

select count(*) as malformed_payload_hashes
from public.document_creation_events
where payload_hash !~ '^[a-f0-9]{64}$';
```

## Safe functional checks

- Register the same installation twice: one active primary record, idempotent success.
- Try another installation: `DEVICE_LIMIT_REACHED`.
- Request a lease twice before consumption: same usable lease, no extra grant.
- Exhaust a lease and request again: a new bounded batch is available if plan quota remains.
- Reconcile the same event twice: one usage increment.
- Reuse operation ID with changed payload: `OPERATION_PAYLOAD_MISMATCH`.
- Delete/restore/export a document: account usage remains unchanged.

Do not update counters manually to make a test pass.
