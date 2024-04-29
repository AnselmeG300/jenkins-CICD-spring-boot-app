package com.paymybuddy.paymybuddy.repository;

import com.paymybuddy.paymybuddy.model.Transaction;
import com.paymybuddy.paymybuddy.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@DataJpaTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class TransactionRepositoryIT {
    @Autowired
    TransactionRepository transactionRepository;

    @Autowired
    UserRepository userRepository;

    private Transaction transaction;
    private User        payee;
    private User        issuer;

    // configure LocalDateTime.now() to 18th July 2022, 10:00:00
    private final static LocalDateTime LOCAL_DATE_NOW = LocalDateTime.of(2022, 7, 18, 10, 0, 0);
    @MockBean
    Clock clock;

    @BeforeEach
    public void setup() {
        // Configure a fixed clock to have fixed LocalDate.now()
        Clock fixedClock = Clock.fixed(LOCAL_DATE_NOW.atZone(ZoneId.systemDefault()).toInstant(),
                                       ZoneId.systemDefault());
        when(clock.instant()).thenReturn(fixedClock.instant());
        when(clock.getZone()).thenReturn(fixedClock.getZone());

        // create a payee and an issuer for test transaction
        payee = new User(1, "abc@email.com", "1234ABC", "Jean", "Dupont", new BigDecimal(150), new ArrayList<>(),
                         new ArrayList<>(), new ArrayList<>(), new ArrayList<>());
        issuer = new User(2, "def@email.com", "6571fsqdSDV", "Jane", "Doe", new BigDecimal(150), new ArrayList<>(),
                          new ArrayList<>(), new ArrayList<>(), new ArrayList<>());
        payee = userRepository.save(payee);
        issuer = userRepository.save(issuer);

        // create test transaction
        transaction = new Transaction();
        transaction.setDate(LocalDateTime.now(clock));
        transaction.setIssuer(issuer);
        transaction.setPayee(payee);
        transaction.setAmount(new BigDecimal(10));
    }

    @Test
    @DisplayName("Saving a new transaction should add an ID")
    void createTransaction_savesNewTransaction() {
        Transaction savedTransaction = transactionRepository.save(transaction);
        assertThat(savedTransaction.getId()).isNotNull();
    }


    @Test
    @DisplayName("findByIssuerOrPayee should return a transaction when transaction exists")
    public void findByIssuerOrPayee_shouldReturn_aTransaction() {
        //GIVEN an existing transaction
        Transaction transactionToFind = transactionRepository.save(transaction);
        // WHEN trying to findByIssuerOrPayee
        List<Transaction> transaction = transactionRepository.findByIssuerOrPayee(issuer, payee);
        //THEn a transaction should be found
        assertFalse(transaction.isEmpty());
    }

    @Test
    @DisplayName("findByIssuer should return a transaction when transaction exists")
    void findByIssuer_shouldReturn_aTransaction() {
        //GIVEN an existing transaction
        Transaction transactionToFind = transactionRepository.save(transaction);
        // WHEN trying to findByEmail
        List<Transaction> transaction = transactionRepository.findByIssuer(issuer);
        //THEn a transaction should be found
        assertFalse(transaction.isEmpty());
    }

    @Test
    @DisplayName("findByPayee should return a transaction when transaction exists")
    void findByPayee_shouldReturn_aTransaction() {
        //GIVEN an existing transaction
        Transaction transactionToFind = transactionRepository.save(transaction);
        // WHEN trying to findByFirstNameAndLastName
        List<Transaction> transaction = transactionRepository.findByPayee(payee);
        //THEn a transaction should be found
        assertFalse(transaction.isEmpty());
    }

    @Test
    @DisplayName("deleteById should delete a transaction by ID")
    void deleteById_shouldDeleteTransaction() {
        //GIVEN an existing transaction
        Transaction transactionToDelete = transactionRepository.save(transaction);
        assertTrue(transactionRepository.existsById(transactionToDelete.getId()));
        // WHEN deleting transaction
        transactionRepository.deleteById(transactionToDelete.getId());
        //THEn the transaction should not be found
        assertTrue(transactionRepository.findById(transactionToDelete.getId()).isEmpty());
    }
}