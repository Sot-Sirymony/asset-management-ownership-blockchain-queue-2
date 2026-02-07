package com.hrd.asset_holder_api.model.request;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@Data
@Builder
@NoArgsConstructor
public class UserRegister {
    private String fullName;
    private String username;

    @NotNull
    @NotBlank
    @Pattern(regexp = "^(?=.*[A-Za-z])(?=.*\\d)[A-Za-z\\d@$!%*#?&]{8,}$",
            message = "Password must be at least 8 characters long and include both letters and numbers"
    )
    @Schema(example = "*****")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String password;

    @NotNull(message = "Department cannot be null")
    @Min(value = 1, message = "Department must be a positive number")
    private Integer department;

    @Pattern(regexp = "^[\\w-\\.]+@([\\w-]+\\.)+[\\w-]{2,4}$", message = "Invalid email format")
    @Email(message = "Email should be valid")
    @Schema(example = "john.doe@example.com", description = "Email address of the user")
    @Size(max = 50, message = "Email must be less than or equal to 255 characters")
    private String email;
}
