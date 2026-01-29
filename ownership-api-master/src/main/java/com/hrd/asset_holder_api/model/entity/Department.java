package com.hrd.asset_holder_api.model.entity;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
public class Department {
    private Integer dep_id;
    private String dep_name;
    private String description;
    private LocalDateTime created_at;
    private LocalDateTime updated_at;
}
