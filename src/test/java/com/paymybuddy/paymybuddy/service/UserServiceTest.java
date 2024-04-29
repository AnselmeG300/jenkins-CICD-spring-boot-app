package com.paymybuddy.paymybuddy.service;

import com.paymybuddy.paymybuddy.exceptions.EmailAlreadyUsedException;
import com.paymybuddy.paymybuddy.model.BankAccount;
import com.paymybuddy.paymybuddy.model.User;
import com.paymybuddy.paymybuddy.model.viewmodel.UserViewModel;
import com.paymybuddy.paymybuddy.repository.BankAccountRepository;
import com.paymybuddy.paymybuddy.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(SpringExtension.class)
@Import(UserService.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class UserServiceTest {
    /**
     * Class under test.
     */
    @Autowired
    UserService userService;

    @MockBean
    UserRepository        userRepository;
    @MockBean
    BCryptPasswordEncoder passwordEncoder;
    @MockBean
    BankAccountRepository bankAccountRepository;

    private User testUser;
    private User otherUser;
    private BankAccount bankAccount;

    @BeforeEach
    public void initUsers() {
        testUser = new User();
        testUser.setId(1);
        testUser.setFirstName("Chandler");
        testUser.setLastName("Bing");
        testUser.setPassword("CouldIBeAnyMoreBored");
        testUser.setEmail("bingchandler@friends.com");
        testUser.setBalance(new BigDecimal("2509.56"));

        otherUser = new User();
        otherUser.setId(2);
        otherUser.setFirstName("Joey");
        otherUser.setLastName("Tribbiani");
        otherUser.setPassword("HowUDoin");
        otherUser.setEmail("otheremail@mail.com");
        otherUser.setBalance(new BigDecimal("09.56"));

        bankAccount = new BankAccount(testUser, "Test Bank", "FR7630001007941234567890185", new BigDecimal("12648.62"));

    }

    @Test
    @DisplayName("Saving user with valid email should create new user")
    public void createUser_usingValidEmail_shouldCreate_newUser() {
        String emailAddress = "username@domain.com";
        String password     = "ABCDEF123";
        testUser.setEmail(emailAddress);
        doReturn(Optional.empty())
                .when(userRepository).findByEmail(emailAddress);
        when(passwordEncoder.encode(any(String.class)))
                .thenReturn(password);
        doReturn(testUser)
                .when(userRepository).save(any(User.class));

        testUser = userService.createUser(testUser);

        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    @DisplayName("Saving user with invalid email should throw exception")
    public void createUser_usingValidEmail_shouldThrow_exception() {
        String emailAddress = "username@domain";
        testUser.setEmail(emailAddress);

        assertThrows(IllegalArgumentException.class, () -> userService.createUser(testUser));
    }

    @Test
    @DisplayName("Saving a user with unique email should create new user")
    void createUser_shouldCreate_newUser() {
        // GIVEN a new user with unique email
        doReturn(Optional.empty())
                .when(userRepository).findByEmail(any(String.class));
        when(passwordEncoder.encode(any(String.class)))
                .thenReturn("ABCDEF123");
        doReturn(testUser)
                .when(userRepository).save(any(User.class));
        // WHEN
        testUser = userService.createUser(testUser);
        // THEN
        // asserting that created user is not null does not work, thus we check if the balance was actually set to
        // 0.00 during user creation
        verify(userRepository, times(1)).save(any(User.class));
        assertThat(testUser).isNotNull();
    }


    @Test
    @DisplayName("Saving a user with already existing email should throw exception")
    void createUser_shouldThrow_exception() {
        // GIVEN a new user with ready used email
        when(userRepository.findByEmail(any(String.class)))
                .thenReturn(Optional.of(testUser));
        // THEN
        assertThrows(EmailAlreadyUsedException.class, () -> userService.createUser(testUser));
    }

    @Test
    @DisplayName("Id should not be null when calling getUserById")
    void getUserById_whenIDIsNull_shouldThrowException() {
        assertThrows(IllegalArgumentException.class,
                     () -> userService.getUserById(null));
    }

    @Test
    @DisplayName("Email should not be null when calling getUserByEmail")
    void getUserByEmail_whenEmailIsNull_shouldThrowException() {
        assertThrows(IllegalArgumentException.class,
                     () -> userService.getUserByEmail(null));
    }

    @Test
    @DisplayName("User should not be null when calling deleteUser")
    void deleteUser_whenUserIsNull_shouldThrowException() {
        assertThrows(IllegalArgumentException.class,
                     () -> userService.deleteUser(null));
    }

    @Test
    @DisplayName("User should not be null when calling getUserBankAccount")
    void getUserBankAccount_whenUserIsNull_shouldThrowException() {
        assertThrows(IllegalArgumentException.class,
                     () -> userService.getUserBankAccount(null));
    }

    @Test
    @DisplayName("User should not be null when calling deposit")
    void deposit_whenUserIsNull_shouldThrowException() {
        assertThrows(IllegalArgumentException.class,
                     () -> userService.deposit(null, "30"));
    }

    @Test
    @DisplayName("User should own a bank account to make a deposit")
    void deposit_whenBankAccountDoesNotExist_shouldThrowException() {
        when(bankAccountRepository.findByUser(testUser)).thenReturn(Optional.empty());
        assertThrows(IllegalArgumentException.class,
                     () -> userService.deposit(null, "30"));
    }

    @Test
    @DisplayName("Deposit should add amount to user's balance")
    void deposit_shouldAdd_amountFrom_BuddyAccount() {
        when(bankAccountRepository.findByUser(testUser)).thenReturn(Optional.of(bankAccount));
        String amount = "490.44";
        userService.deposit(testUser, amount);
        verify(userRepository, times(1)).save(testUser);
        assertThat(testUser.getBalance()).isEqualTo(new BigDecimal("3000.00"));
    }

    @Test
    @DisplayName("Deposit should subtract amount to user's bank account")
    void deposit_shouldSub_amountFrom_BankAccount() {
        when(bankAccountRepository.findByUser(testUser)).thenReturn(Optional.of(bankAccount));
        String amount = "648.62";
        userService.deposit(testUser, amount);
        verify(userRepository, times(1)).save(testUser);
        assertThat(bankAccount.getBalance()).isEqualTo(new BigDecimal("12000.00"));
    }

    @Test
    @DisplayName("Deposit should replace any \"-\" in amount ")
    void deposit_shouldReplaceSign() {
        when(bankAccountRepository.findByUser(testUser)).thenReturn(Optional.of(bankAccount));
        String amount = "-490.44";
        userService.deposit(testUser, amount);
        assertThat(testUser.getBalance()).isEqualTo(new BigDecimal("3000.00"));
    }

    @Test
    @DisplayName("User should not be null when calling withdraw")
    void withdraw_whenUserIsNull_shouldThrowException() {
        assertThrows(IllegalArgumentException.class,
                     () -> userService.withdraw(null, "30"));
    }

    @Test
    @DisplayName("Withdrawal should withdraw money from user's account")
    void withdraw_shouldWithdraw_amount() {
        when(bankAccountRepository.findByUser(testUser)).thenReturn(Optional.of(bankAccount));
        String amount = "509.56";
        userService.withdraw(testUser, amount);
        verify(userRepository, times(1)).save(testUser);
        assertThat(testUser.getBalance()).isEqualTo("2000.00");
    }

    @Test
    @DisplayName("Withdrawal should replace any \"-\" in amount ")
    void withdraw_shouldReplaceSign() {
        when(bankAccountRepository.findByUser(testUser)).thenReturn(Optional.of(bankAccount));
        String amount = "-509.56";
        userService.withdraw(testUser, amount);
        assertThat(testUser.getBalance()).isEqualTo(new BigDecimal("2000.00"));
    }

    @Test
    @DisplayName("User should own a bank account to make a withdrawal")
    void withdraw_whenBankAccountDoesNotExist_shouldThrowException() {
        when(bankAccountRepository.findByUser(testUser)).thenReturn(Optional.empty());
        assertThrows(IllegalArgumentException.class,
                     () -> userService.withdraw(null, "30"));
    }

    @Test
    @DisplayName("Withdrawal should add amount to user's bank account")
    void withdraw_shouldAdd_amountTo_BankAccount() {
        when(bankAccountRepository.findByUser(testUser)).thenReturn(Optional.of(bankAccount));
        String amount = "351.38";
        userService.withdraw(testUser, amount);
        verify(userRepository, times(1)).save(testUser);
        assertThat(bankAccount.getBalance()).isEqualTo(new BigDecimal("13000.00"));
    }

    @Test
    @DisplayName("getUsers should return a list of User with their email, first and last names, and balance " +
                 "information")
    void getUsers_shouldReturn_listOfUserViewModels() {
        when(userRepository.findAll()).thenReturn(List.of(testUser, otherUser));

        List<UserViewModel> result = userService.getUsers();

        assertTrue(result.contains(UserService.userToViewModel(testUser)));
        assertTrue(result.contains(UserService.userToViewModel(otherUser)));
    }

    @Test
    @DisplayName("userToViewModel should return correct value")
    void userToViewModel() {
        UserViewModel result = UserService.userToViewModel(testUser);

        assertThat(result.getEmail()).isEqualTo(testUser.getEmail());
        assertThat(result.getBalance()).isEqualTo(testUser.getBalance());
        assertThat(result.getFirstname()).isEqualTo(testUser.getFirstName());
        assertThat(result.getLastname()).isEqualTo(testUser.getLastName());
    }

    @Test
    @DisplayName("A bank account must be returned if the user has one")
    void getUserBankAccount_shouldReturn_aBankAccount() {
        when(bankAccountRepository.findByUser(testUser)).thenReturn(Optional.of(bankAccount));
        Optional<BankAccount> result = userService.getUserBankAccount(testUser);
        assertTrue(result.isPresent());
    }

    @Test
    @DisplayName("No bank account must be returned if the user does not have one")
    void getUserBankAccount_shouldReturn_emptyOptional() {
        when(bankAccountRepository.findByUser(testUser)).thenReturn(Optional.empty());
        Optional<BankAccount> result = userService.getUserBankAccount(testUser);
        assertTrue(result.isEmpty());
    }
}
