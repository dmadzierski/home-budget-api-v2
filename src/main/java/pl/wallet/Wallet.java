package pl.wallet;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.*;
import org.hibernate.annotations.Cascade;
import pl.user.User;
import pl.wallet.currency.Currency;
import pl.wallet.transaction.model.Transaction;
import pl.wallet.transaction.model.TransactionRecurring;

import javax.persistence.*;
import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

import static org.hibernate.annotations.CascadeType.DELETE;

@EqualsAndHashCode(exclude = {"owner", "users"})
@NoArgsConstructor
@Getter
@Setter
@Entity
@Table(name = "wallet")
public class Wallet {


    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "wallet_id")
    @Id
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private BigDecimal balance;

    @OneToMany(mappedBy = "wallet")
    private List<Transaction> transactions;

    @ManyToMany(mappedBy = "wallets")
    @JsonIgnoreProperties
    private Set<User> users;

    @OneToOne
    private User owner;

    @OneToMany(mappedBy = "wallet")
    private List<TransactionRecurring> transactionsRecurring;

    @OneToOne
    private Currency currency;

    @Builder
    public Wallet(Long id, String name, BigDecimal balance, List<Transaction> transactions, Set<User> users, User owner, List<TransactionRecurring> transactionsRecurring, Currency currency) {
        this.id = id;
        this.name = name;
        this.balance = balance;
        this.transactions = transactions;
        this.users = users;
        this.owner = owner;
        this.transactionsRecurring = transactionsRecurring;
        this.currency = currency;
    }

    public void addTransaction(Transaction transaction) {
        this.balance = transaction.getCategory().getType().countBalance(this, transaction);
    }

    public void removeTransaction(Transaction transaction) {
        this.balance = transaction.getCategory().getType().undoCountBalance(this, transaction);
    }

    public void addUser(User user) {
        if (users == null)
            users = new HashSet<>(Collections.singletonList(user));
        users.add(user);
    }

    public void removeUser(User user) {
        this.users = this.users.stream().filter(u -> !u.equals(user)).collect(Collectors.toSet());
    }

    public void addTransactionRecurring(TransactionRecurring transactionRecurring) {
        if (transactionsRecurring == null)
            transactionsRecurring = new ArrayList<>();
        this.transactionsRecurring.add(transactionRecurring);
    }

    public void removeTransactionRecurring(TransactionRecurring transactionRecurring) {
        this.transactionsRecurring = transactionsRecurring.stream().filter(t -> t.equals(transactionRecurring)).collect(Collectors.toList());
    }
}
