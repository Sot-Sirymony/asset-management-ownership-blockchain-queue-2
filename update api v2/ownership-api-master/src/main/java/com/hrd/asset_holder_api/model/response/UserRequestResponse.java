package com.hrd.asset_holder_api.model.response;

import com.hrd.asset_holder_api.model.entity.Department;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserRequestResponse {
    private Integer userId;
    private String fullName;
    private DepartmentResponse department;
    private String profileImg;
    private String email;
    private String username;
}
