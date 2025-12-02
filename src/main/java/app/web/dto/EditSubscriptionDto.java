package app.web.dto;

import app.subscription.model.SubscriptionPeriod;
import app.subscription.model.SubscriptionType;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class EditSubscriptionDto {
    private UUID id;
    @NotBlank
    private String name;

    @NotNull
    private SubscriptionPeriod period;

    @NotNull
    private LocalDate expiryOn;

    @NotNull
    private SubscriptionType type;

    @NotNull
    @DecimalMin("0.01")
    private BigDecimal price;
}
