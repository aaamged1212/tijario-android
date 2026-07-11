# Document Template Catalog

The canonical document template catalog is versioned and shared across the web and Android implementations.

Current rules:
- `tijario-classic` is the Free template.
- Starter, Pro, and Business can use every published template.
- Legacy aliases are normalized before persistence or rendering.
- The catalog version must stay aligned across both repositories.

When adding a new template:
1. Add the canonical ID, labels, aliases, and visual tokens to the catalog.
2. Update both renderers to support the new canonical ID.
3. Update Supabase allowed-template values if the template should be available in paid plans.
4. Add tests that fail if the version, IDs, or aliases diverge.

Rollback note:
- Do not remove historical aliases from rendering without a migration or a safe compatibility layer.
- Existing documents must keep rendering with their stored template IDs.
