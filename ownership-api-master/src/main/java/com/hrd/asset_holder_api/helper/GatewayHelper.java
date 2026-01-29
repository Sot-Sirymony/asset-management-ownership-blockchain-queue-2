package com.hrd.asset_holder_api.helper;

import lombok.AllArgsConstructor;
import org.hyperledger.fabric.gateway.Gateway;
import org.hyperledger.fabric.gateway.Wallet;
import org.hyperledger.fabric.gateway.Wallets;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

public class GatewayHelper {
    private static String currentUser;

    public static Gateway connect(String username) throws IOException {
        // Load a file system based wallet for managing identities.
        Path walletPath = Paths.get("wallet");
        Wallet wallet = Wallets.newFileSystemWallet(walletPath);

        // load a ccp
        Path networkConfigPath = Paths.get("src/main/resources/connection-org1.json");

        if (wallet.get(username) == null) {
            throw new IllegalArgumentException("User " + username + " does not exist in the wallet");
        }


        Gateway.Builder builder = Gateway.createBuilder();
        builder.identity(wallet, username).networkConfig(networkConfigPath).discovery(true);
        currentUser = username;
        return builder.connect();
    }
    public static String getCurrentUser() {
        return currentUser;
    }
}
