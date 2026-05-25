CREATE TABLE test_parent (
    id           uuid         NOT NULL,
    created_at   timestamptz  NOT NULL,
    updated_at   timestamptz  NOT NULL,
    version      bigint       NOT NULL,
    deleted_at   timestamptz,
    label        varchar(200) NOT NULL,
    CONSTRAINT pk_test_parent PRIMARY KEY (id)
);

CREATE TABLE test_child (
    id           uuid         NOT NULL,
    created_at   timestamptz  NOT NULL,
    updated_at   timestamptz  NOT NULL,
    version      bigint       NOT NULL,
    deleted_at   timestamptz,
    value        varchar(200) NOT NULL,
    parent_id    uuid         NOT NULL,
    CONSTRAINT pk_test_child PRIMARY KEY (id),
    CONSTRAINT fk_test_child_parent FOREIGN KEY (parent_id)
        REFERENCES test_parent (id)
);

CREATE TABLE test_profile (
    id           uuid         NOT NULL,
    created_at   timestamptz  NOT NULL,
    updated_at   timestamptz  NOT NULL,
    version      bigint       NOT NULL,
    deleted_at   timestamptz,
    bio          varchar(500) NOT NULL,
    CONSTRAINT pk_test_profile PRIMARY KEY (id)
);

CREATE TABLE test_owner (
    id           uuid         NOT NULL,
    created_at   timestamptz  NOT NULL,
    updated_at   timestamptz  NOT NULL,
    version      bigint       NOT NULL,
    deleted_at   timestamptz,
    handle       varchar(200) NOT NULL,
    profile_id   uuid,
    CONSTRAINT pk_test_owner PRIMARY KEY (id),
    CONSTRAINT fk_test_owner_profile FOREIGN KEY (profile_id)
        REFERENCES test_profile (id)
);

CREATE TABLE test_left (
    id           uuid         NOT NULL,
    created_at   timestamptz  NOT NULL,
    updated_at   timestamptz  NOT NULL,
    version      bigint       NOT NULL,
    deleted_at   timestamptz,
    code         varchar(100) NOT NULL,
    CONSTRAINT pk_test_left PRIMARY KEY (id)
);

CREATE TABLE test_right (
    id           uuid         NOT NULL,
    created_at   timestamptz  NOT NULL,
    updated_at   timestamptz  NOT NULL,
    version      bigint       NOT NULL,
    deleted_at   timestamptz,
    name         varchar(200) NOT NULL,
    CONSTRAINT pk_test_right PRIMARY KEY (id)
);

CREATE TABLE test_left_right (
    left_id      uuid NOT NULL,
    right_id     uuid NOT NULL,
    CONSTRAINT pk_test_left_right PRIMARY KEY (left_id, right_id),
    CONSTRAINT fk_test_lr_left FOREIGN KEY (left_id)
        REFERENCES test_left (id),
    CONSTRAINT fk_test_lr_right FOREIGN KEY (right_id)
        REFERENCES test_right (id)
);
