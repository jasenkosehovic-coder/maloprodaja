---
name: Create API Endpoint
description: Step-by-step checklist for adding a new REST endpoint in the Spring Boot backend
type: project
---

When asked to create a new API endpoint:

> **Service contract** (also applies when invoking the Create Service skill):
> - Interface goes in `/Services/Interfaces/`, implementation in `/Services/`
> - All methods return `Result<T>` for expected outcomes
> - All async methods use `CompletableFuture`

1. Create a DTO record in `/Models/DTOs/`
2. Create a Hibernate Validator for the DTO
3. Create the service interface and implementation (see above contract)
4. Create a thin controller action that delegates to the service
5. Use `Result<T>` pattern for return types
6. Create endpoint naming like `/api/[entity]`
7. For PDF report endpoints, generate reports using Jasper Report Studio LTS
