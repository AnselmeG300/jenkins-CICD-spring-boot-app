package com.paymybuddy.paymybuddy.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.DynamicUpdate;

import javax.persistence.*;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "\"user\"")
@DynamicUpdate
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class User {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "user_id")
	private Integer id;

	private String email;

	private String password;

	@Column(name = "firstname")
	private String firstName;

	@Column(name = "lastname")
	private String lastName;

	@Column(name = "balance")
	private BigDecimal balance;

	@OneToMany(mappedBy = "initializer")
	private List<Connection> initializedConnections = new ArrayList<>();

	@OneToMany(mappedBy = "receiver")
	private List<Connection> receivedConnections = new ArrayList<>();

	@OneToMany(mappedBy = "issuer")
	private List<Transaction> initiatedTransactions = new ArrayList<>();

	@OneToMany(mappedBy = "payee")
	private List<Transaction> receivedTransactions = new ArrayList<>();
}
