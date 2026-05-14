package com.steve.account_service.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import lombok.*;
import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateAccountRequest {

    @Schema(
            description = "Initial deposit amount. Defaults to 0.00 if not provided.",
            example = "5000.00"
    )
    @DecimalMin(value = "0.0", inclusive = true, message = "Initial balance cannot be negative")
    private BigDecimal initialBalance;

    @Schema(
            description = "Account number. Leave blank to auto-generate.",
            example = ""
    )
    private String accountNumber;

    @NotBlank(message = "Account type is required")
    @Schema(
            description = "Type of account",
            example = "SAVINGS",
            allowableValues = {"SAVINGS", "CHECKING"}
    )
    private String accountType;
}