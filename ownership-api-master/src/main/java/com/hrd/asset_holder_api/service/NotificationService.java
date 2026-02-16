package com.hrd.asset_holder_api.service;

import com.hrd.asset_holder_api.model.response.UserRequestResponse;

/**
 * Service interface for sending notifications.
 * Supports email notifications for asset transfers and other events.
 */
public interface NotificationService {

    /**
     * Sends a notification to both parties when an asset is transferred.
     *
     * @param assetId The ID of the transferred asset
     * @param assetName The name of the asset
     * @param fromUser The user transferring the asset
     * @param toUser The user receiving the asset
     */
    void sendAssetTransferNotification(String assetId, String assetName, 
                                       UserRequestResponse fromUser, UserRequestResponse toUser);

    /**
     * Sends a notification when an asset is created.
     *
     * @param assetId The ID of the created asset
     * @param assetName The name of the asset
     * @param assignedUser The user assigned to the asset
     */
    void sendAssetCreatedNotification(String assetId, String assetName, UserRequestResponse assignedUser);
}
