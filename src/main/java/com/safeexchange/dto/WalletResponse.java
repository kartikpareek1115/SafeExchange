package com.safeexchange.dto;

import java.math.BigDecimal;

public record WalletResponse(
        Long id,
        BigDecimal balance
) {}