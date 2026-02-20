package com.up.asset_holder_api.service.serviceImp;

import com.up.asset_holder_api.repository.UserRepository;
import com.up.asset_holder_api.service.AdminService;
import org.hyperledger.fabric.gateway.Identities;
import org.hyperledger.fabric.gateway.Identity;
import org.hyperledger.fabric.gateway.Wallet;
import org.hyperledger.fabric.gateway.Wallets;
import org.hyperledger.fabric.sdk.Enrollment;
import org.hyperledger.fabric.sdk.security.CryptoSuite;
import org.hyperledger.fabric.sdk.security.CryptoSuiteFactory;
import org.hyperledger.fabric_ca.sdk.HFCAClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import lombok.extern.slf4j.Slf4j;

import java.nio.file.Paths;
import java.util.Properties;

@Slf4j
@Service
public class AdminServiceImp implements AdminService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${pemFile}")
    private String pemFile;

    @Value("${fabric.ca.org1.url:https://ca.org1.ownify.com:7054}")
    private String fabricCaOrg1Url;

    @Value("${fabric.org1.mspId:Org1MSP}")
    private String fabricOrg1MspId;

    @Value("${fabric.gateway.admin.id:admin}")
    private String fabricGatewayAdminId;

    @Value("${fabric.gateway.admin.password:adminpw}")
    private String fabricGatewayAdminPassword;

    public AdminServiceImp(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @EventListener(ApplicationReadyEvent.class)
    @Override
    public void enrollService() {
        Properties props = new Properties();
        props.put("pemFile", pemFile);
        props.put("allowAllHostNames", "true");

        try {
            HFCAClient caClient = HFCAClient.createNewInstance(fabricCaOrg1Url, props);
            CryptoSuite cryptoSuite = CryptoSuiteFactory.getDefault().getCryptoSuite();
            caClient.setCryptoSuite(cryptoSuite);

            String walletPath = System.getenv().getOrDefault("WALLET_PATH", "wallet");
            Wallet wallet = Wallets.newFileSystemWallet(Paths.get(walletPath));

            // Always refresh gateway identity on startup so wallet stays valid after Fabric network reset.
            Enrollment enrollment = caClient.enroll(fabricGatewayAdminId, fabricGatewayAdminPassword);
            Identity identity = Identities.newX509Identity(fabricOrg1MspId, enrollment);
            wallet.put(fabricGatewayAdminId, identity);

            if (userRepository.findUserByUsername(fabricGatewayAdminId) == null) {
                String encodedPassword = passwordEncoder.encode(fabricGatewayAdminPassword);
                userRepository.createAdmin(fabricGatewayAdminId, encodedPassword);
            }
            log.info(
                    "Successfully refreshed Fabric gateway identity '{}' in wallet path '{}'",
                    fabricGatewayAdminId,
                    walletPath
            );

        } catch (Exception e) {
            log.error("Error during admin enrollment: {}", e.getMessage(), e);
            throw new IllegalStateException("Error during admin enrollment", e);
        }
    }
}
