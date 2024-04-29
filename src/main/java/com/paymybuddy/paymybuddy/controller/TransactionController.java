package com.paymybuddy.paymybuddy.controller;

import com.paymybuddy.paymybuddy.model.viewmodel.TransactionViewModel;
import com.paymybuddy.paymybuddy.service.TransactionService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Optional;

@Slf4j
@RestController
@RequestMapping("/transaction")
public class TransactionController {
    @Autowired
    TransactionService transactionService;
    /**
     * Lists all transactions.
     *
     * @return List of all transactions.
     */
    @GetMapping
    public List<TransactionViewModel> getTransactions() {
        return transactionService.getTransactions();
    }

    /**
     * Gets a transaction by its ID.
     *
     * @param id
     *         transaction to find
     *
     * @return Optional transaction
     */
    @GetMapping("/{id}")
    public Optional<TransactionViewModel> getConnectionById(@PathVariable Integer id) {
        return transactionService.getTransactionById(id);
    }
}
