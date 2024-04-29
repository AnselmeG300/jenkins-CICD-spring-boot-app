package com.paymybuddy.paymybuddy.model.viewmodel;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TransferViewModel {
    String payeeEmail;
    double amount;
    double amountWithFee;
    String description;
}
