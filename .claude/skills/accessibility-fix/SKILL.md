---
name: Accessibility Fix
description: WCAG remediation checklist — labels, ARIA, focus management, heading hierarchy
type: project
---

When fixing accessibility issues:

1. Every `<input>` gets a `<label for="id">` or `aria-label`
2. Every `<button>` has visible text or `aria-label`
3. Dynamic content uses `aria-live="polite"`
4. Error messages use `role="alert"`
5. Custom widgets have proper `role`, `tabindex`, keyboard handlers
6. Modals trap focus and return focus to trigger on close
7. Heading hierarchy is sequential (`h1` → `h2` → `h3`)
8. Images have `alt` text (or `aria-hidden="true"` if decorative)
