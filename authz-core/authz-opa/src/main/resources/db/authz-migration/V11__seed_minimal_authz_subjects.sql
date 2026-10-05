-- ============================================================
-- authz-core V11: Seed Minimal Baseline Subjects
-- Seeds baseline system roles and default administrator into authz_subject
-- ============================================================

INSERT INTO authz_subject (
    subject_type,
    subject_id,
    subject_name,
    display_name,
    email,
    description,
    status,
    version,
    created_at,
    updated_at,
    synced_at,
    deleted_at
)
VALUES 
    (
        'ROLE',
        '00000000-0000-0000-0000-000000000000',
        'POLICY_ADMIN',
        'POLICY_ADMIN',
        NULL,
        'Root Policy Administrator with OPA authoring rights',
        'ACTIVE',
        0,
        CURRENT_TIMESTAMP,
        CURRENT_TIMESTAMP,
        CURRENT_TIMESTAMP,
        NULL
    ),
    (
        'ROLE',
        '22222222-2222-2222-2222-222222222222',
        'USER',
        'USER',
        NULL,
        'Standard User Role',
        'ACTIVE',
        0,
        CURRENT_TIMESTAMP,
        CURRENT_TIMESTAMP,
        CURRENT_TIMESTAMP,
        NULL
    ),
    (
        'USER',
        '10000000-0000-0000-0000-000000000000',
        'admin@123.com',
        'System Admin',
        'admin@123.com',
        NULL,
        'ACTIVE',
        0,
        CURRENT_TIMESTAMP,
        CURRENT_TIMESTAMP,
        CURRENT_TIMESTAMP,
        NULL
    ),
    (
        'USER',
        '20000000-0000-0000-0000-000000000000',
        'user@123.com',
        'Standard User',
        'user@123.com',
        NULL,
        'ACTIVE',
        0,
        CURRENT_TIMESTAMP,
        CURRENT_TIMESTAMP,
        CURRENT_TIMESTAMP,
        NULL
    )
ON CONFLICT (subject_type, subject_id) DO UPDATE
SET subject_name = EXCLUDED.subject_name,
    display_name = EXCLUDED.display_name,
    email        = EXCLUDED.email,
    description  = EXCLUDED.description,
    status       = 'ACTIVE',
    deleted_at   = NULL,
    updated_at   = CURRENT_TIMESTAMP,
    synced_at    = CURRENT_TIMESTAMP;
