package com.hrd.asset_holder_api.helper;

import org.hyperledger.fabric.gateway.Gateway;
import org.hyperledger.fabric.gateway.Network;
import org.hyperledger.fabric.gateway.Wallet;
import org.hyperledger.fabric.gateway.Wallets;

import java.io.InputStream;
import java.nio.file.*;

public class GatewayHelperV1 {

    private static final String DEFAULT_CHANNEL = "mychannel";
    private static final String DEFAULT_CONN_PROFILE = "/app/connection.yaml";
    private static final String DEFAULT_WALLET_DIR = "/app/wallet";

    private static Path resolveNetworkConfigPath() throws Exception {
        // Prefer mounted file (Docker / K8s)
        String profilePath = System.getenv().getOrDefault("CONNECTION_PROFILE", DEFAULT_CONN_PROFILE);
        Path fsPath = Paths.get(profilePath);

        if (Files.exists(fsPath) && Files.isRegularFile(fsPath)) {
            return fsPath;
        }

        // Fallback local dev: copy classpath resource to a real temp file
        try (InputStream in = GatewayHelperV1.class.getClassLoader().getResourceAsStream("connection.yaml")) {
            if (in == null) {
                throw new IllegalStateException(
                        "connection.yaml not found. Mount it at " + profilePath +
                                " (recommended) or include it in src/main/resources."
                );
            }

            Path tmp = Files.createTempFile("fabric-connection-", ".yaml");
            tmp.toFile().deleteOnExit();
            Files.copy(in, tmp, StandardCopyOption.REPLACE_EXISTING);
            return tmp;
        }
    }

    private static Wallet loadWallet() throws Exception {
        String walletDir = System.getenv().getOrDefault("WALLET_PATH", DEFAULT_WALLET_DIR);
        Path walletPath = Paths.get(walletDir);

        if (!Files.exists(walletPath)) {
            throw new IllegalStateException("Wallet path does not exist: " + walletPath.toAbsolutePath());
        }
        return Wallets.newFileSystemWallet(walletPath);
    }

    public static Gateway connect(String username) throws Exception {
        Wallet wallet = loadWallet();

        if (wallet.get(username) == null) {
            String walletDir = System.getenv().getOrDefault("WALLET_PATH", DEFAULT_WALLET_DIR);
            throw new IllegalArgumentException(
                    "User '" + username + "' does not exist in wallet: " + Paths.get(walletDir).toAbsolutePath()
            );
        }

        Path networkConfigPath = resolveNetworkConfigPath();

        boolean discoveryEnabled = Boolean.parseBoolean(
                System.getenv().getOrDefault("FABRIC_DISCOVERY", "true")
        );

        // IMPORTANT: do NOT rely on localhost inside Docker.
        // This setting is handled by your connection.yaml (it must not contain localhost),
        // but we also keep discovery configurable.
        return Gateway.createBuilder()
                .identity(wallet, username)
                .networkConfig(networkConfigPath)
                .discovery(discoveryEnabled)
                .connect();
    }

    /** Always use channel from env so services never hardcode it */
    public static Network getNetwork(Gateway gateway) {
        String channel = System.getenv().getOrDefault("FABRIC_CHANNEL", DEFAULT_CHANNEL);
        return gateway.getNetwork(channel);
    }
}
