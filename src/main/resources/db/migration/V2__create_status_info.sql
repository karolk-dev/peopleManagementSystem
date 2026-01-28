CREATE TABLE status_info (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    status VARCHAR(255) NULL,
    started_at TIMESTAMP,
    finished_at TIMESTAMP,
    processed_rows BIGINT DEFAULT 0
);