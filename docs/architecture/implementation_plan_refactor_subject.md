# Subject Management Architecture Refactoring Plan

## Goal
To refine the Subject Management implementation across `authz-core` and `identity-service` to ensure strict separation of concerns, single-responsibility methods, and adherence to clean architecture principles.

## Proposed Changes

### 1. `authz-core`: `DefaultSubjectManagementService.java`
The `apply()` method currently contains all logic for finding/creating the entity, checking versions, mapping fields, and handling soft-deletes. This violates the Single Responsibility Principle.

**Refactoring:**
- Break down `apply()` into cohesive private methods:
  - `findOrCreateSubject(AuthzSubjectSyncEvent event)`
  - `isStaleEvent(AuthzSubjectJpaEntity entity, AuthzSubjectSyncEvent event)`
  - `updateSubjectFields(AuthzSubjectJpaEntity entity, AuthzSubjectSyncEvent event)`
  - `handleSoftDelete(AuthzSubjectJpaEntity entity, AuthzSubjectSyncEvent event)`

### 2. `identity-service`: `AuthzSubjectEventPublisher.java`
The event publisher currently constructs the `SubjectSyncMessage` inline for 8 different domain events. This mixes the concern of "listening and publishing" with "DTO mapping".

**Refactoring:**
- Create a private factory class or methods within the publisher to handle the mapping logic (e.g., `buildUserMessage(...)`, `buildRoleMessage(...)`).
- This centralizes the knowledge of how a domain event maps to the wire contract, making the listener methods purely about routing the event to the publisher.

## User Review Required
Please review this refactoring plan. It ensures the codebase remains maintainable and adheres to pure architectural standards by removing "fat" methods and isolating mapping logic. Do you approve proceeding with these changes?
