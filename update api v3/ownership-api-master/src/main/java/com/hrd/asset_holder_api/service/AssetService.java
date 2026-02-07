package com.hrd.asset_holder_api.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.hrd.asset_holder_api.model.entity.Asset;
import com.hrd.asset_holder_api.model.request.AssetTrasferRequest;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;

@Service
public interface AssetService {
    JsonNode getAssetById(String id) throws IOException;

    JsonNode createAsset(Asset asset);

    JsonNode updateAsset(String id, Asset asset);

    Boolean deleteAsset(String id);

    JsonNode getAllAsset();

    Boolean trasfterAsset(String id, AssetTrasferRequest assetTrasferRequest);

    JsonNode getHistoryById(String id);

    JsonNode getAllAssetHistroy();
}
