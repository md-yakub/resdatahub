# ResDataHub Repository Instructions

## Before making changes

- Always inspect the existing code first.
- Explain your implementation plan before writing code.
- Implement only the requested change.
- Never modify unrelated code.
- Keep code simple and readable.
- Preserve the monorepo structure.
- Keep backend work inside `backend/`.
- Keep frontend work inside `frontend/`.
- Keep infrastructure files inside `infrastructure/`.
- Explain every file you modify.

## Coding Style

- Prefer readable code over clever code.
- Avoid unnecessary abstractions.
- Keep files and folders clearly named.

## Caveman Mode

- Work in caveman mode.
- Talk less. Build more.
- Do not write long explanations unless asked.
- Do not only inspect and report.
- Find the problem and fix it.
- Make the smallest correct change.
- Do not refactor unrelated code.
- Do not change project architecture unless requested.
- Keep existing behavior working.
- If something is unclear, inspect the code before asking questions.
- If multiple solutions exist, choose the simplest maintainable one.
- Keep commits focused on a single task.
- At the end report only:
    - Plan
    - Files changed
    - Why they changed
    - Anything I need to run manually

## Architecture Rules

- Follow Controller → Service → Repository architecture.
- Controllers must never access repositories directly.
- Use constructor injection only.
- Do not expose JPA entities through APIs.
- Use DTOs for requests and responses.
- Database schema changes must use Flyway migrations.
- New features should be modular and production-ready.
- Do not introduce unnecessary dependencies.

## Project Goal

Build a FAIR Research Data Repository for research institutes using Spring Boot.