---
name: SonarQube Fix
description: Remediation steps for common SonarQube issues — complexity, labels, duplication, catch blocks
type: project
---

When fixing SonarQube issues:

1. Cognitive complexity > 15 → extract into smaller private methods
2. Missing labels → add `label[for]` or `aria-label`
3. Unused imports → remove them
4. Nested ternaries → refactor to `if/else` or `switch`
5. Duplicated code → extract into shared methods or services
6. Empty catch blocks → add logging or remove the try/catch
