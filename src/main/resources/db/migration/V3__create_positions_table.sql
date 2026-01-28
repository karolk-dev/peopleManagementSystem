CREATE TABLE positions (
    id BIGSERIAL PRIMARY KEY,
    employee_id BIGINT NOT NULL,
    title VARCHAR(100) NOT NULL,
    start_date DATE NOT NULL,
    end_date DATE NOT NULL,
    salary DOUBLE PRECISION NOT NULL,
    version BIGINT NOT NULL DEFAULT 1,
    CONSTRAINT fk_positions_employee
        FOREIGN KEY (employee_id)
        REFERENCES person (id)
        ON DELETE CASCADE
);

CREATE INDEX idx_positions_employee_id
    ON positions (employee_id);