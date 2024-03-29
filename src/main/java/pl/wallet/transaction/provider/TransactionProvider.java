package pl.wallet.transaction.provider;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import pl.wallet.transaction.repository.TransactionRepository;

@Service
@AllArgsConstructor
public class TransactionProvider {

    private final TransactionRepository transactionRepository;

    public void removeWalletTransactions(Long walletId) {
        transactionRepository.getTransactionsByWalletId(walletId).forEach(transaction -> this.remove(transaction.getId()));
    }

    public void remove(Long transactionId) {
        transactionRepository.deleteById(transactionId);
    }
}
