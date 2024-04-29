package com.paymybuddy.paymybuddy.model.viewmodel;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Objects;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TransactionViewModel {
    private Integer       id;
    private UserViewModel issuer;
    private UserViewModel payee;

    private LocalDateTime date;

    private BigDecimal amount;

    private String description;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TransactionViewModel transactionViewModel = (TransactionViewModel) o;
        return (id.equals(transactionViewModel.id) &&
                issuer.equals(transactionViewModel.issuer) &&
                payee.equals(transactionViewModel.payee) &&
                date.isEqual(transactionViewModel.date) &&
                amount.equals(transactionViewModel.amount) &&
                description.equalsIgnoreCase(transactionViewModel.description));
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, issuer, payee, date, amount, description);
    }
}
