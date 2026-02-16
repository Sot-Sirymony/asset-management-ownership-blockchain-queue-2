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
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Service implementation for asset request management operations.
 */
@Slf4j
@Service
@AllArgsConstructor
public class AssetRequestServiceImp implements AssetRequestService {

    private final AssetRequestRepository assetRequestRepository;
    private final UserRepository userRepository;

    @Override
    public List<AssetRequest> getAllUserAssetRequest() {
        log.debug("Fetching all user asset requests");
        return assetRequestRepository.findAllUserAssetRequest();
    }

    /**
     * Retrieves asset request by ID.
     * Admins can view any request, users can only view their own requests.
     *
     * @param id The asset request ID
     * @return List of asset requests
     * @throws NotFoundException if request is not found
     */
    @Override
    public List<AssetRequest> getUserAssetRequestById(Integer id) {
        Integer userId = GetCurrentUser.currentId();
        log.debug("Fetching asset request: {} for user: {}", id, userId);
        
        User user = userRepository.findUserId(userId);
        if (user == null) {
            log.error("User not found: {}", userId);
            throw new NotFoundException("User not found");
        }
        
        if (user.getRoles().equals("ADMIN")){
            List<AssetRequest> requests = assetRequestRepository.findUserAssetRequestById(id);
            if (requests.isEmpty()){
                log.warn("Asset request not found: {}", id);
                throw new NotFoundException("The asset request is not found");
            }
            log.debug("Admin retrieved asset request: {}", id);
            return requests;
        }else{
            List<AssetRequest> requests = assetRequestRepository.findUserOwnAssetRequestById(id, userId);
            if (requests.isEmpty()) {
                log.warn("Asset request not found for user: {} - request ID: {}", userId, id);
                throw new NotFoundException("The asset request is not found");
            }
            log.debug("User {} retrieved own asset request: {}", userId, id);
            return requests;
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
