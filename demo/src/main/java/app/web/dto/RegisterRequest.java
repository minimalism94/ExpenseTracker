package app.web.dto;

import app.user.model.Country;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Data
public class RegisterRequest {
    @Size(min = 6, message = "Username must be at least 6 symbols")
    @NotBlank
    private String username;
    @NotBlank
    @NotNull(message = "Email is required")
    private String email;
    @NotBlank
    @Size(min = 6, message = "Password must be at least 6 symbols")
    private String password;

    @NotNull(message = "Country is required")
    private Country country;
}
