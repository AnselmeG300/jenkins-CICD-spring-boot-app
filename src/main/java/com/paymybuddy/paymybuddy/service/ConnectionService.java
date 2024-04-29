package com.paymybuddy.paymybuddy.service;

import com.paymybuddy.paymybuddy.exceptions.AlreadyABuddyException;
import com.paymybuddy.paymybuddy.exceptions.BuddyNotFoundException;
import com.paymybuddy.paymybuddy.model.Connection;
import com.paymybuddy.paymybuddy.model.User;
import com.paymybuddy.paymybuddy.model.viewmodel.ConnectionViewModel;
import com.paymybuddy.paymybuddy.model.viewmodel.UserViewModel;
import com.paymybuddy.paymybuddy.repository.ConnectionRepository;
import com.paymybuddy.paymybuddy.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.time.Clock;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@Slf4j
public class ConnectionService {

	@Autowired
	ConnectionRepository connectionRepository;

	@Autowired
	PaginationService paginationService;

	@Autowired
	UserRepository userRepository;
	@Autowired
	Clock          clock;

	/**
	 * List all user's connection
	 *
	 * @param user user for which the connections are wanted
	 * @return a list of user with their name, first name, last name and balance
	 */
	public List<UserViewModel> getUserConnections(User user) {
		Integer             userId      = user.getId();
		List<UserViewModel> connections = new ArrayList<>();
		// Get all connections where user is involved
		List<Connection> connectionsWhereUserIsInvolved = connectionRepository
				.findByInitializerOrReceiver(user, user);
		log.debug("Found connections involving " + user.getEmail() + ":\n" + connectionsWhereUserIsInvolved);
		// If user was initializer, then add the receiver to list and vice versa
		for (Connection connection : connectionsWhereUserIsInvolved) {
			User initializer = connection.getInitializer();
			User receiver    = connection.getReceiver();
			if (userId.equals(initializer.getId())) {
				connections.add(UserService.userToViewModel(receiver));
			} else if (userId.equals(receiver.getId())) {
				connections.add(UserService.userToViewModel(initializer));
			}
		}
		log.info("Connections for " + user.getEmail() + ":\n" + connections);
		return connections;
	}


	/**
	 * Returns a paginated list of user's connections.
	 *
	 * @param pageable Pageable object.
	 * @param user       Connected user.
	 * @return a paginated list of connections.
	 */
	public Page<?> getPaginatedUserConnections(Pageable pageable, User user) {
		// Get raw list of connections
		List<UserViewModel> connections = getUserConnections(user);
		return paginationService.getPaginatedList(pageable, connections);
	}



	/**
	 * Creates a connection between two users and saves it to database.
	 *
	 * @param initializer connection initializer
	 * @param email       buddy to add
	 * @return connection object
	 */
	@Transactional
	public Connection createConnectionBetweenTwoUsers(User initializer, String email) {
		if (UserService.isInvalidEmail(email)) {
			String invalidEmailMessage = "The email provided is invalid.";
			log.error(invalidEmailMessage);
			throw new IllegalArgumentException(invalidEmailMessage);
		}
		if (email.equalsIgnoreCase(initializer.getEmail())) {
			log.error("You are trying to add yourself!");
			throw new IllegalArgumentException("You are trying to add yourself!");
		}
		Optional<User> optionalReceiver = userRepository.findByEmail(email);
		if (optionalReceiver.isEmpty()) {
			// Check if a user with specified email exists
			String errorMessage = "Email " + email + " does not match any buddy.";
			log.error(errorMessage);
			throw new BuddyNotFoundException(errorMessage);
		}
		User receiver = optionalReceiver.get();
		if (getUserConnections(initializer).contains(UserService.userToViewModel(receiver))) {
			String errorMessage = receiver.getFirstName() + " " + receiver.getLastName() + " is already a Buddy!";
			log.error(errorMessage);
			throw new AlreadyABuddyException(errorMessage);
		} else {
			// Create connection with both users
			log.info("Creating new connection between " +
					initializer.getEmail() +
					" and " + receiver.getEmail() + ".");
			return saveConnection(createConnection(initializer, receiver));
		}

	}

	/**
	 * Creates a connection object.
	 *
	 * @param initializer connection initializer
	 * @param receiver    connection receiver
	 * @return connection object
	 */
	protected Connection createConnection(User initializer, User receiver) {
		Connection connection = new Connection();
		connection.setInitializer(initializer);
		connection.setReceiver(receiver);
		connection.setStartingDate(LocalDateTime.now(clock));
		// Add connection to initializer's initiatedConnections
		initializer.getInitializedConnections().add(connection);
		// Add connection to receiver's receivedConnections
		receiver.getReceivedConnections().add(connection);
		return connection;
	}

	/**
	 * Saves connection to database.
	 *
	 * @param connection connection to save
	 * @return saved connection
	 */
	@Transactional
	public Connection saveConnection(Connection connection) {
		return connectionRepository.save(connection);
	}

	/**
	 * Lists all connections in data base
	 *
	 * @return a list of connections
	 */
	public List<ConnectionViewModel> getConnections() {
		Iterable<Connection>      connections          = connectionRepository.findAll();
		List<ConnectionViewModel> connectionViewModels = new ArrayList<>();
		// extract info from user to user view model
		connections.forEach(connection -> connectionViewModels.add(connectionToViewModel(connection)));
		return connectionViewModels;
	}

	/**
	 * Gets a connection by its ID.
	 *
	 * @param id connection to find
	 * @return Optional connection
	 */
	public Optional<ConnectionViewModel> getConnectionById(Integer id) {
		if (connectionRepository.findById(id).isPresent()) {
			return Optional.of(connectionToViewModel(connectionRepository.findById(id).get()));
		} else {
			return Optional.empty();
		}
	}

	public static ConnectionViewModel connectionToViewModel(Connection connection) {
		return new ConnectionViewModel(connection.getId(), UserService.userToViewModel(connection.getInitializer()),
				UserService.userToViewModel(connection.getReceiver()),
				connection.getStartingDate());
	}
}
