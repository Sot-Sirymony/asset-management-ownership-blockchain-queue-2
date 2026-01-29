package com.hrd.asset_holder_api.repository;

import com.hrd.asset_holder_api.model.entity.User;
import com.hrd.asset_holder_api.model.response.UserRequestResponse;
import com.hrd.asset_holder_api.model.response.UserResponse;
import org.apache.ibatis.annotations.*;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Repository;

import java.util.List;

@Mapper
public interface UserRepository {

    @Select("SELECT user_id, username, password, role FROM users WHERE username = #{username}")
    @Result(property = "userId", column = "user_id")
    @Result(property = "roles", column = "role")
    User findUserByUsername(@Param("username") String username);

    @Select("SELECT role FROM users WHERE username = #{username}")
    String findUserRolesByUsername(@Param("username") String username);

    //--------------------add admin to db-----------------------
    @Insert("INSERT INTO users(username, password, role)" +
            "VALUES (#{enrollmentID}, #{password}, 'ADMIN')")
    void createAdmin(String enrollmentID, String password);

    //-----------------Find user by id-------------------
    @Select("SELECT * FROM users WHERE user_id = #{id}")
    @Result(property = "userId", column = "user_id")
    @Result(property = "fullName", column = "full_name")
    @Result(property = "department", column = "dep_id",
        many = @Many(select = "com.hrd.asset_holder_api.repository.DepartmentRepository.findDepartmentResponseById")
    )
    @Result(property = "profileImg", column = "profile_img")
    UserRequestResponse findUserById(@Param("id") Integer id);

    //----------------find user by name----------------------
    @Select("SELECT * FROM users WHERE username = #{username}")
    @Result(property = "userId", column = "user_id")
    @Result(property = "fullName", column = "full_name")
    @Result(property = "department", column = "dep_id",
            many = @Many(select = "com.hrd.asset_holder_api.repository.DepartmentRepository.findDepartmentResponseById")
    )
    @Result(property = "profileImg", column = "profile_img")
    UserRequestResponse findUserByName(String username);

    //------------find user id---------------
    @Select("SELECT * FROM users WHERE user_id = #{id}")
    @Result(property = "userId", column = "user_id")
    @Result(property = "roles", column = "role")
    User findUserId(@Param("id") Integer id);
}
