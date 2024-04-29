package com.paymybuddy.paymybuddy.controller;

import com.paymybuddy.paymybuddy.exceptions.BuddyNotFoundException;
import com.paymybuddy.paymybuddy.model.User;
import com.paymybuddy.paymybuddy.model.viewmodel.ConnectionViewModel;
import com.paymybuddy.paymybuddy.model.viewmodel.TransactionViewModel;
import com.paymybuddy.paymybuddy.model.viewmodel.UserViewModel;
import com.paymybuddy.paymybuddy.service.ConnectionService;
import com.paymybuddy.paymybuddy.service.TransactionService;
import com.paymybuddy.paymybuddy.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@Slf4j
@RestController
@RequestMapping("/user")
public class UserController {

    @Autowired
    private UserService userService;

    @Autowired
    private ConnectionService  connectionService;
    @Autowired
    private TransactionService transactionService;

    /**
     * Add new user.
     *
     * @param user user with firstname, lastname, email and password.
     *
     * @return User saved or exception if email already used
     */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public User createUser(@RequestBody User user) {
        return userService.createUser(user);
    }

    /**
     * Lists all users.
     *
     * @return List of all users.
     */
    @GetMapping
    public List<UserViewModel> getUsers() {
        return userService.getUsers();
    }


    /**
     * Gets user by email
     *
     * @param id
     *         email of user to find
     *
     * @return optional user
     */
    @GetMapping("/{id}")
    public Optional<UserViewModel> getUserById(@PathVariable(name = "id") Integer id) {
        Optional<User> userById = userService.getUserById(id);
        if (userById.isEmpty())  return Optional.empty();
        return Optional.of(UserService.userToViewModel(userById.get()));
    }


    /**
     * Deposits money to user account.
     *
     * @param amount
     *         amount to deposit
     */
    @PutMapping("/deposit")
    public UserViewModel deposit(@RequestParam String amount) {
        userService.deposit(userService.getAuthenticatedUser(), amount);
        return UserService.userToViewModel(userService.getAuthenticatedUser());
    }

    /**
     * Withdraws money to user account.
     *
     * @param amount
     *         amount to withdraw
     */
    @PutMapping("/withdraw")
    public UserViewModel withdraw(@RequestParam String amount) {
        userService.withdraw(userService.getAuthenticatedUser(), amount);
        return UserService.userToViewModel(userService.getAuthenticatedUser());
    }

    /**
     * Adds a connection to a user
     *
     * @param email
     *         email of buddy to add
     *
     * @return new connection object
     */
    @PostMapping("/add-connection")
    @ResponseStatus(HttpStatus.CREATED)
    public ConnectionViewModel addConnection(@RequestParam String email) {
        return ConnectionService.connectionToViewModel(connectionService.createConnectionBetweenTwoUsers(userService.getAuthenticatedUser(),
                                                                                                         email));
    }

    /**
     * Get user connections.
     *
     * @param id
     *         user for which the connections are wanted
     *
     * @return a list of connections
     */
    @GetMapping("/{id}/connections")
    public List<UserViewModel> getConnections(@PathVariable Integer id) {
        return connectionService.getUserConnections(getUser(id));
    }

    /**
     * Creates a transaction involving the user and the buddy behind the specified email.
     *
     * @param email
     *         transaction receiver email
     * @param description
     *         short description for transaction
     * @param amount
     *         amount of transaction
     *
     * @return a transaction object
     */
    @PostMapping("/pay")
    @ResponseStatus(HttpStatus.CREATED)
    public TransactionViewModel payABuddy(@RequestParam String email,
                                 @RequestParam String description,
                                 @RequestParam double amount) {
        if (userService.getUserByEmail(email).isEmpty()) {
            String errorMessage = "The buddy with " +
                                  "email (" + email + ") does not exist.";
            log.error(errorMessage);
            throw new BuddyNotFoundException(errorMessage);
        }
        return TransactionService.transactionToViewModel(transactionService.createTransaction(userService.getAuthenticatedUser(),
                                                    userService.getUserByEmail(email).get(),
                                                    description,
                                                    amount));
    }

    /**
     * Get user connections.
     *
     * @param id
     *         user for which the transactions are wanted
     *
     * @return a list of transactions
     */
    @GetMapping("/{id}/transactions")
    public List<TransactionViewModel> getTransactions(@PathVariable Integer id) {
        return transactionService.getUserTransactions(id);
    }


    /**
     * Useful function to get a User object thanks to an ID
     *
     * @param id
     *         id of user to return
     *
     * @return a User object
     */
    protected User getUser(Integer id) {
        Optional<User> user = userService.getUserById(id);
        if (user.isEmpty()) {
            String errorMessage = "User with ID " + id + " does not exist.";
            log.error(errorMessage);
            throw new BuddyNotFoundException(errorMessage);
        }
        return user.get();
    }

}
