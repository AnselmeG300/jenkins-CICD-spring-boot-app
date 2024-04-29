package com.paymybuddy.paymybuddy.service;

import com.paymybuddy.paymybuddy.exceptions.AlreadyABuddyException;
import com.paymybuddy.paymybuddy.exceptions.BuddyNotFoundException;
import com.paymybuddy.paymybuddy.model.Connection;
import com.paymybuddy.paymybuddy.model.User;
import com.paymybuddy.paymybuddy.model.viewmodel.ConnectionViewModel;
import com.paymybuddy.paymybuddy.model.viewmodel.UserViewModel;
import com.paymybuddy.paymybuddy.repository.ConnectionRepository;
import com.paymybuddy.paymybuddy.repository.UserRepository;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(SpringExtension.class)
@Import(ConnectionService.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class ConnectionServiceTest {
    // configure LocalDateTime.now() to 18th July 2022, 10:00:00
    private final static LocalDateTime LOCAL_DATE_NOW = LocalDateTime.of(2022, 7, 18, 10, 0, 0);

    /**
     * Class under test.
     */
    @Autowired
    ConnectionService connectionService;

    @MockBean
    Clock clock;

    @MockBean
    ConnectionRepository connectionRepository;
    @MockBean
    PaginationService paginationService;

    @MockBean
    UserRepository userRepository;

    private User initializer;
    private User receiver;

    private Connection connection;


    @BeforeAll
    public void initUsers() {
        initializer = new User();
        initializer.setId(1);
        initializer.setFirstName("Chandler");
        initializer.setLastName("Bing");
        initializer.setPassword("CouldIBeAnyMoreBored");
        initializer.setEmail("bingchandler@friends.com");
        initializer.setBalance(new BigDecimal("1250.48"));

        receiver = new User();
        receiver.setId(2);
        receiver.setFirstName("Joey");
        receiver.setLastName("Tribbiani");
        receiver.setPassword("HowUDoin");
        receiver.setEmail("tribbianijoey@friends.com");
        receiver.setBalance(new BigDecimal("0.00"));

        connection = new Connection(1, initializer, receiver, LOCAL_DATE_NOW);
    }

    @BeforeEach
    public void initClock() {
        // Configure a fixed clock to have fixed LocalDate.now()
        Clock fixedClock = Clock.fixed(LOCAL_DATE_NOW.atZone(ZoneId.systemDefault()).toInstant(),
                                       ZoneId.systemDefault());
        when(clock.instant()).thenReturn(fixedClock.instant());
        when(clock.getZone()).thenReturn(fixedClock.getZone());
    }

    @Test
    @DisplayName("getUserConnections should return a list of two connections")
    void getUserConnections() {
        // Given three users
        User testUser = new User(3,
                                 "rossgeller@friends.com",
                                 "wewereonabreak",
                                 "Ross",
                                 "Geller",
                                 new BigDecimal("215.64"),
                                 new ArrayList<>(),
                                 new ArrayList<>(),
                                 new ArrayList<>(),
                                 new ArrayList<>());

        Connection connection1 = new Connection(1, testUser, receiver, LocalDateTime.now(clock));
        Connection connection2 = new Connection(2, initializer, testUser, LocalDateTime.now(clock));
        when(connectionRepository.findByInitializerOrReceiver(any(User.class), any(User.class)))
                .thenReturn(List.of(connection1, connection2));

        // WHEN getting connections from testUser
        List<UserViewModel> userConnections = connectionService.getUserConnections(testUser);

        // THEN testUser should have two connections, one they initiated and one they received
        assertTrue(userConnections.contains(UserService.userToViewModel(receiver)));
        assertTrue(userConnections.contains(UserService.userToViewModel(initializer)));
    }

    @Test
    @DisplayName("Adding user with invalid email should throw exception")
    public void updateUser_usingValidEmail_shouldThrow_exception() {
        String email = "username@domain";

        assertThrows(IllegalArgumentException.class,
                     () -> connectionService.createConnectionBetweenTwoUsers(initializer, email));
    }

    @Test
    @DisplayName("Adding self email should throw exception")
    public void updateUser_usingSelfEmail_shouldThrow_exception() {

        assertThrows(IllegalArgumentException.class,
                () -> connectionService.createConnectionBetweenTwoUsers(initializer, initializer.getEmail()));
    }

    @Test
    @DisplayName("Adding a connection should connect initializer and receiver")
    void addConnection_shouldConnect_initializerAndReceiver() {
        String email = "tribbianijoey@friends.com";
        when(userRepository.findByEmail(email)).thenReturn(Optional.of(receiver));

        connectionService.createConnectionBetweenTwoUsers(initializer, email);

        // Assert both initializer and receiver have a connection in which they appear as such
        assertThat(initializer.getInitializedConnections()
                              .stream()
                              .filter(connection -> connection.getInitializer().equals(initializer)
                                                    && connection.getReceiver().equals(receiver))).isNotNull();
        assertThat(receiver.getReceivedConnections()
                           .stream()
                           .filter(connection -> connection.getInitializer().equals(initializer)
                                                 && connection.getReceiver().equals(receiver))).isNotNull();
    }

    @Test
    @DisplayName("Adding a connection should add a connection to initializer's list of initiated connections")
    void addConnection_shouldAdd_connectionToInitializedConnections() {
        String email = "tribbianijoey@friends.com";
        when(userRepository.findByEmail(email)).thenReturn(Optional.of(receiver));

        int initiatedConnectionsSizeBefore = initializer.getInitializedConnections().size();

        connectionService.createConnectionBetweenTwoUsers(initializer, email);

        assertThat(initializer.getInitializedConnections().size()).isEqualTo(initiatedConnectionsSizeBefore + 1);
    }

    @Test
    @DisplayName("Adding a connection should add a connection to receiver's list of received connections")
    void addConnection_shouldAdd_connectionToReceivedConnections() {
        String email = "tribbianijoey@friends.com";
        when(userRepository.findByEmail(email)).thenReturn(Optional.of(receiver));

        int receivedConnectionsSizeBefore = receiver.getReceivedConnections().size();

        connectionService.createConnectionBetweenTwoUsers(initializer, email);

        assertThat(receiver.getReceivedConnections().size()).isEqualTo(receivedConnectionsSizeBefore + 1);
        verify(connectionRepository, times(1)).save(any(Connection.class));
    }

    @Test
    @DisplayName("Adding a connection with non-existent user should throw an exception")
    void addConnection_shouldThrow_exception() {
        String email = "tribbianijoey@friends.com";
        when(userRepository.findByEmail(email)).thenReturn(Optional.empty());

        assertThrows(BuddyNotFoundException.class,
                     () -> connectionService.createConnectionBetweenTwoUsers(initializer, email));
    }

    @Test
    @DisplayName("Adding a connection who is already a buddy should throw an exception")
    void addConnection_withConflict_shouldThrow_exception() {
        String email = "tribbianijoey@friends.com";
        when(userRepository.findByEmail(email)).thenReturn(Optional.of(receiver));
        Connection existingConnection = new Connection(null, initializer, receiver, LocalDateTime.now(clock));
        when(connectionRepository
                     .findByInitializerOrReceiver(any(User.class), any(User.class)))
                .thenReturn(List.of(existingConnection));

        assertThrows(AlreadyABuddyException.class,
                     () -> connectionService.createConnectionBetweenTwoUsers(initializer, email));
    }

    @Test
    @DisplayName("getConnections should return a list of ConnectionViewModels")
    void getConnections_shouldReturn_listOfConnectionViewModels() {
        when(connectionRepository.findAll()).thenReturn(List.of(connection));

        List<ConnectionViewModel> result = connectionService.getConnections();

        assertTrue(result.contains(ConnectionService.connectionToViewModel(connection)));
    }

    @Test
    @DisplayName("getConnectionById should return a connection when exists")
    void getConnectionById() {
        when(connectionRepository.findById(connection.getId())).thenReturn(Optional.ofNullable(connection));
        Optional<ConnectionViewModel> connectionViewModel = connectionService.getConnectionById(connection.getId());

        assertEquals(connectionViewModel, Optional.of(ConnectionService.connectionToViewModel(connection)));
    }

    @Test
    @DisplayName("getConnectionById should returnempty optional when connection does not exist")
    void getConnectionById_empty() {
        when(connectionRepository.findById(connection.getId())).thenReturn(Optional.empty());
        Optional<ConnectionViewModel> connectionViewModel = connectionService.getConnectionById(connection.getId());

        assertTrue(connectionViewModel.isEmpty());
    }

    @Test
    @DisplayName("connectionToViewModel should return correct value")
    void connectionToViewModel() {
        ConnectionViewModel result = ConnectionService.connectionToViewModel(connection);

        assertTrue(result.getStartingDate().isEqual(connection.getStartingDate()));
        assertThat(result.getId()).isEqualTo(connection.getId());
        assertEquals(result.getInitializer(), UserService.userToViewModel(connection.getInitializer()));
        assertEquals(result.getReceiver(), UserService.userToViewModel(connection.getReceiver()));
    }
}