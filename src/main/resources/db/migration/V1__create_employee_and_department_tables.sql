CREATE TABLE departments
(
    id         UUID         NOT NULL,
    name       VARCHAR(100) NOT NULL,
    created_at TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    deleted_at TIMESTAMP WITHOUT TIME ZONE,
    CONSTRAINT pk_departments PRIMARY KEY (id)
);

CREATE TABLE employees
(
    id            UUID         NOT NULL,
    first_name    VARCHAR(100) NOT NULL,
    last_name     VARCHAR(100) NOT NULL,
    email         VARCHAR(150) NOT NULL,
    status        VARCHAR(20)  NOT NULL,
    role          VARCHAR(20)  NOT NULL,
    created_at    TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    updated_at    TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    deleted_at    TIMESTAMP WITHOUT TIME ZONE,
    department_id UUID,
    CONSTRAINT pk_employees PRIMARY KEY (id)
);

ALTER TABLE departments
    ADD CONSTRAINT uk_department_name UNIQUE (name);

ALTER TABLE employees
    ADD CONSTRAINT uk_employee_email UNIQUE (email);

CREATE INDEX idx_department_name ON departments (name);

CREATE INDEX idx_employee_email ON employees (email);

ALTER TABLE employees
    ADD CONSTRAINT FK_EMPLOYEE_DEPARTMENT FOREIGN KEY (department_id) REFERENCES departments (id);