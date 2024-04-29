package com.paymybuddy.paymybuddy.repository;

import com.paymybuddy.paymybuddy.model.Transaction;
import com.paymybuddy.paymybuddy.model.User;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TransactionRepository extends CrudRepository<Transaction, Integer> {
	List<Transaction> findByIssuerOrPayee(User issuer, User payee);
	List<Transaction> findByIssuer(User issuer);
	List<Transaction> findByPayee(User payee);
}
