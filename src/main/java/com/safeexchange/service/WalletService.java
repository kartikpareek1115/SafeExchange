package com.safeexchange.service;

import com.safeexchange.dto.DepositRequest;
import com.safeexchange.dto.WalletResponse;
import com.safeexchange.entity.Transaction;
import com.safeexchange.entity.TransactionType;
import com.safeexchange.entity.User;
import com.safeexchange.entity.Wallet;
import com.safeexchange.exception.UserNotFoundException;
import com.safeexchange.repository.TransactionRepository;
import com.safeexchange.repository.UserRepository;
import com.safeexchange.repository.WalletRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class WalletService {

    private final WalletRepository walletRepository;
    private final UserRepository userRepository;
    private final TransactionRepository transactionRepository;

    public WalletResponse getBalance(String username) {
        Wallet wallet = getWalletByUsername(username);
        return new WalletResponse(wallet.getId(), wallet.getBalance());
    }

    @Transactional
    public WalletResponse deposit(String username, DepositRequest request) {
        Wallet wallet = getWalletByUsername(username);

        wallet.setBalance(wallet.getBalance().add(request.amount()));
        walletRepository.save(wallet);

        Transaction transaction = Transaction.builder()
                .wallet(wallet)
                .amount(request.amount())
                .type(TransactionType.DEPOSIT)
                .description("Wallet deposit")
                .build();
        transactionRepository.save(transaction);

        return new WalletResponse(wallet.getId(), wallet.getBalance());
    }

    private Wallet getWalletByUsername(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UserNotFoundException("User not found: " + username));
        return walletRepository.findByUserId(user.getId())
                .orElseThrow(() -> new UserNotFoundException("Wallet not found for user: " + username));
    }
}