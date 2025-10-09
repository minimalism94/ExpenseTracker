package app.web.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class TransactionDto {

        @NotNull(message = "Amount is required")
        @DecimalMin(value = "0.01", message = "Amount must be greater than zero")
        private BigDecimal amount;

        @NotNull(message = "Date is required")
        private LocalDate date;

        @NotBlank(message = "Type is required")
        private String type;

        @NotBlank(message = "Category is required")
        private String categoryType;

        @Size(max = 255, message = "Description must be under 255 characters")
        private String description;

}
