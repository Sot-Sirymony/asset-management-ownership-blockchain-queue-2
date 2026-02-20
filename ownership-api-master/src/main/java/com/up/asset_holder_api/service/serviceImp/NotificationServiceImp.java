package com.up.asset_holder_api.service.serviceImp;

import com.up.asset_holder_api.model.response.UserRequestResponse;
import com.up.asset_holder_api.service.NotificationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * Implementation of notification service.
 * Currently logs notifications. Can be extended to send emails, SMS, or push notifications.
 */
@Slf4j
@Service
public class NotificationServiceImp implements NotificationService {

    /**
     * Sends notification to both parties when an asset is transferred.
     * As per BRD requirement: "Both parties receive notification".
     */
    @Override
    public void sendAssetTransferNotification(String assetId, String assetName, 
                                             UserRequestResponse fromUser, UserRequestResponse toUser) {
        log.info("=== Asset Transfer Notification ===");
        log.info("Asset ID: {}", assetId);
        log.info("Asset Name: {}", assetName);
        log.info("Transferred from: {} (ID: {})", 
                fromUser.getFullName() != null ? fromUser.getFullName() : fromUser.getUsername(), 
                fromUser.getUserId());
        log.info("Transferred to: {} (ID: {})", 
                toUser.getFullName() != null ? toUser.getFullName() : toUser.getUsername(), 
                toUser.getUserId());
        log.info("================================");

        // TODO: Implement email notification
        // Example:
        // emailService.sendEmail(
        //     fromUser.getEmail(),
        //     "Asset Transferred",
        //     "You have transferred asset " + assetName + " to " + toUser.getFullName()
        // );
        // emailService.sendEmail(
        //     toUser.getEmail(),
        //     "Asset Assigned",
        //     "You have been assigned asset " + assetName + " from " + fromUser.getFullName()
        // );
    }

    /**
     * Sends notification when an asset is created.
     */
    @Override
    public void sendAssetCreatedNotification(String assetId, String assetName, UserRequestResponse assignedUser) {
        log.info("=== Asset Created Notification ===");
        log.info("Asset ID: {}", assetId);
        log.info("Asset Name: {}", assetName);
        log.info("Assigned to: {} (ID: {})", 
                assignedUser.getFullName() != null ? assignedUser.getFullName() : assignedUser.getUsername(), 
                assignedUser.getUserId());
        log.info("================================");

        // TODO: Implement email notification
        // Example:
        // emailService.sendEmail(
        //     assignedUser.getEmail(),
        //     "Asset Assigned",
        //     "You have been assigned a new asset: " + assetName
        // );
    }
}
