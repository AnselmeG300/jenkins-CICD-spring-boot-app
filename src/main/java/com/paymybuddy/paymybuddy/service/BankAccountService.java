package com.paymybuddy.paymybuddy.service;

import com.paymybuddy.paymybuddy.model.BankAccount;
import com.paymybuddy.paymybuddy.model.User;
import com.paymybuddy.paymybuddy.repository.BankAccountRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import javax.transaction.Transactional;
import java.math.BigDecimal;
import java.time.Clock;
import java.util.List;
import java.util.Optional;

@Service
@Slf4j
public class BankAccountService {
	@Autowired
	BankAccountRepository bankAccountRepository;
	@Autowired
	UserService           userService;
	@Autowired
	PaginationService paginationService;
	@Autowired
	Clock                 clock;

	/**
	 * Saves a new bank account.
	 */
	@Transactional
	public BankAccount createBankAccount(User user, String bankName, String iban, BigDecimal balance) {
		Assert.notNull(user, "User must not be null");
		Assert.notNull(balance, "Balance must not be null");
		return bankAccountRepository.save(new BankAccount(user, bankName, iban, balance));
	}

	/**
	 * Lists all bank accounts in database
	 *
	 * @return a list of bank accounts
	 */
	public List<BankAccount> getBankAccounts() {
		return (List<BankAccount>) bankAccountRepository.findAll();
	}

	/**
	 * Returns a bank account given an ID.
	 * @param id ID  bank account needed.
	 * @return a bank account if exists, empty optional otherwise.
	 *
	 * @see com.paymybuddy.paymybuddy.model.BankAccount
	 */
	public Optional<BankAccount> getBankAccountById(Integer id) {
		Assert.notNull(id, "User ID must not be null");
		return bankAccountRepository.findById(id);
	}

	/**
	 * Deletes a bank account.
	 *
	 * @param bankAccount
	 *         Bank account to delete.
	 */
	@Transactional
	public void deleteBankAccount(BankAccount bankAccount) {
		Assert.notNull(bankAccount, "User must not be null");
		bankAccountRepository.delete(bankAccount);
	}

}

