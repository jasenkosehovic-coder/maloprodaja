---
name: Create Angular Component
description: Checklist for generating a new standalone Angular component with signals, OnPush, and accessibility
type: project
---

When asked to create an Angular component:

1. Generate as standalone component
2. Use signal inputs (`input.required` / `input`)
3. Use `output()` for events
4. Use `OnPush` change detection for presentational components
5. Handle all 4 UI states: loading, error, empty, success
6. All form controls must have associated labels (`label[for]` or `aria-label`)
7. Use `takeUntilDestroyed()` for any subscriptions
8. Use `computed()` for derived state, not methods in templates
9. Use `@for` with track by stable ID, never `$index`
10. Keep templates under 80 lines — extract child components if larger
