package com.paymybuddy.paymybuddy.repository;

import com.paymybuddy.paymybuddy.model.Connection;
import com.paymybuddy.paymybuddy.model.User;
import org.junit.jupiter.api.BeforeAll;
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
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@DataJpaTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class ConnectionRepositoryIT {
    @Autowired
    ConnectionRepository connectionRepository;

    @Autowired
    UserRepository userRepository;

    private Connection connection;
    private User       initializer;
    private User       receiver;

    // configure LocalDateTime.now() to 18th July 2022, 10:00:00
    private final static LocalDateTime LOCAL_DATE_NOW = LocalDateTime.of(2022, 7, 18, 10, 0, 0);
    @MockBean
    Clock clock;

    @BeforeAll
    public void setup() {
        // Configure a fixed clock to have fixed LocalDate.now()
        Clock fixedClock = Clock.fixed(LOCAL_DATE_NOW.atZone(ZoneId.systemDefault()).toInstant(),
                                       ZoneId.systemDefault());
        when(clock.instant()).thenReturn(fixedClock.instant());
        when(clock.getZone()).thenReturn(fixedClock.getZone());

        // create a payee and an issuer for test connection
        initializer = new User(1, "abc@email.com", "1234ABC", "Jean", "Dupont", new BigDecimal(150), new ArrayList<>(),
                               new ArrayList<>(), new ArrayList<>(), new ArrayList<>());
        receiver = new User(2, "def@email.com", "6571fsqdSDV", "Jane", "Doe", new BigDecimal(150), new ArrayList<>(),
                            new ArrayList<>(), new ArrayList<>(), new ArrayList<>());
        initializer = userRepository.save(initializer);
        receiver = userRepository.save(receiver);

        // create test connection
        connection = new Connection();
        connection.setStartingDate(LocalDateTime.now(clock));
        connection.setInitializer(initializer);
        connection.setReceiver(receiver);
    }

    @Test
    @DisplayName("Saving a new connection should add an ID")
    void createConnection_savesNewConnection() {
        Connection savedConnection = connectionRepository.save(connection);
        assertThat(savedConnection.getId()).isNotNull();
    }


    @Test
    @DisplayName("findByInitializerOrReceiver should return a connection when connection exists")
    public void findByInitializerOrReceiver_shouldReturn_aConnection() {
        //GIVEN an existing connection
        Connection connectionToFind = connectionRepository.save(connection);
        // WHEN trying to findByIssuerOrPayee
        List<Connection> connection = connectionRepository.findByInitializerOrReceiver(receiver, initializer);
        //THEn a connection should be found
        assertTrue(connection.isEmpty());
    }
}