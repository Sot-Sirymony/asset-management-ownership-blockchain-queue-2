package com.hrd.asset_holder_api.model.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@Data
@NoArgsConstructor
@Builder
public class UserPassword {
    private String oldPassword;
    private String newPassword;
    private String confirmPassword;
}
