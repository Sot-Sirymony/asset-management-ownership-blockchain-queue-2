package com.hrd.asset_holder_api.controller;

import com.hrd.asset_holder_api.model.entity.AssetRequest;
import com.hrd.asset_holder_api.model.request.AssetRequestRes;
import com.hrd.asset_holder_api.model.response.ApiResponse;
import com.hrd.asset_holder_api.service.AssetRequestService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.sql.Timestamp;
import java.util.List;

@RestController
@RequestMapping("/api/v1")
@SecurityRequirement(name = "bearerAuth")
@CrossOrigin
@AllArgsConstructor
public class AssetReqeustController {

    private final AssetRequestService assetRequestService;

    @GetMapping("/admin/assetRequest")
    @Operation(summary = "Admin get all asset request from user")
    public List<AssetRequest> getAllUserAssetRequest() {
        return assetRequestService.getAllUserAssetRequest();
    }

    @GetMapping("/assetRequest/{id}")
    @Operation(summary = "Admin and user view asset request by id")
    public List<AssetRequest> getUserAssetRequest(@PathVariable Integer id) {
        return assetRequestService.getUserAssetRequestById(id);
    }


    @GetMapping("/user/assetRequest")
    @Operation(summary = "User get all asset reqeust")
    public ResponseEntity<ApiResponse<List<AssetRequest>>> getUserAssetRequest() {
        ApiResponse<List<AssetRequest>> res = ApiResponse.<List<AssetRequest>>builder()
                .message("User get all asset request successful")
                .payload(assetRequestService.getUserAssetRequest())
                .timestamp(new Timestamp(System.currentTimeMillis()))
                .httpStatus(HttpStatus.OK)
                .build();
        return ResponseEntity.ok(res);
    }


    @PostMapping("/user/createAssetRequest")
    @Operation(summary = "User create asset request")
    public ResponseEntity<ApiResponse<AssetRequestRes>> createUserAssetRequest(@RequestBody AssetRequestRes requestRes) {
        ApiResponse<AssetRequestRes> res = ApiResponse.<AssetRequestRes>builder()
                .message("Success")
                .payload(assetRequestService.createUserAssetRequest(requestRes))
                .timestamp(new Timestamp(System.currentTimeMillis()))
                .httpStatus(HttpStatus.OK)
                .build();
        return ResponseEntity.ok(res);
    }


    @PutMapping("/user/updateAssetRequest/{id}")
    @Operation(summary = "User update asset request")
    public ResponseEntity<ApiResponse<AssetRequestRes>> updateUserAssetRequest(@PathVariable Integer id,@RequestBody AssetRequestRes requestRes) {
        ApiResponse<AssetRequestRes> res = ApiResponse.<AssetRequestRes>builder()
                .message("Success")
                .payload(assetRequestService.updateUserAssetRequest(id,requestRes))
                .timestamp(new Timestamp(System.currentTimeMillis()))
                .httpStatus(HttpStatus.OK)
                .build();
        return ResponseEntity.ok(res);
    }


    @DeleteMapping("/user/deleteAssetRequest/{id}")
    @Operation(summary = "User delete asset request")
    public ResponseEntity<ApiResponse<Boolean>> deleteUserAsset(@PathVariable Integer id) {
        ApiResponse<Boolean> res = ApiResponse.<Boolean>builder()
                .message("Success")
                .payload(assetRequestService.deleteUserAsset(id))
                .timestamp(new Timestamp(System.currentTimeMillis()))
                .httpStatus(HttpStatus.OK)
                .build();
        return ResponseEntity.ok(res);
    }
}
