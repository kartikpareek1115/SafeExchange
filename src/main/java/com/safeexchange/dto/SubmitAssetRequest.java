package com.safeexchange.dto;

import jakarta.validation.constraints.NotBlank;

public record SubmitAssetRequest(

        @NotBlank(message = "Submission link is required")
        String submissionLink
) {}