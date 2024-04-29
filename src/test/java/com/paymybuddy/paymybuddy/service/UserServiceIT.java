package com.paymybuddy.paymybuddy.service;

import com.paymybuddy.paymybuddy.model.BankAccount;
import com.paymybuddy.paymybuddy.model.User;
import com.paymybuddy.paymybuddy.repository.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.fail;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@SpringBootTest
public class UserServiceIT {
    @Autowired
    UserService userService;

    @Autowired
    UserRepository     userRepository;
    @Autowired
    BankAccountService bankAccountService;

    private User        user;
    private Integer     id;
    private BankAccount bankAccount;
    private Integer     accountId;


    @BeforeEach
    void init() {
        user = new User();
        user.setEmail("testIT@mail.com");
        user.setFirstName("Firstname");
        user.setLastName("Lastname");
        user.setPassword("tawfzeklf");

        user = userService.createUser(user);
        id = user.getId();

        bankAccount = bankAccountService.createBankAccount(user,
                                                           "UserServiceIT Test Bank",
                                                           "FR7630001007941234567890185",
                                                           new BigDecimal("250.00"));
        accountId = bankAccount.getId();
    }

    @AfterEach
    void reset() {
        bankAccountService.deleteBankAccount(bankAccount);
        userService.deleteUser(user);
    }

    @Test
    @DisplayName("Deposit should add money to user's balance")
    void deposit() {
        String amount = "50";

        userService.deposit(user, amount);

        Optional<User> updatedUser = userService.getUserById(id);
        if (updatedUser.isEmpty()) {
            fail("User was not found.");
        } else {
            assertThat(updatedUser.get().getBalance()).isEqualTo(new BigDecimal("50.00"));
        }
        Optional<BankAccount> updatedAccount = bankAccountService.getBankAccountById(accountId);
        if (updatedAccount.isEmpty()) {
            fail("Bank account was not well created");
        } else {
            assertThat(updatedAccount.get().getBalance()).isEqualTo(new BigDecimal("200.00"));
        }
    }

    @Test
    @DisplayName("Withdraw should subtract money to user's balance")
    void withdraw() {
        String amount = "50";

        userService.withdraw(user, amount);

        Optional<User> updatedUser = userService.getUserById(id);
        if (updatedUser.isEmpty()) {
            fail("User was not found.");
        } else {
            assertThat(updatedUser.get().getBalance()).isEqualTo(new BigDecimal("-50.00"));
        }
        Optional<BankAccount> updatedAccount = bankAccountService.getBankAccountById(accountId);
        if (updatedAccount.isEmpty()) {
            fail("Bank account was not well created");
        } else {
            assertThat(updatedAccount.get().getBalance()).isEqualTo(new BigDecimal("300.00"));
        }
    }
}
