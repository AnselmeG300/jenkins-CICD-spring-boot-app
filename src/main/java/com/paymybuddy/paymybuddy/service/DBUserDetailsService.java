package com.paymybuddy.paymybuddy.service;

import com.paymybuddy.paymybuddy.exceptions.BuddyNotFoundException;
import com.paymybuddy.paymybuddy.model.User;
import com.paymybuddy.paymybuddy.repository.UserRepository;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.Optional;

@Service
public class DBUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    public DBUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /**
     * Spring security implementation of loadByUsername.
     * @param username email provided during login
     * @return a UserDetails object
     * @throws com.paymybuddy.paymybuddy.exceptions.BuddyNotFoundException Exception thrown if the provided email does not match any user
     */
    @Override public UserDetails loadUserByUsername(String username) throws BuddyNotFoundException {
        // source: https://youtu.be/TNt3GHuayXs
        Optional<User> user = userRepository.findByEmail(username);
        if (user.isEmpty()) {
            throw new BuddyNotFoundException("Email " + username + " does not match any Buddy.");
        }
        return new org.springframework.security.core.userdetails.User(
                user.get().getEmail(), user.get().getPassword(), true, true,
                true, true,
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER")));

    }
}
