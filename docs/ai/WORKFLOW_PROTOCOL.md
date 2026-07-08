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

# Handoff rule
The next agent must be able to understand current work only by reading docs/ai/*.md and git history.
