package com.hrd.asset_holder_api.model.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@Data
@NoArgsConstructor
@Builder
public class AssetTrasferRequest {
    @Schema(example = "1")
    private Integer newAssignTo;
}
