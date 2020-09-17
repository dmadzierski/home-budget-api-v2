package pl.wallet;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import pl.exception.ThereIsNoYourPropertyException;
import pl.wallet.transaction.model.Transaction;

@Service
@AllArgsConstructor
public class WalletProvider {

   private WalletRepository walletRepository;

   public Wallet save(Wallet wallet) {
      return walletRepository.save(wallet);
   }

   public Wallet isUserWallet(String email, Long walletId) {
      return walletRepository.findByIdAndUserEmail(walletId, email).orElseThrow(ThereIsNoYourPropertyException::new);
   }

   public Wallet addTransaction(Wallet wallet, Transaction transaction) {
      wallet.addTransaction(transaction);
      return save(wallet);
   }
}
