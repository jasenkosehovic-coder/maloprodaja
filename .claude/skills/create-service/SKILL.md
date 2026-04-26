---
name: Create Service
description: Step-by-step checklist for creating a new Spring Boot service with interface and implementation
type: project
---

When asked to create a new service:

1. Create interface in `/Services/Interfaces/`
2. Create implementation in `/Services/`
3. All methods return `Result<T>` for expected outcomes
4. All async methods use `CompletableFuture`
5. Inject dependencies via constructor (primary constructor preferred)
6. Add structured logging with SLF4J + Logback / Log4j2
7. Error log in DB with AOP automatic catch exception with user / requestId / correlationId
8. GlobalExceptionHandler with `@ControllerAdvice`
