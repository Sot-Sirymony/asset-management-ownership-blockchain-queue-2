package com.hrd.asset_holder_api.model.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class Asset {
    @JsonIgnore
    private String assetId;
    private String assetName;
    private String qty;
    private String unit;
    private String condition;
    private String attachment;
    @Min(value = 1)
    private Integer assignTo;
    @JsonIgnore
    private String username;
    @JsonIgnore
    private String depName = "default";
    @JsonIgnore
    private LocalDateTime createdAt;
}
