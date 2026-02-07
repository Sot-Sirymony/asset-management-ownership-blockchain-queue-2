package com.hrd.asset_holder_api.controller;


import com.fasterxml.jackson.databind.JsonNode;
import com.hrd.asset_holder_api.model.entity.ReportIssue;
import com.hrd.asset_holder_api.model.response.ApiResponse;
import com.hrd.asset_holder_api.service.ReportIssueService;
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
public class ReportIssueController {

    private final ReportIssueService reportIssueService;

    @PostMapping("/user/createIssue")
    public ResponseEntity<ApiResponse<Boolean>> createIssue(@RequestBody ReportIssue reportIssue) {
        ApiResponse<Boolean> res = ApiResponse.<Boolean>builder()
                .message("Success")
                .payload(reportIssueService.createIssue(reportIssue))
                .timestamp(new Timestamp(System.currentTimeMillis()))
                .httpStatus(HttpStatus.OK)
                .build();
        return ResponseEntity.ok(res);
    }

    @DeleteMapping("/user/deleteIssue/{id}")
    public ResponseEntity<ApiResponse<Boolean>> deleteIssue(@PathVariable String id) {
        ApiResponse<Boolean> res = ApiResponse.<Boolean>builder()
                .message("Success")
                .payload(reportIssueService.deleteIssue(id))
                .timestamp(new Timestamp(System.currentTimeMillis()))
                .httpStatus(HttpStatus.OK)
                .build();
        return ResponseEntity.ok(res);
    }

    @PutMapping("/user/updateIssue/{id}")
    public ResponseEntity<ApiResponse<Boolean>> updateIssue(@PathVariable String id, @RequestBody ReportIssue reportIssue) {
        ApiResponse<Boolean> res = ApiResponse.<Boolean>builder()
                .message("Success")
                .payload(reportIssueService.updateIssue(id,reportIssue))
                .timestamp(new Timestamp(System.currentTimeMillis()))
                .httpStatus(HttpStatus.OK)
                .build();
        return ResponseEntity.ok(res);
    }


    @GetMapping("/user/getIssueById/{id}")
    public ResponseEntity<ApiResponse<JsonNode>> getIssueById(@PathVariable String id) {
        ApiResponse<JsonNode> res = ApiResponse.<JsonNode>builder()
                .message("Success")
                .payload(reportIssueService.getIssueById(id))
                .timestamp(new Timestamp(System.currentTimeMillis()))
                .httpStatus(HttpStatus.OK)
                .build();
        return ResponseEntity.ok(res);
    }

    @GetMapping("/user/getAllIssue")
    public ResponseEntity<ApiResponse<JsonNode>> getAllIssue() {
        ApiResponse<JsonNode> res = ApiResponse.<JsonNode>builder()
                .message("Success")
                .payload(reportIssueService.getAllIssue())
                .timestamp(new Timestamp(System.currentTimeMillis()))
                .httpStatus(HttpStatus.OK)
                .build();
        return ResponseEntity.ok(res);
    }

}
