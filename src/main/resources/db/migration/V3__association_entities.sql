CREATE TABLE parent (
    id           uuid         NOT NULL,
    created_at   timestamptz  NOT NULL,
    updated_at   timestamptz  NOT NULL,
    version      bigint       NOT NULL,
    deleted_at   timestamptz,
    label        varchar(200) NOT NULL,
    CONSTRAINT pk_parent PRIMARY KEY (id)
);

CREATE TABLE child (
    id           uuid         NOT NULL,
    created_at   timestamptz  NOT NULL,
    updated_at   timestamptz  NOT NULL,
    version      bigint       NOT NULL,
    deleted_at   timestamptz,
    value        varchar(200) NOT NULL,
    parent_id    uuid         NOT NULL,
    CONSTRAINT pk_child PRIMARY KEY (id),
    CONSTRAINT fk_child_parent FOREIGN KEY (parent_id)
        REFERENCES parent (id)
);

CREATE TABLE owner (
    id           uuid         NOT NULL,
    created_at   timestamptz  NOT NULL,
    updated_at   timestamptz  NOT NULL,
    version      bigint       NOT NULL,
    deleted_at   timestamptz,
    handle       varchar(200) NOT NULL,
    example_id   uuid UNIQUE,
    CONSTRAINT pk_owner PRIMARY KEY (id),
    CONSTRAINT fk_owner_example FOREIGN KEY (example_id)
        REFERENCES example (id)
);

CREATE TABLE left_item (
    id           uuid         NOT NULL,
    created_at   timestamptz  NOT NULL,
    updated_at   timestamptz  NOT NULL,
    version      bigint       NOT NULL,
    deleted_at   timestamptz,
    code         varchar(100) NOT NULL,
    CONSTRAINT pk_left_item PRIMARY KEY (id)
);

CREATE TABLE right_item (
    id           uuid         NOT NULL,
    created_at   timestamptz  NOT NULL,
    updated_at   timestamptz  NOT NULL,
    version      bigint       NOT NULL,
    deleted_at   timestamptz,
    name         varchar(200) NOT NULL,
    CONSTRAINT pk_right_item PRIMARY KEY (id)
);

CREATE TABLE left_right_item (
    left_id      uuid NOT NULL,
    right_id     uuid NOT NULL,
    CONSTRAINT pk_left_right_item PRIMARY KEY (left_id, right_id),
    CONSTRAINT fk_lr_left FOREIGN KEY (left_id)
        REFERENCES left_item (id),
    CONSTRAINT fk_lr_right FOREIGN KEY (right_id)
        REFERENCES right_item (id)
);
