package com.up.asset_holder_api.service;

import com.fasterxml.jackson.databind.JsonNode;

/**
 * Service interface for asset verification.
 * Supports internal and external verification workflows as per BRD requirements.
 */
public interface VerificationService {

    /**
     * Verifies asset authenticity and ownership (Internal verification).
     * Used by internal auditors and department managers.
     *
     * @param assetId The asset ID to verify
     * @return Verification result with asset details and ownership history
     */
    JsonNode verifyAssetInternal(String assetId);

    /**
     * Verifies asset authenticity and ownership (External verification).
     * Used by external auditors and verifiers.
     *
     * @param assetId The asset ID to verify
     * @return Verification result with asset details and transaction history
     */
    JsonNode verifyAssetExternal(String assetId);

    /**
     * Gets complete asset verification trail including all transactions.
     *
     * @param assetId The asset ID
     * @return Complete verification trail
     */
    JsonNode getVerificationTrail(String assetId);
}
