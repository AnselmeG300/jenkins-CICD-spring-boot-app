package com.paymybuddy.paymybuddy.repository;

import com.paymybuddy.paymybuddy.model.BankAccount;
import com.paymybuddy.paymybuddy.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DataJpaTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class BankAccountRepositoryIT {
    @Autowired
    BankAccountRepository bankAccountRepository;
    @Autowired
    UserRepository userRepository;
    private BankAccount bankAccount;
    private User        user;
    
    @BeforeEach
    public void setup() {
        // create a user, owner of test bank account
        user = new User(1, "abc@email.com", "1234ABC", "Jean", "Dupont", new BigDecimal(150), new ArrayList<>(),
                        new ArrayList<>(), new ArrayList<>(), new ArrayList<>());
        user = userRepository.save(user);

        // create test bank account
        bankAccount = new BankAccount();
        bankAccount.setUser(user);
        bankAccount.setBankName("bank name");
        bankAccount.setIban("FR7630001007941234567890185");
        bankAccount.setBalance(new BigDecimal("1650.95"));
    }

    @Test
    @DisplayName("Saving a new bank account should add an ID")
    void createBankAccount_savesNewBankAccount() {
        BankAccount savedBankAccount = bankAccountRepository.save(bankAccount);
        assertThat(savedBankAccount.getId()).isNotNull();
    }


    @Test
    @DisplayName("findByUser should return a bank account when bank account exists")
    public void findByUser_shouldReturn_aBankAccount() {
        //GIVEN an existing bank account
        BankAccount bankAccountToFind = bankAccountRepository.save(bankAccount);
        // WHEN trying to findByUser
        Optional<BankAccount> bankAccount = bankAccountRepository.findByUser(user);
        //THEN found bank account should not be empty
        assertTrue(bankAccount.isPresent());
    }

    @Test
    @DisplayName("findById should return a bank account when bank account exists")
    void findById_shouldReturn_aBankAccount() {
        //GIVEN an existing bank account
        BankAccount bankAccountToFind = bankAccountRepository.save(bankAccount);
        // WHEN trying to findByEmail
        Optional<BankAccount> bankAccount = bankAccountRepository.findById(bankAccountToFind.getId());
        //THEn a bank account should be found
        assertTrue(bankAccount.isPresent());
    }


    @Test
    @DisplayName("deleteById should delete a bank account by ID")
    void deleteById_shouldDeleteBankAccount() {
        //GIVEN an existing bank account
        BankAccount bankAccountToDelete = bankAccountRepository.save(bankAccount);
        assertTrue(bankAccountRepository.existsById(bankAccountToDelete.getId()));
        // WHEN deleting bank account
        bankAccountRepository.deleteById(bankAccountToDelete.getId());
        //THEn the bank account should not be found
        assertTrue(bankAccountRepository.findById(bankAccountToDelete.getId()).isEmpty());
    }
}