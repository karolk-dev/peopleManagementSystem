CREATE TABLE person (

    id BIGSERIAL PRIMARY KEY,


    first_name VARCHAR(50) NOT NULL,
    last_name VARCHAR(50) NOT NULL,
    pesel VARCHAR(11) NOT NULL UNIQUE,
    height INT NOT NULL,
    weight DOUBLE PRECISION NOT NULL,
    email VARCHAR(100) NOT NULL,
    person_type VARCHAR(31) NOT NULL,
    version BIGINT NOT NULL DEFAULT 1,

    employment_start_date DATE,
    current_position VARCHAR(100),
    salary DOUBLE PRECISION,

    university_name VARCHAR(200),
    study_year INT,
    field_of_study VARCHAR(100),
    scholarship_amount DOUBLE PRECISION,

    pension_amount DOUBLE PRECISION,
    years_worked INT

);
    CREATE INDEX idx_person_email ON person(email);
    CREATE INDEX idx_person_type ON person(person_type);