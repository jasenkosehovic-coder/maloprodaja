# Global Rules

- Any task involving UI creation or modification must use the `frontend-design` plugin for styling and design decisions
- Infer the correct agent and skill from context — do not ask, just apply them
- When a task spans multiple layers, coordinate between the relevant agents

---

# Agent + Skill Routing

## Backend (java / Spring boot / API) LTS
- **Agent:** `java-expert`
- **Skills:**
  - When creating a new endpoint → follow the "Create API Endpoint" skill
  - When creating a new service → follow the "Create Service" skill
  - When fixing SonarQube issues → follow the "SonarQube Fix" skill
  - java language mastery — modern java features, pattern matching, records, nullable reference types, streams, functional programing 
  - hibernate Framework — query optimization, change tracking, migrations, temporal tables, bulk operations
  - Architecture — Clean Architecture, Mediator pattern, Domain-Driven Design, vertical slices
  - SOLID principles — applied pragmatically, not dogmatically
  - Testing — unit testing, integration testing, test doubles, testable design
  - Performance — profiling, memory allocation awareness, async best practices, caching strategies
  - Security — authentication, authorization, input validation, data protection

## Frontend (Angular) LTS
- **Agent:** `angular-expert`
- **Plugin:** Always use the `frontend-design` plugin when creating or modifying UI
- **Skills:**
  - When creating a component → follow the "Create Angular Component" skill
  - When adding a form → follow the "Create Reactive Form" skill
  - When fixing accessibility → follow the "Accessibility Fix" skill
  - Use Google material design
  - Signals — Angular's reactive primitive for synchronous state management
  - RxJS — mastery of operators, patterns, and proper subscription lifecycle management
  - Component architecture — smart/dumb components, encapsulation, reusability
  - Reactive forms — complex form handling, validation, dynamic forms
  - Performance — change detection strategy, lazy loading, bundle optimization
  - Accessibility — WCAG compliance, keyboard navigation, screen reader support
  - UI/UX best practices — responsive design, loading states, error states, empty states

## Architecture / Refactoring
- **Agent:** `software-architect`
- **Skills:**
  - Java / Spring boot LTS — Clean Architecture, Domain-Driven Design, SOLID principles, Hibernate Framework
  - Angular LTS — component architecture, state management, reactive patterns, standalone components, accessibility 
  - Cross-cutting — API contract design, shared models, authentication flows, error handling patterns, CI/CD

## Requirements / Analysis
- **Agent:** `business-analyst`
- **Skills:**
  - Requirements analysis — extracting, structuring, and validating functional and non-functional requirements from code
  - User flow mapping — tracing end-to-end user journeys across API and client applications
  - Gap analysis — identifying missing features, edge cases, validation rules, and business logic holes
  - Documentation — producing clear specs, user stories, and acceptance criteria

## Testing / QA
- **Agent:** `qa-engineer`
- **Skills:**
  - When testing a web page → use Playwright MCP
  - Create JUnit and integration test's 
  - End-to-end testing using Playwright via MCP (browser automation)
  - API testing — verifying endpoints directly via HTTP
  - Test strategy — planning systematic test coverage across web
  - Regression detection — identifying what broke and when
  - Accessibility testing — verifying WCAG compliance through browser interaction

---

# Business Rules

## Pravilo popusta (Discount Resolution)

Postoje tri izvora popusta. Kada se primjenjuje popust za artikal na kasi ili pri kalkulaciji cijene, vrijede sljedeća pravila:

### Izvori popusta

| Izvor | Entitet / polje | Scope |
|---|---|---|
| Popust centrale | `ArtikalKompanija.popustProcenat` | Važi za sve poslovnice kompanije |
| Popust poslovnice | `ArtikalPoslovnica.popustProcenat` | Važi samo za tu poslovnicu |
| Vremenski (kampanja) | `Popust` entitet sa `datumOd`/`datumDo` | Ako ima `idPoslovnice` → samo ta poslovnica; ako nema → cijela kompanija |

### Logika odabira

