package com.hrd.asset_holder_api.helper;

import org.hyperledger.fabric.gateway.Identities;
import org.hyperledger.fabric.gateway.Wallet;
import org.hyperledger.fabric.gateway.Wallets;
import org.hyperledger.fabric.gateway.X509Identity;
import org.hyperledger.fabric.sdk.Enrollment;
import org.hyperledger.fabric.sdk.User;

import java.io.IOException;
import java.nio.file.Paths;
import java.security.PrivateKey;
import java.util.Set;

public class IdentityHelper {
    public static User getAdminIdentity() throws IOException {

        Wallet wallet = Wallets.newFileSystemWallet(Paths.get("wallet"));
        X509Identity adminIdentity = (X509Identity)wallet.get("admin");

        User admin = new User() {
            @Override
            public String getName() {
                return "admin";
            }

            @Override
            public Set<String> getRoles() {
                return null;
            }

            @Override
            public String getAccount() {
                return null;
            }

            @Override
            public String getAffiliation() {
                return "org1.department1";
            }

            @Override
            public Enrollment getEnrollment() {
                return new Enrollment() {
                    @Override
                    public PrivateKey getKey() {
                        return adminIdentity.getPrivateKey();
                    }

                    @Override
                    public String getCert() {
                        return Identities.toPemString(adminIdentity.getCertificate());
                    }
                };
            }

            @Override
            public String getMspId() {
                return "Org1MSP";
            }
        };

        return admin;
    }
}
