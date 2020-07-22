package pl.wallet;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Controller;
import pl.exception.CanNotRemoveOwnerFromWalletExcecption;
import pl.exception.SavedEntityCanNotHaveIdException;
import pl.user.User;
import pl.user.UserService;
import pl.user.friend_ship.FriendShipService;
import pl.wallet.transaction.TransactionService;

import java.security.Principal;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Controller
@AllArgsConstructor
public class WalletController {
    private WalletService walletService;
    private UserService userService;
    private TransactionService transactionService;
    private FriendShipService friendShipService;

    WalletDto addWallet(Principal principal, WalletDto walletDto) {
        if (walletDto.getId() != null) throw new SavedEntityCanNotHaveIdException();
        User user = userService.getUser(principal);
        Wallet wallet = WalletMapper.toEntity(walletDto);
        wallet.setUsers(Collections.singleton(user));
        wallet.setOwner(user);
        wallet = walletService.save(wallet);
        return WalletMapper.toDto(wallet);
    }

    List<WalletDto> getWallets(Principal principal) {
        User user = userService.getUser(principal);
        return walletService.getWalletsByUser(user).stream().map(WalletMapper::toDto).collect(Collectors.toList());
    }

    private void isUserWallet(Principal principal, Long walletId) {
        User user = userService.getUser(principal);
        walletService.isUserWallet(user, walletId);
    }

    void removeWallet(Principal principal, Long walletId) {
        isUserWallet(principal, walletId);
        transactionService.removeWalletTransactions(walletId);
        walletService.removeWallet(walletId);
    }

    WalletDto getWallet(Principal principal, Long walletId) {
        User user = this.userService.getUser(principal);
        Wallet wallet = walletService.getWalletByUserAndId(user, walletId);
        return WalletMapper.toDto(wallet);
    }

    WalletDto editWallet(Principal principal, WalletDto walletDto) {
        User user = this.userService.getUser(principal);
        Wallet wallet = walletService.getWalletByUserAndId(user, walletDto.getId());
        wallet.setName(walletDto.getName());
        Wallet updateWallet = walletService.save(wallet);
        return WalletMapper.toDto(updateWallet);
    }

    WalletDto shareWalletWithFriend(Principal principal, Long walletId, String friendUserEmail) {
        User owner = userService.getUser(principal);
        User friend = userService.getUser(friendUserEmail::toString);
        friendShipService.isFriends(owner, friend);
        Wallet wallet = walletService.getWalletByOwnerAndId(owner, walletId);
        wallet.addUser(friend);
        Wallet savedWallet = walletService.save(wallet);
        return WalletMapper.toDto(savedWallet);
    }

    WalletDto changeWalletOwner(Principal principal, Long walletId, String newOwnerUserEmail) {
        User owner = userService.getUser(principal);
        User newOwner = userService.getUser(newOwnerUserEmail::toString);
        Wallet wallet = walletService.getWalletByOwnerAndId(owner, walletId);
        wallet.setOwner(newOwner);
        Wallet savedWallet = walletService.save(wallet);
        return WalletMapper.toDto(savedWallet);
    }

    WalletDto removeUserFromWallet(Principal principal, Long walletId, String friendToRemoveFromWalletUserEmail) {
        User owner = userService.getUser(principal);
        User friendToRemoveFromWallet = userService.getUser(friendToRemoveFromWalletUserEmail::toString);
        if (owner.equals(friendToRemoveFromWallet)) throw new CanNotRemoveOwnerFromWalletExcecption();
        Wallet wallet = walletService.getWalletByOwnerAndId(owner, walletId);
        wallet.removeUser(friendToRemoveFromWallet);
        Wallet savedWallet = walletService.save(wallet);
        return WalletMapper.toDto(savedWallet);
    }
}
