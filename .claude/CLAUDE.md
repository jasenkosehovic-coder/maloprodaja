# Global Rules

- Any task involving UI creation or modification must use the `frontend-design` plugin for styling and design decisions
- Infer the correct agent and skill from context — do not ask, just apply them
- When a task spans multiple layers, coordinate between the relevant agents

# Behavioral Rules

- **User is a UI/UX designer.** Keep responses minimal — do work silently, report results in 1-2 sentences. No narration, no technical play-by-play, no file lists, no option tables. Only speak up for user decisions, failures, or product-level impact. When unsure: ask immediately instead of guessing. When stuck or spending disproportionate time: stop and consult the user — don't spiral.
- **MCP servers (SQLite, GitHub, Figma...) may be off** — if a task needs them and they're disconnected, remind the user to turn them on before proceeding.
- Say "I don't know" when that's the truth. Never fabricate answers.
- **Use skills proactively — do not wait for user instruction. Ask user to use agents if needed**
- Read the full call graph before editing. Understand callers, dependencies, state layers (code + DB + Zustand persist + process memory).
- Before deleting anything: find why it exists first. Weird-looking code often encodes hard-won lessons.
- Trace blast radius before acting: what calls this? What reads/writes what it touches?
- Fix root causes, not symptoms. One change per reason — don't "clean up while I'm in here."
- Before irreversible actions: full stop, confirm, double-check.
- **Golden standard rule — NEVER take shortcuts. Every solution must follow current industry best practices and production-grade architecture. Never duplicate logic across similar components — unify into composable patterns. Never propose "quick" or "simple" approaches when a proper abstraction exists. When facing "duplicate a little vs design properly", always design properly. Code should feel like 2030, not 2020. This applies to algorithms (smooth degradation over hard caps), component architecture (composition over variant flags), data flow (proper hooks and shared state), and every other design decision. If the first idea is the naive one, keep thinking.**
- **Evidence before solutions. For any design or architecture question: read the affected code, list all consumers and affected paths FIRST, then propose. Never propose a solution without having read the code it touches. The failure pattern to avoid: reading the problem, immediately forming a solution, and outputting it. That produces "technically correct but shallow" answers that waste rounds of revision. Gather evidence → understand full picture → only then design.**

## Working Process Rules

- **Never commit or deploy without explicit instruction.**
- **Use `dispatching-parallel-agents` only when absolutely needed.** Always use `executing-plans` for multi-step implementation.
- **For QA and fix work, use `/fix`** — 9-phase systematic debug workflow. Never skip phases.

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

# Projekt — Arhitektura i Trenutno Stanje

See `.claude/ARCHITECTURE.md` for full tech stack, package structure, and domain rules.
