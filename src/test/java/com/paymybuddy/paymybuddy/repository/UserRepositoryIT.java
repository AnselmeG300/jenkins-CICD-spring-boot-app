package com.paymybuddy.paymybuddy.repository;

import com.paymybuddy.paymybuddy.model.User;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DataJpaTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class UserRepositoryIT {
    @Autowired
    UserRepository userRepository;

    private User user;

    @BeforeAll
    public void setup() {
        user = new User();
        user.setFirstName("Test");
        user.setLastName("UN");
        user.setEmail("abc@mail.com");
        user.setBalance(new BigDecimal(0));
    }

    @Test
    @DisplayName("Saving a new user should add an ID to User object")
    void createUser_savesNewUser() {
        User savedUser = userRepository.save(user);
        assertThat(savedUser.getId()).isNotNull();
    }


    @Test

    @DisplayName("findByID should return a user when the user exists")
    public void findById_shouldReturn_aUser() {
        //GIVEN an existing user
        User userToFind = userRepository.save(user);
        // WHEN trying to findById
        Optional<User> user = userRepository.findById(userToFind.getId());
        //THEn a user should be found
        assertTrue(user.isPresent());
    }

    @Test
    @DisplayName("findByEmail should return a user when the user exists")
    void findByEmail_shouldReturn_aUser() {
        //GIVEN an existing user
        User userToFind = userRepository.save(user);
        // WHEN trying to findByEmail
        Optional<User> user = userRepository.findByEmail(userToFind.getEmail());
        //THEn a user should be found
        assertTrue(user.isPresent());
    }

    @Test
    @DisplayName("findByFirstNameAndLastName should return a user when the user exists")
    void findByFirstNameAndLastName_shouldReturn_aUser() {
        //GIVEN an existing user
        User userToFind = userRepository.save(user);
        // WHEN trying to findByFirstNameAndLastName
        Optional<User> user = userRepository.findByFirstNameAndLastName(userToFind.getFirstName(),
                                                                        userToFind.getLastName());
        //THEn a user should be found
        assertTrue(user.isPresent());
    }

    @Test
    @DisplayName("deleteById should delete a user")
    void deleteUser_shouldDeleteUser() {
        //GIVEN an existing user
        User userToDelete = userRepository.save(user);
        assertTrue(userRepository.existsById(userToDelete.getId()));
        // WHEN deleting user
        userRepository.deleteById(userToDelete.getId());
        //THEn the user should not be found
        assertTrue(userRepository.findById(userToDelete.getId()).isEmpty());
    }
}