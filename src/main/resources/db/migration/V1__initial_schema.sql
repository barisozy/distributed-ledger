-- V1__initial_schema.sql

-- 1. Extensions
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- 2. Tables
-- Accounts table
CREATE TABLE accounts (
                          id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
                          account_number VARCHAR(255) NOT NULL,
                          account_number_hash VARCHAR(64) UNIQUE NOT NULL,
                          account_name VARCHAR(255) NOT NULL,
                          balance DECIMAL(19, 4) NOT NULL DEFAULT 0.0000,
                          currency VARCHAR(3) NOT NULL,
                          status VARCHAR(20) NOT NULL,
                          created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
                          updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
                          version BIGINT NOT NULL DEFAULT 0
);

-- Transactions table
CREATE TABLE transactions (
                              id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
                              transaction_reference VARCHAR(100) UNIQUE NOT NULL,
                              from_account_id UUID REFERENCES accounts(id),
                              to_account_id UUID REFERENCES accounts(id),
                              amount DECIMAL(19, 4) NOT NULL,
                              currency VARCHAR(3) NOT NULL,
                              transaction_type VARCHAR(50) NOT NULL,
                              status VARCHAR(20) NOT NULL,
                              description TEXT,
                              created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
                              completed_at TIMESTAMP WITH TIME ZONE,
                              metadata JSONB,
                              CONSTRAINT positive_amount CHECK (amount > 0)
);

-- Ledger entries table (double-entry bookkeeping)
CREATE TABLE ledger_entries (
                                id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
                                transaction_id UUID NOT NULL REFERENCES transactions(id),
                                account_id UUID NOT NULL REFERENCES accounts(id),
                                entry_type VARCHAR(10) NOT NULL CHECK (entry_type IN ('DEBIT', 'CREDIT')),
                                amount DECIMAL(19, 4) NOT NULL,
                                currency VARCHAR(3) NOT NULL,
                                balance_after DECIMAL(19, 4) NOT NULL,
                                created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                CONSTRAINT positive_ledger_amount CHECK (amount > 0)
);

-- Audit log table
CREATE TABLE audit_log (
                           id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
                           entity_type VARCHAR(100) NOT NULL,
                           entity_id UUID NOT NULL,
                           action VARCHAR(50) NOT NULL,
                           user_id VARCHAR(100),
                           changes JSONB,
                           ip_address varchar(45),
                           user_agent TEXT,
                           created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- 3. Indexes
CREATE INDEX idx_accounts_number_hash ON accounts(account_number_hash);
CREATE INDEX idx_accounts_status ON accounts(status);

CREATE INDEX idx_transactions_reference ON transactions(transaction_reference);
CREATE INDEX idx_transactions_from_account ON transactions(from_account_id);
CREATE INDEX idx_transactions_to_account ON transactions(to_account_id);
CREATE INDEX idx_transactions_status ON transactions(status);
CREATE INDEX idx_transactions_created_at ON transactions(created_at);

CREATE INDEX idx_ledger_entries_transaction ON ledger_entries(transaction_id);
CREATE INDEX idx_ledger_entries_account ON ledger_entries(account_id);
CREATE INDEX idx_ledger_entries_created_at ON ledger_entries(created_at);

CREATE INDEX idx_audit_log_entity ON audit_log(entity_type, entity_id);
CREATE INDEX idx_audit_log_created_at ON audit_log(created_at);

-- 4. Functions & Triggers
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
RETURN NEW;
END;
$$ language 'plpgsql';

CREATE TRIGGER update_accounts_updated_at BEFORE UPDATE ON accounts
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

-- 5. Comments
COMMENT ON TABLE accounts IS 'Stores account information';
COMMENT ON COLUMN accounts.account_number IS 'Encrypted account number (PII)';
COMMENT ON COLUMN accounts.account_number_hash IS 'Blind index for account number uniqueness and lookup';
COMMENT ON TABLE transactions IS 'Stores transaction records';
COMMENT ON TABLE ledger_entries IS 'Double-entry bookkeeping ledger entries';
COMMENT ON TABLE audit_log IS 'Audit trail for all operations';