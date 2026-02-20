package com.up.asset_holder_api.util;

import java.util.UUID;

/**
 * Utility class for generating thread-safe asset IDs.
 * Uses UUID to ensure uniqueness and thread-safety in concurrent environments.
 */
public class AssetIdGenerator {

    /**
     * Generates a unique asset ID using UUID.
     * Format: "asset-" + UUID (without hyphens for shorter IDs)
     *
     * @return A unique asset ID string
     */
    public static String generateAssetId() {
        return "asset-" + UUID.randomUUID().toString().replace("-", "");
    }

    /**
     * Generates a unique asset ID with a custom prefix.
     *
     * @param prefix The prefix to use for the asset ID
     * @return A unique asset ID string with the specified prefix
     */
    public static String generateAssetId(String prefix) {
        return prefix + "-" + UUID.randomUUID().toString().replace("-", "");
    }
}
