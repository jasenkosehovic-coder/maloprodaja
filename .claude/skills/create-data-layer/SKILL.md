---
name: Create data layer
description: Conventions for creating the persistence layer — entities, repositories, audit columns
type: project
---

When creating the data layer:

1. Use code-first pattern — create DB tables from entities
2. Create a repository for every entity
3. Don't create an entity for data that only has name and value; use an enum for fixed data (status, payment type, etc.)
4. For every table, add audit columns: `sys_create_date`, `sys_create_by`, `sys_modify_date`, `sys_modify_by` — populated automatically on every insert/update
5. Add every application configuration to `application.yml` — do not hard-code paths, parameters, etc. in code
6. Store every user/company-level configuration in the DB table for that user/company — do not hard-code in code
