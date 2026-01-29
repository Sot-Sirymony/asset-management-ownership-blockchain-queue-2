package com.hrd.asset_holder_api.model.request;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.hrd.asset_holder_api.model.response.UserRequestResponse;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@AllArgsConstructor
@Data
@NoArgsConstructor
@Builder
public class AssetRequestRes {
    private String assetName;
    private Integer qty;
    private Integer unit;
    private String reason;
    @JsonIgnore
    private LocalDateTime createdAt;
    private String attachment;
}
