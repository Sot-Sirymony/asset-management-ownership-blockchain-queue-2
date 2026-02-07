package com.hrd.asset_holder_api.service.serviceImp;

import com.hrd.asset_holder_api.exception.NotFoundException;
import com.hrd.asset_holder_api.model.entity.AssetRequest;
import com.hrd.asset_holder_api.model.entity.User;
import com.hrd.asset_holder_api.model.request.AssetRequestRes;
import com.hrd.asset_holder_api.repository.AssetRequestRepository;
import com.hrd.asset_holder_api.repository.UserRepository;
import com.hrd.asset_holder_api.service.AssetRequestService;
import com.hrd.asset_holder_api.utils.GetCurrentUser;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@AllArgsConstructor
public class AssetRequestServiceImp implements AssetRequestService {

    private final AssetRequestRepository assetRequestRepository;
    private final UserRepository userRepository;

    @Override
    public List<AssetRequest> getAllUserAssetRequest() {
        return assetRequestRepository.findAllUserAssetRequest();
    }

    @Override
    public List<AssetRequest> getUserAssetRequestById(Integer id) {
        Integer userId = GetCurrentUser.currentId();
        System.out.println("=================");
        User user = userRepository.findUserId(userId);
        System.out.println("User"+user);
        if (user.getRoles().equals("ADMIN")){
            System.out.println("xxxx"+assetRequestRepository.findUserAssetRequestById(id));
            if (assetRequestRepository.findUserAssetRequestById(id).isEmpty()){
                throw new NotFoundException("The asset request is null");
            }else
                return assetRequestRepository.findUserAssetRequestById(id);
        }else{
            if (assetRequestRepository.findUserOwnAssetRequestById(id,userId).isEmpty())
                throw new NotFoundException("The asset request is null");
            return assetRequestRepository.findUserOwnAssetRequestById(id,userId);
        }
    }

    @Override
    public List<AssetRequest> getUserAssetRequest() {
        Integer userId = GetCurrentUser.currentId();
        return assetRequestRepository.findUserAssetRequest(userId);
    }

    @Override
    public AssetRequestRes createUserAssetRequest(AssetRequestRes requestRes) {
        Integer userId = GetCurrentUser.currentId();
        return assetRequestRepository.insertUserAssetRequest(requestRes,userId);
    }

    @Override
    public AssetRequestRes updateUserAssetRequest(Integer requestId,AssetRequestRes requestRes) {
        return assetRequestRepository.updateUserAssetRequest(requestRes,requestId);
    }

    @Override
    public Boolean deleteUserAsset(Integer id) {
        Integer userId = GetCurrentUser.currentId();
        return assetRequestRepository.deleteUserAsset(id, userId);
    }
}
