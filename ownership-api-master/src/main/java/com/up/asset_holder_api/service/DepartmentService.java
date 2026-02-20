package com.up.asset_holder_api.service;

import com.up.asset_holder_api.model.entity.Dashboard;
import com.up.asset_holder_api.model.entity.Department;
import com.up.asset_holder_api.model.request.DepartmentRequest;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface DepartmentService {


    List<Department> getAllDepartment(Integer page, Integer size);

    Department getDepartmentById(Integer id);

    Boolean addDepartment(DepartmentRequest departmentRequest);

    Boolean updateDepartment(Integer id, DepartmentRequest departmentRequest);

    Boolean deleteDepartment(Integer id);

    Dashboard getDashboard();
}
