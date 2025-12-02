package app.web.dto;

import app.transactions.model.Category;
import app.transactions.model.Type;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Data
public class TransactionDto {

    @NotNull(message = "Amount is required")
    @DecimalMin(value = "0.01", message = "Amount must be greater than zero")
    private BigDecimal amount;

    @NotNull(message = "Date is required")
    private LocalDateTime date;

    @NotNull(message = "Type is required")
    private Type type;

    @NotNull(message = "Category is required")
    private Category category;

    @Size(max = 255, message = "Description must be under 255 characters")
    private String description;

}
