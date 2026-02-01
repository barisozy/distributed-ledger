package com.distributed.ledger.domain.model;

import com.distributed.ledger.domain.exception.DomainException;
import java.util.Currency;

public class Account {

    private final AccountId id;
    private final String name;
    private final String accountNumber;
    private Money balance;
    private AccountStatus status;
    private Long version;

    private Account(AccountId id, String name, String accountNumber, Money balance, AccountStatus status, Long version) {
        this.id = id;
        this.name = name;
        this.accountNumber = accountNumber;
        this.balance = balance;
        this.status = status;
        this.version = version;
    }

    public static Account create(String accountNumber, String name, Money initialBalance) {
        return new Account(
                AccountId.generate(),
                name,
                accountNumber,
                initialBalance,
                AccountStatus.ACTIVE,
                0L
        );
    }

    public static Account with(AccountId id, String name, String accountNumber, Money balance, AccountStatus status, Long version) {
        return new Account(id, name, accountNumber, balance, status, version);
    }


    public void deposit(Money amount) {
        validateActiveStatus();
        this.balance = this.balance.add(amount);
    }

    public void withdraw(Money amount) {
        validateActiveStatus();

        if (this.balance.subtract(amount).isNegative()) {
            throw new DomainException(
                    String.format("Insufficient funds. Balance: %s, Attempted: %s", this.balance, amount)
            );
        }

        this.balance = this.balance.subtract(amount);
    }

    private void validateActiveStatus() {
        if (!this.status.canTransact()) {
            throw new DomainException("Account is not active. Status: " + this.status);
        }
    }

    public AccountId getId() { return id; }
    public String getName() { return name; }
    public String getAccountNumber() { return accountNumber; }
    public Money getBalance() { return balance; }
    public AccountStatus getStatus() { return status; }
    public Long getVersion() { return version; }
    public Currency getCurrency() { return balance.getCurrency(); }
}