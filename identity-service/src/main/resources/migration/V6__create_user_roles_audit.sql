CREATE TABLE user_roles_aud (
    user_id UUID NOT NULL,
    role_id UUID NOT NULL,
    rev INT NOT NULL,
    revtype SMALLINT,
    PRIMARY KEY (user_id, role_id, rev)
);
