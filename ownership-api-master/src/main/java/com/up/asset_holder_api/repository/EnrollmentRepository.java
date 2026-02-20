package com.up.asset_holder_api.repository;

import com.up.asset_holder_api.model.request.UserPassword;
import com.up.asset_holder_api.model.request.UserRegister;
import com.up.asset_holder_api.model.request.UserRequest;
import com.up.asset_holder_api.model.response.UserResponse;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface EnrollmentRepository {

    @Select("INSERT INTO users(full_name, username, password, dep_id, email, role, certificate) " +
            "VALUES (#{rq.fullName}, #{rq.username}, #{rq.password}, #{rq.department}, #{rq.email}, 'USER', #{certificate}) " +
            "RETURNING full_name, username, dep_id, email, role")

    @Results(
            id = "enroll",
            value = {
                    @Result(property = "fullName", column = "full_name"),
                    @Result(property = "department", column = "dep_id")
            }
    )
    UserRegister createUser(@Param("rq") UserRegister user, String certificate);


    //----------------Get all user for admin------------
    @Select("SELECT * FROM users WHERE role != 'ADMIN' " +
            "LIMIT #{size} OFFSET ((#{page} - 1) * #{size})")
    @Result(property = "userId", column = "user_id")
    @Result(property = "fullName", column = "full_name")
    @Result(property = "department", column = "dep_id",
            one = @One(select = "com.up.asset_holder_api.repository.DepartmentRepository.findDepartmentResponseById")
    )
    @Result(property = "phoneNumber", column = "phone_number")
    @Result(property = "profileImg", column = "profile_img")
    @Result(property = "placeOfBirth",column = "place_of_birth")
    List<UserResponse> findAllUser(Integer size, Integer page);

    //----------------update user admin------------------------
    @Select(""" 
            UPDATE users SET full_name = #{rq.full_name},
                            gender = #{rq.gender},
                            phone_number = #{rq.phone_number}, address = #{rq.address},
                            dob = #{rq.dob},
                            place_of_birth = #{rq.place_of_birth},
                            description = #{rq.description},
                            profile_img = #{rq.profile_img}
            WHERE user_id = #{id} RETURNING *;
            """)
    UserRequest updateUser(Integer id,@Param("rq") UserRequest userRequest);


    //---------------------Admin get user by id--------------------------
    @Select("SELECT * FROM users WHERE user_id = #{id}")
    @Results(
            id = "user",
            value = {
                    @Result(property = "userId", column = "user_id"),
                    @Result(property = "fullName", column = "full_name"),
                    @Result(property = "department", column = "dep_id",
                            one = @One(select = "com.up.asset_holder_api.repository.DepartmentRepository.findDepartmentResponseById")
                    ),
                    @Result(property = "phoneNumber", column = "phone_number"),
                    @Result(property = "profileImg", column = "profile_img"),
                    @Result(property = "placeOfBirth",column = "place_of_birth"),
                    @Result(property = "email", column = "email")
            }
    )
    UserResponse findUserById(Integer id);


    @Select("SELECT * FROM users WHERE user_id = #{id}")
//    @Result(property = "userId", column = "user_id")
//    @Result(property = "fullName", column = "full_name")
//    @Result(property = "department", column = "dep_id",
//            one = @One(select = "com.up.asset_holder_api.repository.DepartmentRepository.findDepartmentResponseById")
//    )
//    @Result(property = "phoneNumber", column = "phone_number")
//    @Result(property = "profileImg", column = "profile_img")
//    @Result(property = "placeOfBirth",column = "place_of_birth")
    UserResponse findCurrentUserById(Integer id);


    @Update("""
            UPDATE users SET full_name = #{rq.full_name}, 
                 gender = #{rq.gender}, 
                phone_number = #{rq.phone_number},
                address = #{rq.address},
                dob = #{rq.dob},
                place_of_birth = #{rq.place_of_birth},
                description = #{rq.description},
                profile_img = #{rq.profile_img}
            WHERE user_id = #{userId};
            """)
    boolean updateProfile(@Param("rq") UserRequest userRequest,Integer userId);


    @Update("""
            UPDATE users SET password = #{rq.newPassword}
            WHERE user_id = #{userId}
            """)
    Boolean updateAdminPassword(@Param("rq") UserPassword userPassword, Integer userId);


    //-----------------------find old password-------------------
    @Select("""
            SELECT password FROM users WHERE user_id = #{userId}
            """)
    String findOldPassword(Integer userId);


    @Delete("""
    DELETE FROM users WHERE user_id = #{userId} AND user_id != #{adminId}
    """)
    Boolean deleteUserById(Integer userId, Integer adminId);
}
