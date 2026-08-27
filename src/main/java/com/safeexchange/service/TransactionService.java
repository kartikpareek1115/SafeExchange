package com.safeexchange.service;

import com.safeexchange.dto.TransactionResponse;
import com.safeexchange.entity.User;
import com.safeexchange.entity.Wallet;
import com.safeexchange.exception.UserNotFoundException;
import com.safeexchange.repository.TransactionRepository;
import com.safeexchange.repository.UserRepository;
import com.safeexchange.repository.WalletRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final WalletRepository walletRepository;
    private final UserRepository userRepository;

    public List<TransactionResponse> getHistory(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UserNotFoundException("User not found: " + username));
        Wallet wallet = walletRepository.findByUserId(user.getId())
                .orElseThrow(() -> new UserNotFoundException("Wallet not found"));

        return transactionRepository.findByWalletIdOrderByCreatedAtDesc(wallet.getId())
                .stream()
                .map(t -> new TransactionResponse(t.getId(), t.getAmount(), t.getType(),
                        t.getDescription(), t.getCreatedAt()))
                .toList();
    }
}