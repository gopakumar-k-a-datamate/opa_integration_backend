
### Part 1: Admin UI Capabilities (How Admins Manage Rules)

If your consumer application is acting as the security administration portal, the library provides a set of ready-to-use APIs to power that UI. Here is the exact feature flow the UI can support:

**1. Namespace Discovery (Module Selection)**
* **Feature:** The UI can ask the backend "What application modules exist?"
* **How it works:** The library scans the database and returns a list of active namespaces (e.g., `clinical`, `pharmacy`, `billing`). The UI uses this to show a dropdown so the Admin can select which department's rules they want to edit.

**2. Condition Field Discovery (Building Rules)**
* **Feature:** When an admin wants to add a condition (e.g., "Only allow if Status is X"), the UI needs to know what fields are available for that specific action.
* **How it works:** The library exposes an API that returns all fields a developer tagged with `@PolicyField`. 
* **Dynamic Dropdowns:** If a field has a predefined list of values (e.g., `DRAFT`, `PAID`), the library sends those to the UI. If the field is dynamic (e.g., "Assigned Doctor"), the library tells the UI exactly which API endpoint to call to fetch the live list of doctors for the dropdown.

**3. Policy Grid Fetching (Viewing Existing Rules)**
* **Feature:** The UI asks for all existing rules for a specific Role (e.g., "Pharmacist") or a specific User.
* **How it works:** The library pulls the policies from the database, formats them into a flat "Grid" structure, and returns them so the UI can easily display checkboxes, allow/deny toggles, and condition blocks.

**4. Custom Rego Syntax Validation**
* **Feature:** If an advanced admin writes a custom code snippet (Rego language) in the UI, it must be validated before saving.
* **How it works:** When the UI sends the snippet, the library intercepts it, packages it into a temporary test, and sends it to the OPA engine. If there's a typo, OPA returns the exact line and character number of the error, which the library passes back to the UI to highlight the mistake in red.

**5. Atomic Policy Saving (Full Sync)**
* **Feature:** The Admin clicks "Save" after changing multiple checkboxes and conditions across the grid.
* **How it works:** The UI sends the entire final state of the grid. The library compares this against the database (diffing). It automatically figures out which rules to insert, which to update, and which to cleanly soft-delete. It then instantly recompiles the new rules into a `.tar.gz` bundle so sidecars can download the changes immediately.


### Part 2: Consumer Application Validation Flow (How Rules are Enforced)

When a regular user interacts with a consumer application (e.g., a Pharmacist trying to dispense medication), here is the exact step-by-step flow of how the library validates that action:

**1. The Request Arrives**
A Pharmacist clicks "Dispense" in the frontend. The HTTP request hits the `PharmacyController` and passes down to the `DispenseMedicationService`.

**2. The Invisible Interception (AOP)**
The developer has placed a `@ProtectedResource` annotation on the `dispense()` method. 
* **Feature:** The library intercepts the method call *before* any business logic runs. The developer didn't have to write any `if (user.isAllowed())` code.

**3. Identity Extraction**
* **Feature:** The library automatically asks the Spring Security context: "Who is making this request?" It extracts the User ID and their assigned Roles (e.g., `ROLE_PHARMACIST`).

**4. Resource Metadata Extraction**
* **Feature:** The library looks at the data object being passed into the method (e.g., the `Prescription` object). It uses the `@PolicyResource` annotation on that class to know the action being attempted is `pharmacy:prescription:dispense`.
* It then scans the object for fields annotated with `@PolicyField` (like `status = "APPROVED"` or `prescribedBy = "Dr. Smith"`) and builds a map of the data.

**5. OPA Sidecar Evaluation**
* **Feature:** The library packages the User Identity, the Action, and the Data Attributes into a JSON payload and sends it over a lightning-fast local network hop to the OPA sidecar container.

**6. The Decision**
OPA evaluates the payload against the latest rules it downloaded from the Admin UI. It instantly returns either `ALLOW` or `DENY`.

**7. Execution or Rejection**
* If `ALLOW`: The library gets out of the way, and the `dispense()` method executes normally.
* If `DENY`: The library immediately blocks the execution and throws an `AuthzDeniedException`, which translates into an HTTP `403 Forbidden` response to the frontend.