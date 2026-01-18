CREATE TYPE user_role AS ENUM ('OWNER', 'READER', 'EDITOR');


CREATE TABLE task_ontologies (
    id UUID PRIMARY KEY,
    owner_id UUID NOT NULL,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT now(),
    updated_at TIMESTAMP NOT NULL DEFAULT now()
);

CREATE INDEX idx_task_owner ON task_ontologies(owner_id);


CREATE TABLE rule_ontologies (
    id UUID PRIMARY KEY,
    owner_id UUID NOT NULL,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT now(),
    updated_at TIMESTAMP NOT NULL DEFAULT now()
);

CREATE INDEX idx_rule_owner ON rule_ontologies(owner_id);


CREATE TABLE users_tasks (
    user_id UUID NOT NULL,
    task_id UUID NOT NULL,
    role user_role NOT NULL,

    PRIMARY KEY (user_id, task_id),
    FOREIGN KEY (task_id) REFERENCES task_ontologies(id) ON DELETE CASCADE
);

CREATE INDEX idx_users_tasks_task ON users_tasks(task_id);


CREATE TABLE users_rules (
    user_id UUID NOT NULL,
    rule_id UUID NOT NULL,
    role user_role NOT NULL,

    PRIMARY KEY (user_id, rule_id),
    FOREIGN KEY (rule_id) REFERENCES rule_ontologies(id) ON DELETE CASCADE
);

CREATE INDEX idx_users_rules_rule ON users_rules(rule_id);


CREATE TABLE tasks_rules_order (
    task_id UUID NOT NULL,
    rule_id UUID NOT NULL,
    order_number INT NOT NULL CHECK (order_number >= 0),

    PRIMARY KEY (task_id, rule_id),
    FOREIGN KEY (task_id) REFERENCES task_ontologies(id) ON DELETE CASCADE,
    FOREIGN KEY (rule_id) REFERENCES rule_ontologies(id) ON DELETE CASCADE,
    UNIQUE (task_id, order_number)
);