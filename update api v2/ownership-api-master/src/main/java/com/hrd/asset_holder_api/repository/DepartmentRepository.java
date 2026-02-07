package com.hrd.asset_holder_api.repository;

import com.hrd.asset_holder_api.model.entity.Department;
import com.hrd.asset_holder_api.model.request.DepartmentRequest;
import com.hrd.asset_holder_api.model.response.ApiResponse;
import com.hrd.asset_holder_api.model.response.DepartmentResponse;
import org.apache.ibatis.annotations.*;
import org.springframework.http.ResponseEntity;

import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface DepartmentRepository {

    @Select("SELECT * FROM department " +
            "LIMIT 10 OFFSET 10 * (#{page}-1)")
    List<Department> findAllDepartment(Integer page);


    //-------------------Find department By id-----------------
    @Select("SELECT * FROM department WHERE dep_id = #{departmentId}")
    Department findDepartmentById(Integer departmentId);

    @Select("SELECT * FROM department WHERE dep_id = #{departmentId}")
    DepartmentResponse findDepartmentResponseById(Integer departmentId);


    //-------------------Create new Department-----------------------
    @Insert("INSERT INTO department(dep_name, description, created_at) " +
            "VALUES (#{rq.dep_name}, #{rq.description}, #{createAt})")
    Integer postDepartment(@Param("rq") DepartmentRequest departmentRequest, LocalDateTime createAt);

    //-----------------Update department-----------------------
    @Update("UPDATE department SET dep_name = #{rq.dep_name}, description = #{rq.description}, updated_at = #{updateAt} " +
            "WHERE dep_id = #{id}")
    Boolean updateDepartment(Integer id, @Param("rq") DepartmentRequest departmentRequest, LocalDateTime updateAt);

    //---------------------Delete Department-----------------
    @Delete("DELETE FROM department WHERE dep_id = #{id}")
    Boolean deleteDepartment(Integer id);



    //------------------Dashboard--------------------------
    @Select("select count(user_id) from users;")
    Integer findTotalUser();
    @Select("select count(request_id) from asset_request;")
    Integer findTotalAssetRequest();
    @Select("select count(dep_id) from department;")
    Integer findTotalDepartment();
}