1. Prikupi sve popuste koji važe za dati artikal u datoj poslovnici u datom trenutku
2. Popusti se **ne sabiraju** — uzima se **najveći** od svih važećih popusta
3. Formula za cijenu nakon popusta: `cijena * (1 - maxPopust / 100)`

### Implementacija

- Ovu logiku implementirati u servisnom sloju (ne u kontroleru, ne u bazi)
- Metoda treba primiti: `idArtikla`, `idPoslovnice`, `datumPrimjene` → vratiti `BigDecimal popustProcenat`
- Koristiti pri: kreiranju stavki dokumenta (faktura, otpremnica), prikazu cijene na kasi

---

# Skills

## Create API Endpoint
When asked to create a new API endpoint:
1. Create a DTO record in /Models/DTOs/
2. Create a Hibernate Validator for the DTO
3. Create a service interface in /Services/Interfaces/
4. Create the service implementation in /Services/
5. Create a thin controller action that delegates to the service
6. Use Result<T> pattern for return types
7. Add CompletableFuture to all async methods
8. Create endpoint naming like /api/[entity]

## Create Service
When asked to create a new service:
1. Create interface in /Services/Interfaces/
2. Create implementation in /Services/
3. All methods return Result<T> for expected outcomes
4. All async methods accept CompletableFuture
5. Inject dependencies via constructor (primary constructor preferred)
6. Add structured logging with SLF4J + Logback / Log4j2
7. Error log in DB with AOP automatic catch exception with user / requestId / correlationId
8. GlobalExceptionHandler with @ControllerAdvice
9. No business logic in controllers
10. create pdf reports in jasper report studio LTS

## Create data layer
1. use pattern code first for creating db tables from entities
2. create repository for every entity
3. don't create entity for data that only have name and value, just create enum, if it is fix data (status, type of payment...)
4. for every table in db add extensions with sys_create_date, sys_create_by, sys_modifay_date , sys_modifay_by that is inserted/updated automaticly on every insert/update t otable
5. add to the application.yml every configuration for the application, do not hard code anything in code (path, parametars, etc.)
6. add every configuration for the user/company in db table of user/company, do not hard code anything in code (path, parametars, etc.)

## Create Angular Component
When asked to create an Angular component:
1. Use the `frontend-design` plugin for all UI/styling decisions
2. Generate as standalone component
3. Use signal inputs (input.required / input)
4. Use output() for events
5. Use OnPush change detection for presentational components
6. Handle all 4 UI states (loading, error, empty, success)
7. All form controls must have associated labels (label[for] or aria-label)
8. Use takeUntilDestroyed() for any subscriptions
9. Use computed() for derived state, not methods in templates
10. Use @for with track by stable ID, never $index
11. Keep templates under 80 lines — extract child components if larger

## Create Reactive Form
When asked to create a form:
1. Use the `frontend-design` plugin for form layout and styling
2. Use NonNullableFormBuilder with typed FormGroup
3. Define all validators (required, maxLength, pattern, custom...)
4. Every input has a <label for="id"> or aria-label
5. Show validation errors with role="alert" and aria-live="polite"
6. Disable submit button when form.invalid or isSubmitting
7. Show loading state on submit button during submission
8. Use exhaustMap for form submission to prevent duplicate submits
9. Handle API validation errors and map to form field errors

## SonarQube Fix
When fixing SonarQube issues:
1. Cognitive complexity > 15 → extract into smaller private methods
2. Missing labels → add label[for] or aria-label
3. Unused imports → remove them
4. Nested ternaries → refactor to if/else or switch
5. Duplicated code → extract into shared methods or services
6. Empty catch blocks → add logging or remove the try/catch

## Accessibility Fix
When fixing accessibility issues:
1. Every <input> gets a <label for="id"> or aria-label
2. Every <button> has visible text or aria-label
3. Dynamic content uses aria-live="polite"
4. Error messages use role="alert"
5. Custom widgets have proper role, tabindex, keyboard handlers
6. Modals trap focus and return focus to trigger on close
7. Heading hierarchy is sequential (h1 → h2 → h3)
8. Images have alt text (or aria-hidden="true" if decorative)