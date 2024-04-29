package com.paymybuddy.paymybuddy.repository;

import com.paymybuddy.paymybuddy.model.Connection;
import com.paymybuddy.paymybuddy.model.User;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ConnectionRepository extends CrudRepository<Connection, Integer> {
    List<Connection> findByInitializerOrReceiver(User initializer, User receiver);

    Optional<Connection> findById(Integer id);
}
