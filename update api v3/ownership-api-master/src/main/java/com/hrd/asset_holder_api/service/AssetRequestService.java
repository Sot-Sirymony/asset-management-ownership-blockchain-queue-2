package com.hrd.asset_holder_api.service;

import com.hrd.asset_holder_api.model.entity.AssetRequest;
import com.hrd.asset_holder_api.model.request.AssetRequestRes;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface AssetRequestService {
    List<AssetRequest> getAllUserAssetRequest();

    List<AssetRequest> getUserAssetRequestById(Integer id);

    List<AssetRequest> getUserAssetRequest();

    AssetRequestRes createUserAssetRequest(AssetRequestRes requestRes);

    AssetRequestRes updateUserAssetRequest(Integer id,AssetRequestRes requestRes);

    Boolean deleteUserAsset(Integer id);
}
