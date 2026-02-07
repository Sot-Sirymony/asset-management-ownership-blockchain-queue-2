package com.hrd.asset_holder_api.service;

import com.hrd.asset_holder_api.model.entity.Dashboard;
import com.hrd.asset_holder_api.model.entity.Department;
import com.hrd.asset_holder_api.model.request.DepartmentRequest;
import com.hrd.asset_holder_api.model.response.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface DepartmentService {


    List<Department> getAllDepartment(Integer page);

    Department getDepartmentById(Integer id);

    Boolean addDepartment(DepartmentRequest departmentRequest);

    Boolean updateDepartment(Integer id, DepartmentRequest departmentRequest);

    Boolean deleteDepartment(Integer id);

    Dashboard getDashboard();
}
