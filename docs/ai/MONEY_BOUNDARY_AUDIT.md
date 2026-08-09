# Money Boundary Audit

## 2026-08-09

### Canonical local calculation path
- User-entered money is normalized and parsed as `BigDecimal` by `Validation` and `DocumentCalculator`.
- Document subtotal, discount, tax base, tax amount, shipping, total, and remaining amount are calculated and rounded with `BigDecimal` using `HALF_UP` at two decimal places.
- Room document and item entities already persist financial values as `BigDecimal`.

### Compatibility boundaries retained
- Existing remote API contracts and legacy product/document UI models expose `Double` fields.
- Conversion to `Double` remains limited to the explicit API/model boundary; callers must not calculate money with those values.
- Remote `Double` values are converted back through `BigDecimal.valueOf(...)` when cached locally.

### Regression coverage
- Decimal inputs `0.1` and `0.2` retain their decimal representation.
- Large totals, discount, tax, paid amount, Arabic digits, Arabic decimal separators, and comma decimals are covered by JVM tests.

### Deferred migration
- A wire/schema migration from numeric `Double` contracts requires an explicit, compatible Web/API rollout and is intentionally out of scope for this Android-only hardening step.
