package com.up.asset_holder_api.model.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Asset entity representing a blockchain asset.
 * Contains validation annotations for input validation.
 */
@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
@Schema(description = "Asset entity for blockchain asset management")
public class Asset {
    @JsonIgnore
    private String assetId;
    
    @NotBlank(message = "Asset name is required")
    @Size(max = 255, message = "Asset name must not exceed 255 characters")
    @Schema(description = "Name of the asset", example = "Laptop Dell XPS 15", required = true)
    private String assetName;
    
    @NotBlank(message = "Quantity is required")
    @Schema(description = "Quantity of the asset", example = "1", required = true)
    private String qty;
    
    @Size(max = 50, message = "Unit must not exceed 50 characters")
    @Schema(description = "Unit of measurement", example = "piece")
    private String unit;
    
    @Size(max = 100, message = "Condition must not exceed 100 characters")
    @Schema(description = "Condition of the asset", example = "New")
    private String condition;
    
    @Schema(description = "Attachment URL or path")
    private String attachment;
    
    @NotNull(message = "Assign to user ID is required")
    @Min(value = 1, message = "User ID must be a positive number")
    @Schema(description = "User ID to assign the asset to", example = "1", required = true)
    private Integer assignTo;
    
    @JsonIgnore
    private String username;
    
    @JsonIgnore
    private String depName = "default";
    
    @JsonIgnore
    private LocalDateTime createdAt;
}
