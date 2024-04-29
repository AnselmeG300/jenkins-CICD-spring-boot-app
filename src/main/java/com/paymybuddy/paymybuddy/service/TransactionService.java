package com.paymybuddy.paymybuddy.service;

import com.paymybuddy.paymybuddy.constants.Fee;
import com.paymybuddy.paymybuddy.exceptions.BuddyNotFoundException;
import com.paymybuddy.paymybuddy.exceptions.InsufficientBalanceException;
import com.paymybuddy.paymybuddy.exceptions.InvalidAmountException;
import com.paymybuddy.paymybuddy.exceptions.InvalidPayeeException;
import com.paymybuddy.paymybuddy.model.Transaction;
import com.paymybuddy.paymybuddy.model.User;
import com.paymybuddy.paymybuddy.model.viewmodel.TransactionViewModel;
import com.paymybuddy.paymybuddy.repository.TransactionRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import javax.transaction.Transactional;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Clock;
import java.time.LocalDateTime;
import java.util.*;

@Service
@Slf4j
public class TransactionService {
	@Autowired
	TransactionRepository transactionRepository;
	@Autowired
	ConnectionService     connectionService;
	@Autowired
	UserService           userService;
	@Autowired
	PaginationService paginationService;
	@Autowired
	Clock                 clock;

	/**
	 * Saves a new transaction.
	 */
	@Transactional
	public Transaction createTransaction(User issuer, User payee, String description, double amount) {
		Assert.notNull(issuer, "Issuer must not be null");
		Assert.notNull(payee, "Payee must not be null");
		// Check that amount is not negative nor 0
		if (amount < 0) {
			String errorMessage = "Transaction amount can not be negative.";
			log.error(errorMessage);
			throw new InvalidAmountException(errorMessage);
		}
		if (amount == 0) {
			String errorMessage = "Transaction amount must be more than 0.";
			log.error(errorMessage);
			throw new InvalidAmountException(errorMessage);
		}
		// Calculate fee and total amount
		BigDecimal amountWithFee = calculateAmountWithFee(amount).get("amountWithFee");

		// Check that issuer has enough money for this transaction
		if (issuer.getBalance().compareTo(amountWithFee) < 0) {
			String errorMessage = "Issuer has insufficient balance to make this transfer.";
			log.error(errorMessage);
			throw new InsufficientBalanceException(errorMessage);
		}
		// Check that buddy is making a transaction with a connection
		if (!connectionService.getUserConnections(issuer).contains(UserService.userToViewModel(payee))) {
			String errorMessage = "The payee is not a buddy from issuer.";
			log.error(errorMessage);
			throw new InvalidPayeeException(errorMessage);
		}
		// Withdraw amount with applied fee from issuer's balance
		issuer.setBalance(issuer.getBalance().subtract(amountWithFee));
		// Credit payee
		BigDecimal transactionAmount = new BigDecimal(Double.toString(amount))
				.setScale(Fee.SCALE, RoundingMode.HALF_UP);
		payee.setBalance(payee.getBalance()
				.add(BigDecimal.valueOf(amount)
						.setScale(Fee.SCALE, RoundingMode.HALF_UP)));
		// Update transaction with all information before saving
		Transaction transaction = new Transaction();
		transaction.setIssuer(issuer);
		transaction.setPayee(payee);
		transaction.setAmount(transactionAmount);
		transaction.setDate(LocalDateTime.now(clock));
		transaction.setDescription(description);

		issuer.getInitiatedTransactions().add(transaction);
		payee.getReceivedTransactions().add(transaction);

		return transactionRepository.save(transaction);
	}

	/**
	 * Calculates fee information for a given amount.
	 *
	 * @param amount Transaction amount
	 * @return a HashMap with transaction amount, calculated fee, and amount with fee information
	 */
	public Map<String, BigDecimal> calculateAmountWithFee(double amount) {
		// Create the Map
		HashMap<String, BigDecimal> amountAndFee = new HashMap<>();
		BigDecimal bdAmount = new BigDecimal(Double.toString(amount))
				.setScale(Fee.SCALE, RoundingMode.HALF_UP);
		BigDecimal bdFee = new BigDecimal(Double.toString(amount * Fee.TRANSACTION_FEE))
				.setScale(Fee.SCALE, RoundingMode.HALF_UP);
		amountAndFee.put("amount", bdAmount);
		amountAndFee.put("fee", bdFee);
		amountAndFee.put("amountWithFee", bdAmount.add(bdFee));
		return amountAndFee;
	}

	/**
	 * Lists all connections in data base
	 *
	 * @return a list of connections
	 */
	public List<TransactionViewModel> getTransactions() {
		Iterable<Transaction>      transactions          = transactionRepository.findAll();
		List<TransactionViewModel> transactionViewModels = new ArrayList<>();
		// extract info from user to user view model
		transactions.forEach(transaction -> transactionViewModels.add(transactionToViewModel(transaction)));
		return transactionViewModels;
	}

	public Optional<TransactionViewModel> getTransactionById(Integer id) {
		Assert.notNull(id, "User ID must not be null");
		if (transactionRepository.findById(id).isPresent()) {
			return Optional.of(transactionToViewModel(transactionRepository.findById(id).get()));
		} else {
			return Optional.empty();
		}
	}

	/**
	 * List all user's transactions.
	 *
	 * @param id user ID for which the transactions are wanted
	 * @return a list of transactions
	 */
	public List<TransactionViewModel> getUserTransactions(Integer id) {
		Assert.notNull(id, "User ID must not be null");
		if (userService.getUserById(id).isEmpty()) {
			log.error("User does not exist.");
			throw new BuddyNotFoundException("User does not exist.");
		}
		User                       user         = userService.getUserById(id).get();
		List<TransactionViewModel> transactions = new ArrayList<>();
		// Get all transactions where user is involved
		List<Transaction> transactionsWhereUserIsInvolved = transactionRepository
				.findByIssuerOrPayee(user, user);
		log.debug("Found transactions involving " + user.getEmail() + ":\n" + transactionsWhereUserIsInvolved);
		transactionsWhereUserIsInvolved.forEach(transaction -> transactions.add(transactionToViewModel(transaction)));
		log.info("Transactions with " + user.getEmail() + ":\n" + transactions);
		return transactions;
	}

	/**
	 * Returns a paginated list of user's transactions.
	 *
	 * @param pageable Pageable object.
	 * @param id       Id of connected user.
	 * @return a paginated list of transactions.
	 */
	public Page<?> getPaginatedUserTransactions(Pageable pageable, Integer id) {
		// Get raw list of transactions
		List<TransactionViewModel> transactions = getUserTransactions(id);
		return paginationService.getPaginatedList(pageable, transactions);
	}

	public static TransactionViewModel transactionToViewModel(Transaction transaction) {
		return new TransactionViewModel(transaction.getId(),
				UserService.userToViewModel(transaction.getIssuer()),
				UserService.userToViewModel(transaction.getPayee()),
				transaction.getDate(), transaction.getAmount(), transaction.getDescription());
	}

}

