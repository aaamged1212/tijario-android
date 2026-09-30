# Pre-flight
Must run before every task:
- read AGENTS.md
- read docs/ai/*.md
- git status --short
- git branch --show-current
- git log --oneline -8
- git diff --stat
- git diff --name-only
- inspect untracked files
- report understanding before edits

## Mandatory documentation contract

Documentation is part of the implementation, not an optional close-out step. Every source,
configuration, migration, test, release, or external-contract change must update the relevant
AI documentation in the same task. The agent must never wait for the user to request this.

At minimum, after any change:

- update `CURRENT_BASELINE.md` when the current branch, version, validation, architecture, or
  release state changes;
- update `AGENT_HANDOFF.md` with the exact change, files, validation, and remaining risks;
- append one dated entry to `AI_CHANGELOG.md`;
- update `PROJECT_STATE.md` when project state or a blocker changes;
- update `PENDING_RELEASE.md` when a release, migration, deployment, QA, or external action is
  opened, completed, or blocked;
- update `ACTIVE_AGENT.md` to identify the latest task state and next action.

The task is not complete if code is changed but the required documentation is stale, contradictory,
or missing. Documentation entries must state what was verified and must distinguish local code
health from production, device, deployment, migration, and Play Console evidence.

# During work
- work on one focused task
- do not overwrite uncommitted files unexpectedly
- do not broaden scope
- no feature creep
- no production actions

# Post-flight
Must run after every task:
- update AGENT_HANDOFF.md
- append AI_CHANGELOG.md
- update PROJECT_STATE.md if needed
- update PENDING_RELEASE.md if needed
- run tests/builds if code changed
- run git diff --check
- report changed files and validations
- confirm no push/deploy/migration/upload unless approved

## Enforcement

- Before commit, inspect `git diff`, `git status`, and all changed documentation.
- Never claim a test, deployment, migration, device check, or release action that was not observed.
- Never delete historical entries to hide stale work; supersede them with a dated current-state entry.
- If documentation cannot be updated, stop before commit and report the blocker.

# Handoff rule
The next agent must be able to understand current work only by reading docs/ai/*.md and git history.
