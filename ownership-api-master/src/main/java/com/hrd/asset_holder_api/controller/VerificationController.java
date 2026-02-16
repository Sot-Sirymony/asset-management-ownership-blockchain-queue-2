package com.hrd.asset_holder_api.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.hrd.asset_holder_api.model.response.ApiResponse;
import com.hrd.asset_holder_api.service.VerificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.sql.Timestamp;

/**
 * Controller for asset verification endpoints.
 * Supports internal and external verification workflows as per BRD.
 */
@RestController
@RequestMapping("/api/v1")
@SecurityRequirement(name = "bearerAuth")
@CrossOrigin
@AllArgsConstructor
public class VerificationController {

    private final VerificationService verificationService;

    @GetMapping("/user/verifyAsset/{id}")
    @Operation(summary = "Internal verification - Verify asset authenticity and ownership")
    public ResponseEntity<ApiResponse<JsonNode>> verifyAssetInternal(@PathVariable String id) {
        ApiResponse<JsonNode> res = ApiResponse.<JsonNode>builder()
                .message("Asset verified successfully")
                .payload(verificationService.verifyAssetInternal(id))
                .timestamp(new Timestamp(System.currentTimeMillis()))
                .httpStatus(HttpStatus.OK)
                .build();
        return ResponseEntity.ok(res);
    }

    @GetMapping("/user/verifyAssetExternal/{id}")
    @Operation(summary = "External verification - Verify asset for audit purposes")
    public ResponseEntity<ApiResponse<JsonNode>> verifyAssetExternal(@PathVariable String id) {
        ApiResponse<JsonNode> res = ApiResponse.<JsonNode>builder()
                .message("External verification completed")
                .payload(verificationService.verifyAssetExternal(id))
                .timestamp(new Timestamp(System.currentTimeMillis()))
                .httpStatus(HttpStatus.OK)
                .build();
        return ResponseEntity.ok(res);
    }

    @GetMapping("/user/verificationTrail/{id}")
    @Operation(summary = "Get complete verification trail for an asset")
    public ResponseEntity<ApiResponse<JsonNode>> getVerificationTrail(@PathVariable String id) {
        ApiResponse<JsonNode> res = ApiResponse.<JsonNode>builder()
                .message("Verification trail retrieved successfully")
                .payload(verificationService.getVerificationTrail(id))
                .timestamp(new Timestamp(System.currentTimeMillis()))
                .httpStatus(HttpStatus.OK)
                .build();
        return ResponseEntity.ok(res);
    }
}
