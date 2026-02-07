package com.hrd.asset_holder_api.controller;

import com.hrd.asset_holder_api.model.entity.Dashboard;
import com.hrd.asset_holder_api.model.entity.Department;
import com.hrd.asset_holder_api.model.request.DepartmentRequest;
import com.hrd.asset_holder_api.model.response.ApiResponse;
import com.hrd.asset_holder_api.service.DepartmentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import org.apache.ibatis.annotations.Update;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.sql.Timestamp;
import java.util.List;

@RestController
@RequestMapping("/api/v1")
@SecurityRequirement(name = "bearerAuth")
@CrossOrigin(origins = "http://localhost:3000")
@AllArgsConstructor
public class DepartmentController {

    private final DepartmentService departmentService;

    @GetMapping("/admin/department")
    @Operation(summary = "Admin view list of department")
    public ResponseEntity<ApiResponse<List<Department>>> getAllDepartment(
//            @RequestParam(defaultValue = "10") @Positive Integer size ,
            @RequestParam(defaultValue = "1") @Positive Integer page
    ) {
        ApiResponse<List<Department>> apiResponse = ApiResponse.<List<Department>>builder()
                .message("Get all department successfully")
                .payload(departmentService.getAllDepartment(page))
                .httpStatus(HttpStatus.OK)
                .timestamp(new Timestamp(System.currentTimeMillis()))
                .build();
        return ResponseEntity.ok(apiResponse);
    }

    @GetMapping("/admin/department/{id}")
    public Department getDepartmentById(@PathVariable Integer id) {
        return departmentService.getDepartmentById(id);
    }

    @PostMapping("/admin/department")
    @Operation(summary = "Admin create new department")
    public ResponseEntity<ApiResponse<Boolean>> addDepartment(@RequestBody DepartmentRequest departmentRequest) {
        ApiResponse<Boolean> apiResponse = ApiResponse.<Boolean>builder()
                .message("Create department successfully")
                .payload( departmentService.addDepartment(departmentRequest))
                .httpStatus(HttpStatus.OK)
                .timestamp(new Timestamp(System.currentTimeMillis()))
                .build();
        return ResponseEntity.ok(apiResponse);
    }

    @PutMapping("/admin/department/{id}")
    @Operation(summary = "Admin update department")
    public ResponseEntity<ApiResponse<Boolean>> updateDepartment(@PathVariable @Positive Integer id, @RequestBody DepartmentRequest departmentRequest) {
        ApiResponse<Boolean> apiResponse = ApiResponse.<Boolean>builder()
                .message("Update department successfully")
                .payload(departmentService.updateDepartment(id, departmentRequest))
                .httpStatus(HttpStatus.OK)
                .timestamp(new Timestamp(System.currentTimeMillis()))
                .build();
        return ResponseEntity.ok(apiResponse);
    }

    @DeleteMapping("/admin/department/{id}")
    @Operation(summary = "Admin delete department")
    public ResponseEntity<ApiResponse<Boolean>> deleteDepartment(@PathVariable Integer id) {
        ApiResponse<Boolean> apiResponse = ApiResponse.<Boolean>builder()
                .message("Delete department successfully")
                .payload(departmentService.deleteDepartment(id))
                .httpStatus(HttpStatus.OK)
                .timestamp(new Timestamp(System.currentTimeMillis()))
                .build();
        return ResponseEntity.ok(apiResponse);
    }


    @GetMapping("/admin/dashboard")
    @Operation(summary = "Dashboard main page")
    public ResponseEntity<ApiResponse<Dashboard>> getDashboard() {
        ApiResponse<Dashboard> apiResponse = ApiResponse.<Dashboard>builder()
                .message("Delete department successfully")
                .payload(departmentService.getDashboard())
                .httpStatus(HttpStatus.OK)
                .timestamp(new Timestamp(System.currentTimeMillis()))
                .build();
        return ResponseEntity.ok(apiResponse);
    }

}
