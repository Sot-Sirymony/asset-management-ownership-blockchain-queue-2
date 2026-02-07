package com.hrd.asset_holder_api.repository;

import com.hrd.asset_holder_api.model.entity.AssetRequest;
import com.hrd.asset_holder_api.model.request.AssetRequestRes;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface AssetRequestRepository {

    //-----------------Admin see list of request--------------#
    @Select("SELECT * FROM asset_request")
    @Results(
            id = "asset_request",
            value = {
                    @Result(property = "requestId", column = "request_id"),
                    @Result(property = "user", column = "user_id",
                        one = @One(select = "com.hrd.asset_holder_api.repository.UserRepository.findUserById")
                    ),
                    @Result(property = "assetName", column = "asset_name"),
                    @Result(property = "createdAt", column = "created_at")
            }
    )
    List<AssetRequest> findAllUserAssetRequest();


    //-------------------Admin see asset request by id -------------------------
    @Select("SELECT * FROM asset_request WHERE request_id = #{id}")
    @ResultMap("asset_request")
    List<AssetRequest> findUserAssetRequestById(Integer id);


    //--------------------User see asset request -------------------------------
    @Select("SELECT * FROM asset_request WHERE user_id = #{userId}")
    @ResultMap("asset_request")
    List<AssetRequest> findUserAssetRequest(Integer userId);


    //---------------------User create asset request-----------------------------
    @Select("""
            INSERT INTO asset_request(user_id, asset_name, qty, unit, reason, attachment)
            VALUES (#{userId}, #{rq.assetName}, #{rq.qty}, #{rq.unit}, #{rq.reason}, #{rq.attachment})
            RETURNING *;
            """)
    @Result(property = "assetName", column = "asset_name")
    @Result(property = "createdAt", column = "created_at")
    AssetRequestRes insertUserAssetRequest(@Param("rq") AssetRequestRes requestRes, Integer userId);


    //-----------------------User update asset request -------------------------
    @Select("""
            UPDATE asset_request SET asset_name = #{rq.assetName}, qty = #{rq.qty}, 
                         reason = #{rq.reason}, attachment = #{rq.attachment}
            WHERE request_id = #{requestId} RETURNING *
            """)
    @Result(property = "assetName", column = "asset_name")
    @Result(property = "createdAt", column = "created_at")
    AssetRequestRes updateUserAssetRequest(@Param("rq") AssetRequestRes requestRes, Integer requestId);

    @Select("SELECT * FROM asset_request WHERE request_id = #{requestId} AND user_id = #{userId}")
    @ResultMap("asset_request")
    List<AssetRequest> findUserOwnAssetRequestById(Integer requestId, Integer userId);


    //-----------------user delete asset----------------
    @Delete("DELETE FROM asset_request WHERE request_id = #{id} AND user_id = #{userId}")
    Boolean deleteUserAsset(Integer id, Integer userId);

}
