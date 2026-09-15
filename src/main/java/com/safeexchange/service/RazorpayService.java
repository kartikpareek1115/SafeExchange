package com.safeexchange.service;

import com.razorpay.Order;
import com.razorpay.RazorpayClient;
import com.razorpay.Utils;
import com.safeexchange.dto.RazorpayOrderResponse;
import com.safeexchange.dto.VerifyPaymentRequest;
import com.safeexchange.entity.Transaction;
import com.safeexchange.entity.TransactionType;
import com.safeexchange.entity.User;
import com.safeexchange.entity.Wallet;
import com.safeexchange.exception.UserNotFoundException;
import com.safeexchange.repository.TransactionRepository;
import com.safeexchange.repository.UserRepository;
import com.safeexchange.repository.WalletRepository;
import lombok.RequiredArgsConstructor;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class RazorpayService {

    private final RazorpayClient razorpayClient;
    private final UserRepository userRepository;
    private final WalletRepository walletRepository;
    private final TransactionRepository transactionRepository;

    @Value("${razorpay.key.id}")
    private String razorpayKeyId;

    @Value("${razorpay.key.secret}")
    private String razorpayKeySecret;

    public RazorpayOrderResponse createOrder(String username, BigDecimal amount) {

        User user = getUser(username);

        // Razorpay expects amount in paise.
        long amountInPaise = amount
                .multiply(BigDecimal.valueOf(100))
                .longValueExact();

        try {

            JSONObject options = new JSONObject();

            options.put("amount", amountInPaise);
            options.put("currency", "INR");
            options.put(
                    "receipt",
                    "wallet_" + user.getId() + "_" + System.currentTimeMillis()
            );

            Order order = razorpayClient.orders.create(options);

            return new RazorpayOrderResponse(
                    order.get("id"),
                    razorpayKeyId,
                    amountInPaise,
                    "INR"
            );

        } catch (Exception e) {
            throw new RuntimeException("Failed to create Razorpay order", e);
        }
    }

    @Transactional
    public void verifyAndCreditWallet(
            String username,
            VerifyPaymentRequest request
    ) {

        try {

            /*
             * IMPORTANT:
             * The signature is generated from:
             *
             * razorpay_order_id + "|" + razorpay_payment_id
             *
             * using the Razorpay secret.
             */

            JSONObject attributes = new JSONObject();

            attributes.put(
                    "razorpay_order_id",
                    request.razorpayOrderId()
            );

            attributes.put(
                    "razorpay_payment_id",
                    request.razorpayPaymentId()
            );

            attributes.put(
                    "razorpay_signature",
                    request.razorpaySignature()
            );

            boolean valid = Utils.verifyPaymentSignature(
                    attributes,
                    razorpayKeySecret
            );

            if (!valid) {
                throw new IllegalArgumentException(
                        "Invalid Razorpay payment signature"
                );
            }

            /*
             * Signature is valid.
             * Now fetch the Razorpay order from Razorpay itself.
             *
             * NEVER trust the amount sent by the frontend.
             */

            Order order = razorpayClient.orders.fetch(
                    request.razorpayOrderId()
            );

            long amountInPaise = ((Number) order.get("amount")).longValue();

            BigDecimal amount = BigDecimal.valueOf(amountInPaise)
                    .divide(BigDecimal.valueOf(100));

            User user = getUser(username);

            Wallet wallet = walletRepository
                    .findByUserId(user.getId())
                    .orElseThrow(() ->
                            new UserNotFoundException(
                                    "Wallet not found"
                            )
                    );

            /*
             * Credit wallet ONLY AFTER signature verification.
             */

            wallet.setBalance(
                    wallet.getBalance().add(amount)
            );

            walletRepository.save(wallet);

            Transaction transaction = Transaction.builder()
                    .wallet(wallet)
                    .amount(amount)
                    .type(TransactionType.DEPOSIT)
                    .description(
                            "Razorpay wallet deposit - "
                                    + request.razorpayPaymentId()
                    )
                    .build();

            transactionRepository.save(transaction);

        } catch (Exception e) {

            if (e instanceof IllegalArgumentException) {
                throw (IllegalArgumentException) e;
            }

            throw new RuntimeException(
                    "Razorpay payment verification failed",
                    e
            );
        }
    }

    private User getUser(String username) {

        return userRepository.findByUsername(username)
                .orElseThrow(() ->
                        new UserNotFoundException(
                                "User not found: " + username
                        )
                );
    }
}