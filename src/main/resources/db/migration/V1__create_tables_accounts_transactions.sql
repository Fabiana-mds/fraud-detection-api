CREATE TABLE accounts (
                          id BIGSERIAL PRIMARY KEY,
                          account_number VARCHAR(20) NOT NULL UNIQUE,
                          customer_name VARCHAR(100) NOT NULL,
                          base_risk_score DECIMAL(19, 2) NOT NULL
);

CREATE TABLE transactions (
                              id BIGSERIAL PRIMARY KEY,
                              account_id BIGINT NOT NULL,
                              amount DECIMAL(19, 2) NOT NULL,
                              timestamp TIMESTAMP NOT NULL,
                              type VARCHAR(20) NOT NULL,
                              status VARCHAR(20) NOT NULL,
                              risk_score DECIMAL(19, 2) NOT NULL,
                              CONSTRAINT fk_account FOREIGN KEY (account_id) REFERENCES accounts(id)
);

-- Criando índices para performance (isso estava nos requisitos!)
CREATE INDEX idx_transaction_account_id ON transactions(account_id);
CREATE INDEX idx_transaction_timestamp ON transactions(timestamp);