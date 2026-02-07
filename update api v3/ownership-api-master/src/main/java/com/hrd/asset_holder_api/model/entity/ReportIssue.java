package com.hrd.asset_holder_api.model.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.v3.oas.annotations.media.Schema;
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
public class ReportIssue {
    @JsonIgnore
    private String reportId;
    @Schema(example = "asset1")
    @JsonIgnore
    private String assetId;
    private String assetName;
    private String problem;
    private String attachment;
    @JsonIgnore
    private String userId;
    @JsonIgnore
    private String userName;
    @JsonIgnore
    private LocalDateTime createAt;
}
