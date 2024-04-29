CREATE DATABASE db_paymybuddy;
USE db_paymybuddy;

CREATE TABLE user (
    user_id INT NOT NULL PRIMARY KEY AUTO_INCREMENT,
    email VARCHAR(100) NOT NULL UNIQUE,
    password VARCHAR(100) NOT NULL,
    firstname VARCHAR(50) NOT NULL,
    lastname VARCHAR(50) NOT NULL,
    balance DECIMAL(10, 2) NOT NULL DEFAULT 0.0
);

CREATE TABLE bank_account(
    account_id INT NOT NULL AUTO_INCREMENT,
    fk_user_id INT NOT NULL,
    bank_name VARCHAR(100) DEFAULT NULL,
    iban VARCHAR(34) DEFAULT NULL,
    balance DECIMAL(20,2) NOT NULL DEFAULT 0.0,
    PRIMARY KEY (account_id, fk_user_id),
    FOREIGN KEY (fk_user_id)
        REFERENCES user (user_id)
        ON DELETE NO ACTION ON UPDATE NO ACTION
);

CREATE TABLE connection (
    connection_id INT NOT NULL AUTO_INCREMENT,
    fk_initializer_id INT NOT NULL,
    fk_receiver_id INT NOT NULL,
    starting_date DATETIME NOT NULL,
    FOREIGN KEY (fk_initializer_id)
        REFERENCES user (user_id)
        ON DELETE NO ACTION ON UPDATE NO ACTION,
    FOREIGN KEY (fk_receiver_id)
        REFERENCES user (user_id)
        ON DELETE NO ACTION ON UPDATE NO ACTION,
    PRIMARY KEY (connection_id, fk_initializer_id, fk_receiver_id)
);

CREATE TABLE transaction (
    transaction_id INT NOT NULL AUTO_INCREMENT,
    fk_issuer_id INT NOT NULL,
    fk_payee_id INT NOT NULL,
    date DATETIME NOT NULL,
    amount DECIMAL(10, 2) NOT NULL,
    description VARCHAR(140),
    FOREIGN KEY (fk_issuer_id)
        REFERENCES user (user_id)
        ON DELETE NO ACTION ON UPDATE NO ACTION,
    FOREIGN KEY (fk_payee_id)
        REFERENCES user (user_id)
        ON DELETE NO ACTION ON UPDATE NO ACTION,
    PRIMARY KEY (transaction_id, fk_issuer_id, fk_payee_id)
);

INSERT INTO `user` (`email`, `password`, `firstname`, `lastname`, `balance`) VALUES
	('security@mail.com', '$2a$10$vpDkNfBtWg.ebbkL8VwaG.BrmlIlqRCd0RqoyOIb6hgRZRMfJ51xa', 'Security', 'User', 0.00),
	('hayley@mymail.com', '$2a$10$1NDocQWD9pl52dv/cY7mmOuCYbIVTzCd6ahb5EUDQxwkDMkg1Q54y', 'Hayley', 'James', 10.00),
	('clara@mail.com', '$2a$10$41nUyaddehEi9Slu/4kFWeedO3YrLnGCu5nZqYySX3CH7uyHMrclu', 'Clara', 'Tarazi', 133.56),
	('smith@mail.com', '$2a$10$3TU.lRztZJgEueboxsP2b.AV6TeBsKK.qyyCYGYJXKeozeahFVTuu', 'Smith', 'Sam', 8.00),
	('lambda@mail.com', '$2a$10$prOZuMO22K.itqO3CKrEGuVf2KUxdWOB9fGQh8DvWHPHWIiiR6iZy', 'Lambda', 'User', 96.91);

INSERT INTO `bank_account` (`fk_user_id`, `bank_name`, `iban`, `balance`) VALUES
    (5, 'Banque de France', 'FR7630001007941234567890185', 1590.00),
    (2, 'BNP Paribas', 'FR7630004000031234567890143', 352.68),
    (3, 'Crédit Agricole', 'FR7630006000011234567890189', 20.00),
    (4, 'Banque Populaire', 'FR7610107001011234567890129', 0.00);
	
INSERT INTO `connection` (`fk_initializer_id`, `fk_receiver_id`, `starting_date`) VALUES
	(1, 2, '2022-10-24 17:37:33'),
	(1, 3, '2022-10-24 17:37:41'),
	(3, 4, '2022-10-24 17:38:01'),
	(3, 5, '2022-10-24 17:38:08'),
	(5, 2, '2022-10-24 17:38:29'),
	(5, 4, '2022-10-24 17:38:39');

INSERT INTO `transaction` (`fk_issuer_id`, `fk_payee_id`, `date`, `amount`, `description`) VALUES
	(5, 4, '2022-10-24 17:39:55', 8.00, 'Movie tickets'),
	(3, 5, '2022-10-24 17:41:03', 25.00, 'Trip money'),
	(5, 2, '2022-10-24 17:41:40', 10.00, 'Restaurant bill share');
