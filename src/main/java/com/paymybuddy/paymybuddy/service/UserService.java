package com.paymybuddy.paymybuddy.service;

import com.paymybuddy.paymybuddy.constants.EmailValidator;
import com.paymybuddy.paymybuddy.constants.Fee;
import com.paymybuddy.paymybuddy.exceptions.BankAccountNotFoundException;
import com.paymybuddy.paymybuddy.exceptions.BuddyNotFoundException;
import com.paymybuddy.paymybuddy.exceptions.EmailAlreadyUsedException;
import com.paymybuddy.paymybuddy.model.BankAccount;
import com.paymybuddy.paymybuddy.model.User;
import com.paymybuddy.paymybuddy.model.viewmodel.UserViewModel;
import com.paymybuddy.paymybuddy.repository.BankAccountRepository;
import com.paymybuddy.paymybuddy.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import javax.transaction.Transactional;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;

@Service
@Slf4j
public class UserService {

    /**
     * Instance of UserRepository.
     */
    @Autowired
    private final UserRepository userRepository;

    @Autowired
    BankAccountRepository bankAccountRepository;

    @Autowired
    private final BCryptPasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository,
                       BCryptPasswordEncoder passwordEncoder, BankAccountRepository bankAccountRepository) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.bankAccountRepository = bankAccountRepository;
    }

    /**
     * Saves user to database.
     *
     * @param user
     *         user with firstname, lastname, email and password filled from signup form.
     *
     * @return User with id.
     */
    @Transactional
    public User createUser(User user) {
        String email = user.getEmail();
        if (isInvalidEmail(email)) {
            String invalidEmailMessage = "The email provided is invalid.";
            log.error(invalidEmailMessage);
            throw new IllegalArgumentException(invalidEmailMessage);
        }

        // Detect if email is already used
        Optional<User> existingUserWithEmail = userRepository.findByEmail(email);
        if (existingUserWithEmail.isPresent()) {
            String errorMessage = "Email " + email + " is already used." +
                                  " Please sign in with another email.";
            log.error(errorMessage);
            throw new EmailAlreadyUsedException(errorMessage);
        }
        user.setBalance(new BigDecimal("0.00"));
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        return userRepository.save(user);
    }

    /**
     * Lists all users in database.
     *
     * @return a set of users.
     */
    public List<UserViewModel> getUsers() {
        Iterable<User>      users          = userRepository.findAll();
        List<UserViewModel> usersViewModel = new ArrayList<>();
        // extract info from user to user view model
        users.forEach(user -> usersViewModel.add(userToViewModel(user)));
        return usersViewModel;
    }

    /**
     * Finds a user by their id.
     *
     * @param id
     *         User id.
     *
     * @return found user or empty optional.
     */
    public Optional<User> getUserById(Integer id) {
        Assert.notNull(id, "User ID must not be null");
        return userRepository.findById(id);
    }

    /**
     * Finds a user by their email address.
     *
     * @param email
     *         User's email address.
     *
     * @return optional user.
     */
    public Optional<User> getUserByEmail(String email) {
        Assert.notNull(email, "Email must not be null");
        return userRepository.findByEmail(email);
    }

    /**
     * Deletes a user.
     *
     * @param user
     *         User to delete.
     */
    @Transactional
    public void deleteUser(User user) {
        Assert.notNull(user, "User must not be null");
        userRepository.delete(user);
    }

    /**
     * Email validator.
     *
     * @param emailAddress
     *         email address to validate
     *
     * @return true if valid, false otherwise.
     */
    public static boolean isInvalidEmail(String emailAddress) {
        return !Pattern.compile(EmailValidator.REGEX_PATTERN)
                       .matcher(emailAddress)
                       .matches();
    }


    /**
     * Returns provided user's bank account.
     *
     * @param user
     *         user for which the bank account is needed.
     *
     * @return user bank account if exists, empty optional otherwise.
     */
    public Optional<BankAccount> getUserBankAccount(User user) {
        Assert.notNull(user, "User must not be null");
        return bankAccountRepository.findByUser(user);
    }


    /**
     * Deposits money to buddy account.
     */
    @Transactional
    public void deposit(User user, String amount) {
        Assert.notNull(user, "User must not be null");
        // if amount is still not valid after interface's validator, remove any negative signs
        amount = amount.replace("-", "");
        // find user's bank account
        Optional<BankAccount> bankAccount = bankAccountRepository.findByUser(user);
        // deposit cannot be done if the user does not own a bank account
        if (bankAccount.isEmpty()) {
            log.error("The user does not have any bank account. Deposit can not be proceeded.");
            throw new BankAccountNotFoundException("The user does not have any bank account. Deposit can not be proceeded.");
        }
        BigDecimal amountToDeposit = new BigDecimal(amount).setScale(Fee.SCALE, RoundingMode.HALF_UP);
        // Subtract amount to deposit from bank account
        bankAccount.get().setBalance(bankAccount.get().getBalance().subtract(amountToDeposit));
        // add amount to deposit to user's account
        user.setBalance(user.getBalance().add(amountToDeposit));
        userRepository.save(user);
    }

    /**
     * Withdraws money to user's bank account.
     */
    @Transactional
    public void withdraw(User user, String amount) {
        Assert.notNull(user, "User must not be null");
        // if amount is still not valid after interface's validator, remove any negative signs
        amount = amount.replace("-", "");
        // find user's bank account
        Optional<BankAccount> bankAccount = bankAccountRepository.findByUser(user);
        // withdraw cannot be done if the user does not own a bank account
        if (bankAccount.isEmpty()) {
            log.error("The user does not have any bank account. Withdrawal can not be proceeded.");
            throw new BankAccountNotFoundException("The user does not have any bank account. Withdrawal can not be " +
                                                   "proceeded.");
        }
        BigDecimal amountToWithdraw = new BigDecimal(amount).setScale(Fee.SCALE, RoundingMode.HALF_UP);
        // add amount to deposit from bank account
        bankAccount.get().setBalance(bankAccount.get().getBalance().add(amountToWithdraw));
        // subtract amount to deposit to user's account
        user.setBalance(user.getBalance().subtract(amountToWithdraw));
        userRepository.save(user);
    }

    /**
     * Converts User entity to UserViewModel.
     *
     * @param user
     *         user to convert
     *
     * @return a UserViewModel object
     *
     * @see com.paymybuddy.paymybuddy.model.viewmodel.UserViewModel
     */
    public static UserViewModel userToViewModel(User user) {
        return new UserViewModel(user.getId(), user.getEmail(), user.getFirstName(), user.getLastName(),
                                 user.getBalance());
    }

    /**
     * Returns authenticated user using Spring security context.
     *
     * @return authenticated user
     *
     * @see com.paymybuddy.paymybuddy.model.User
     */
    public User getAuthenticatedUser() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        if (getUserByEmail(username).isEmpty()) {
            throw new BuddyNotFoundException("Email " + username + " does not match any Buddy.");
        }
        return getUserByEmail(username).get();
    }

}
