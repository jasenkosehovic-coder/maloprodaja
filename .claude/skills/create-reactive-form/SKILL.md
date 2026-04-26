---
name: Create Reactive Form
description: Checklist for building typed reactive forms with validation, accessibility, and safe submission
type: project
---

When asked to create a form:

1. Use `NonNullableFormBuilder` with typed `FormGroup`
2. Define all validators: `required`, `maxLength`, `pattern`, custom...
3. Every input has a `<label for="id">` or `aria-label`
4. Show validation errors with `role="alert"` and `aria-live="polite"`
5. Disable submit button when `form.invalid` or `isSubmitting`
6. Show loading state on submit button during submission
7. Use `exhaustMap` for form submission to prevent duplicate submits
8. Handle API validation errors and map to form field errors
