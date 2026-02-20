package com.up.asset_holder_api.model.response;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserResponse {

    private String userId;
    private String fullName;
    private String gender;
    private Date dob;
    private String phoneNumber;

    @NotNull(message = "Department cannot be null")
    @Min(value = 1, message = "Department must be a positive number")
    private DepartmentResponse department;

    @Pattern(regexp = "^[\\w-\\.]+@([\\w-]+\\.)+[\\w-]{2,4}$", message = "Invalid email format")
    @Email(message = "Email should be valid")
    @Schema(example = "john.doe@example.com", description = "Email address of the user")
    @Size(max = 50, message = "Email must be less than or equal to 255 characters")
    private String email;

    private String profileImg;
    private String placeOfBirth;
    private String address;
    private String description;
    //field description

}
