# ADR 006: Dynamic Dropdown Pagination Standardization

## 1. Context
The `authz-core` library allows policy condition fields to define an `optionsEndpoint` (via the `@PolicyField` annotation). This endpoint is used by the Admin UI to fetch a dynamic list of allowed values for condition builder dropdowns (e.g., fetching a live list of Doctors).

We need a standardized data contract for these endpoints so the Admin UI can implement infinite scrolling and searching without needing to know the specifics of any consumer application's domain.

> [!IMPORTANT]
> **Precedence Rule:** If a condition field accidentally defines *both* `optionsEndpoint` and hardcoded `allowedValues`, the system (and Admin UI) **MUST prioritize `optionsEndpoint`**. The dynamic link takes precedence, and the hardcoded values should be ignored.

## 2. Decision
We will use **Offset/Limit Pagination** as the standard.

This approach maps easily to Spring Data `Pageable` in consumer applications while satisfying the UI's need for scrolling, searching, and total count display. 

Crucially, **we will not expose Spring's `Page<T>` directly as the public contract.** Instead, we define a standard JSON structure `AllowedValuePageResponse` to ensure the AuthZ contract remains completely agnostic of Spring Data internals.

### Request Standard
```http
GET {optionsEndpoint}?page=0&size=20&search=john
```

| Parameter | Required | Default | Description               | Constraints |
| --------- | -------- | ------: | ------------------------- | ----------- |
| `page`    | No       |     `0` | Zero-based page number    | `>= 0`      |
| `size`    | No       |    `20` | Number of records         | `1 - 100`   |
| `search`  | No       |   empty | Server-side search/filter |             |

### Response Standard (`AllowedValuePageResponse`)
**Note:** The `authz-core` library provides `org.datamate.authz.rest.dto.AllowedValuePageResponse` and `AllowedValueResponse` out of the box, so consumers do not need to implement these DTOs themselves.

```json
{
  "content": [
    {
      "id": "DOC-001",
      "displayName": "John Doe"
    }
  ],
  "page": 0,
  "size": 20,
  "totalElements": 45,
  "last": false
}
```

* `id`: The underlying value saved in the policy.
* `displayName`: The human-readable text shown in the UI.

## 3. Consequences
* **Pros:**
  * Very easy for consumer microservices to implement.
  * Fully supports Admin UI infinite scrolling and server-side searching.
  * Standardizes validation (max size = 100) to prevent abuse.
  * Completely decouples the Admin UI from consumer domain entities (e.g., UI doesn't know what a "Doctor" is, only `id` and `displayName`).
* **Cons:**
  * Deep pagination on massive datasets (e.g., millions of records) may suffer performance penalties due to SQL `OFFSET`. (If this becomes a bottleneck, cursor pagination can be introduced in a future ADR).

## 4. Consumer Implementation Example
Consumer applications are responsible for implementing this contract. The `authz-core` library provides the `AllowedValuePageResponse` and `AllowedValueResponse` DTOs, so consumers only need to map their Spring Data `Page<T>` entities to the standard contract.

### 4.1 Repository
Spring Data JPA `Pageable` handles the pagination natively:

```java
public interface DoctorRepository extends JpaRepository<Doctor, String> {

    Page<Doctor> findByActiveTrue(Pageable pageable);

    Page<Doctor> findByActiveTrueAndNameContainingIgnoreCase(String search, Pageable pageable);
}
```

### 4.2 Application Service
The service isolates Spring Data's `Page<T>` from the public API, mapping the domain entities into `AllowedValueResponse`.

```java
@Service
@RequiredArgsConstructor
public class GetDoctorsService {

    private final DoctorRepository doctorRepository;

    @Transactional(readOnly = true)
    public AllowedValuePageResponse execute(int page, int size, String search) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("name").ascending());
        Page<Doctor> doctors;

        if (search == null || search.isBlank()) {
            doctors = doctorRepository.findByActiveTrue(pageable);
        } else {
            doctors = doctorRepository.findByActiveTrueAndNameContainingIgnoreCase(search, pageable);
        }

        List<AllowedValueResponse> content = doctors.getContent().stream()
                .map(doc -> new AllowedValueResponse(doc.getId(), doc.getName()))
                .collect(Collectors.toList());

        return new AllowedValuePageResponse(
                content,
                doctors.getNumber(),
                doctors.getSize(),
                doctors.getTotalElements(),
                doctors.isLast()
        );
    }
}
```

### 4.3 REST Controller
The controller remains extremely thin and handles contract validation.

```java
@RestController
@RequestMapping("/api/v1/pharmacy/doctors")
@RequiredArgsConstructor
public class DoctorController {

    private final GetDoctorsService getDoctorsService;

    @GetMapping
    public AllowedValuePageResponse getDoctors(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String search
    ) {
        if (page < 0) throw new IllegalArgumentException("page must be >= 0");
        if (size < 1 || size > 100) throw new IllegalArgumentException("size must be between 1 and 100");

        return getDoctorsService.execute(page, size, search);
    }
}
```

### 4.4 Database Registration (Flyway)
It is critical to remember that the Admin UI fetches the condition field configurations from the **database**, not directly from the Java `@PolicyField` annotations. 

When introducing a new `options_endpoint` (or migrating an existing field), the consumer microservice **must** provide a Flyway migration script to insert or update the `authz_condition_field` table. 

**Scenario A: Adding a Brand New Field**
If you are introducing a completely new policy condition field, use `INSERT`:
```sql
-- VX__add_new_dynamic_field.sql
INSERT INTO authz_condition_field (permission_id, field_name, field_type, display_name, options_endpoint)
VALUES (2, 'doctorLevel', 'STRING', 'Doctor Level', '/api/v1/pharmacy/doctors');
```

**Scenario B: Migrating an Existing Legacy Field**
If the field already exists in the database and was previously using hardcoded `allowed_values`, use `UPDATE`:
```sql
-- VX__migrate_legacy_field_to_dynamic.sql
UPDATE authz_condition_field 
SET options_endpoint = '/api/v1/pharmacy/doctors',
    allowed_values = NULL
WHERE permission_id = 2 AND field_name = 'doctorLevel';
```

If you forget this step, the Admin UI will continue to serve whatever stale data was previously stored in the database!
