package com.safeexchange.service;

import com.safeexchange.dto.CreateEscrowRequest;
import com.safeexchange.dto.EscrowResponse;
import com.safeexchange.dto.SubmitAssetRequest;
import com.safeexchange.entity.*;
import com.safeexchange.exception.*;
import com.safeexchange.repository.EscrowRepository;
import com.safeexchange.repository.TransactionRepository;
import com.safeexchange.repository.UserRepository;
import com.safeexchange.repository.WalletRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class EscrowService {

    private final EscrowRepository escrowRepository;
    private final UserRepository userRepository;
    private final WalletRepository walletRepository;
    private final TransactionRepository transactionRepository;

    @Transactional
    public EscrowResponse createEscrow(String sellerUsername, CreateEscrowRequest request) {
        User seller = getUserByUsername(sellerUsername);

        Escrow escrow = Escrow.builder()
                .seller(seller)
                .assetName(request.assetName())
                .assetType(request.assetType())
                .assetDescription(request.assetDescription())
                .amount(request.amount())
                .status(EscrowStatus.LISTED)
                .build();

        Escrow saved = escrowRepository.save(escrow);
        return toResponse(saved);
    }

    public List<EscrowResponse> getAvailableListings() {
        return escrowRepository.findByStatus(EscrowStatus.LISTED)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    public EscrowResponse getEscrow(Long escrowId) {
        return toResponse(getEscrowById(escrowId));
    }

    @Transactional
    public EscrowResponse claimEscrow(Long escrowId, String buyerUsername) {
        Escrow escrow = getEscrowById(escrowId);

        if (escrow.getStatus() != EscrowStatus.LISTED) {
            throw new InvalidEscrowStateException("Escrow is not available for claiming");
        }

        User buyer = getUserByUsername(buyerUsername);

        if (buyer.getUsername().equals(escrow.getSeller().getUsername())) {
            throw new UnauthorizedActionException("Seller cannot buy their own listing");
        }

        escrow.setBuyer(buyer);
        escrow.setStatus(EscrowStatus.CREATED);
        return toResponse(escrowRepository.save(escrow));
    }

    @Transactional
    public EscrowResponse lockFunds(Long escrowId, String buyerUsername) {
        Escrow escrow = getEscrowById(escrowId);
        validateCaller(escrow.getBuyer().getUsername(), buyerUsername, "buyer");

        if (escrow.getStatus() != EscrowStatus.CREATED) {
            throw new InvalidEscrowStateException("Escrow must be in CREATED state to lock funds");
        }

        Wallet buyerWallet = walletRepository.findByUserId(escrow.getBuyer().getId())
                .orElseThrow(() -> new UserNotFoundException("Buyer wallet not found"));

        if (buyerWallet.getBalance().compareTo(escrow.getAmount()) < 0) {
            throw new InsufficientBalanceException("Insufficient wallet balance to fund this escrow");
        }

        buyerWallet.setBalance(buyerWallet.getBalance().subtract(escrow.getAmount()));
        walletRepository.save(buyerWallet);

        transactionRepository.save(Transaction.builder()
                .wallet(buyerWallet)
                .amount(escrow.getAmount())
                .type(TransactionType.ESCROW_LOCK)
                .description("Funds locked for escrow #" + escrow.getId())
                .build());

        escrow.setStatus(EscrowStatus.FUNDED);
        return toResponse(escrowRepository.save(escrow));
    }

    @Transactional
    public EscrowResponse submitAsset(Long escrowId, String sellerUsername, SubmitAssetRequest request) {
        Escrow escrow = getEscrowById(escrowId);
        validateCaller(escrow.getSeller().getUsername(), sellerUsername, "seller");

        if (escrow.getStatus() != EscrowStatus.FUNDED) {
            throw new InvalidEscrowStateException("Escrow must be FUNDED before asset submission");
        }

        escrow.setSubmissionLink(request.submissionLink());
        escrow.setStatus(EscrowStatus.ASSET_SUBMITTED);
        return toResponse(escrowRepository.save(escrow));
    }

    @Transactional
    public EscrowResponse confirmDelivery(Long escrowId, String buyerUsername) {
        Escrow escrow = getEscrowById(escrowId);
        validateCaller(escrow.getBuyer().getUsername(), buyerUsername, "buyer");

        if (escrow.getStatus() != EscrowStatus.ASSET_SUBMITTED) {
            throw new InvalidEscrowStateException("Asset must be submitted before confirming delivery");
        }

        Wallet sellerWallet = walletRepository.findByUserId(escrow.getSeller().getId())
                .orElseThrow(() -> new UserNotFoundException("Seller wallet not found"));

        sellerWallet.setBalance(sellerWallet.getBalance().add(escrow.getAmount()));
        walletRepository.save(sellerWallet);

        transactionRepository.save(Transaction.builder()
                .wallet(sellerWallet)
                .amount(escrow.getAmount())
                .type(TransactionType.ESCROW_RELEASE)
                .description("Funds released for escrow #" + escrow.getId())
                .build());

        escrow.setStatus(EscrowStatus.COMPLETED);
        return toResponse(escrowRepository.save(escrow));
    }

    private void validateCaller(String expectedUsername, String actualUsername, String role) {
        if (!expectedUsername.equals(actualUsername)) {
            throw new UnauthorizedActionException("Only the " + role + " can perform this action");
        }
    }

    private User getUserByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new UserNotFoundException("User not found: " + username));
    }

    private Escrow getEscrowById(Long id) {
        return escrowRepository.findById(id)
                .orElseThrow(() -> new EscrowNotFoundException("Escrow not found: " + id));
    }

    private EscrowResponse toResponse(Escrow e) {
        String buyerUsername = e.getBuyer() != null ? e.getBuyer().getUsername() : null;
        return new EscrowResponse(e.getId(), buyerUsername, e.getSeller().getUsername(),
                e.getAssetName(), e.getAssetType(), e.getAssetDescription(), e.getSubmissionLink(),
                e.getAmount(), e.getStatus(), e.getCreatedAt(), e.getUpdatedAt());
    }
}