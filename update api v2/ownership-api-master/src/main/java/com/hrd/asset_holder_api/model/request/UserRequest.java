package com.hrd.asset_holder_api.model.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;
import java.util.Date;

@AllArgsConstructor
@Data
@NoArgsConstructor
@Builder
public class UserRequest {
//    private String user_id;
    private String full_name;
    private String gender;
    private String phone_number;
    private String address;
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private Date dob;
    private String place_of_birth;
    private String description;
    private String profile_img;

}
