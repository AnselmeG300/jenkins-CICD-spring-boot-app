package com.paymybuddy.paymybuddy.model.viewmodel;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.Objects;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UserViewModel {
    private Integer    id;
    private String     email;
    private String     firstname;
    private String     lastname;
    private BigDecimal balance;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UserViewModel userViewModel = (UserViewModel) o;
        if (userViewModel.balance == null) userViewModel.balance = new BigDecimal("0.00"); // add check of null value
        return (id.equals(userViewModel.id) &&
                email.equalsIgnoreCase(userViewModel.email) &&
                firstname.equalsIgnoreCase(userViewModel.firstname) &&
                lastname.equalsIgnoreCase(userViewModel.lastname) &&
                balance.equals(userViewModel.balance));
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, email, firstname, lastname, balance);
    }
}
