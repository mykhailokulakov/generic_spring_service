-- V2__example.sql
--
-- Schema for ExampleEntity. Columns from the four-class persistence chain
-- (id, created_at, updated_at, version, deleted_at) are emitted here, per
-- the policy established in V1__base.sql.

CREATE TABLE example (
    id           uuid         NOT NULL,
    created_at   timestamptz  NOT NULL,
    updated_at   timestamptz  NOT NULL,
    version      bigint       NOT NULL,
    deleted_at   timestamptz,
    name         varchar(200) NOT NULL,
    description  text,
    quantity     integer,
    price        numeric(19, 2),
    occurred_at  timestamptz,
    status       varchar(32)  NOT NULL,
    CONSTRAINT pk_example PRIMARY KEY (id)
);

CREATE INDEX ix_example_name        ON example (name);
CREATE INDEX ix_example_status      ON example (status);
CREATE INDEX ix_example_occurred_at ON example (occurred_at);

CREATE TABLE example_tag (
    example_id uuid        NOT NULL,
    tag        varchar(64) NOT NULL,
    CONSTRAINT pk_example_tag PRIMARY KEY (example_id, tag),
    CONSTRAINT fk_example_tag_example FOREIGN KEY (example_id)
        REFERENCES example (id) ON DELETE CASCADE
);

CREATE INDEX ix_example_tag_value ON example_tag (tag);
