package com.hrd.asset_holder_api.service.serviceImp;

import com.hrd.asset_holder_api.repository.UserRepository;
import com.hrd.asset_holder_api.service.AdminService;
import lombok.AllArgsConstructor;
import org.hyperledger.fabric.gateway.Identities;
import org.hyperledger.fabric.gateway.Identity;
import org.hyperledger.fabric.gateway.Wallet;
import org.hyperledger.fabric.gateway.Wallets;
import org.hyperledger.fabric.sdk.security.CryptoSuite;
import org.hyperledger.fabric.sdk.security.CryptoSuiteFactory;
import org.hyperledger.fabric_ca.sdk.EnrollmentRequest;
import org.hyperledger.fabric_ca.sdk.HFCAClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

@Service

public class AdminServiceImp implements AdminService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${pemFile}")
    private String pemFile;

    Map<String, Object> res = null;
    Properties props = null;
    HFCAClient caClient = null;
    Wallet wallet = null;

    public AdminServiceImp(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @EventListener(ApplicationReadyEvent.class)
    @Override
    public void enrollService() {
        props = new Properties();
        props.put("pemFile", pemFile);
        props.put("allowAllHostNames", "true");

        try {
            caClient = HFCAClient.createNewInstance("https://localhost:7054", props);
            CryptoSuite cryptoSuite = CryptoSuiteFactory.getDefault().getCryptoSuite();
            caClient.setCryptoSuite(cryptoSuite);

            String enrollmentID = "admin";
            String password = "adminpw";

            wallet = Wallets.newFileSystemWallet(Paths.get("wallet"));

            if(wallet.get(enrollmentID) != null) {
                res = new HashMap<>();
                res.put("localDateTime", LocalDateTime.now());
                res.put("message", "An identity for the admin user \"admin\" already exists in the wallet");
                return;
            }

            // Enroll the admin user, and import the new identity into the wallet.
            EnrollmentRequest enrollmentRequestTLS = new EnrollmentRequest();
            enrollmentRequestTLS.addHost("localhost");
            enrollmentRequestTLS.setProfile("tls");

            org.hyperledger.fabric.sdk.Enrollment enrollment = caClient.enroll(enrollmentID, password, enrollmentRequestTLS);
            Identity user = Identities.newX509Identity("Org1MSP", enrollment);
            wallet.put(enrollmentID, user);
            String encodedPassword = passwordEncoder.encode(password);
            userRepository.createAdmin(enrollmentID, encodedPassword);

            res = new HashMap<>();
            res.put("localDateTime", LocalDateTime.now());
            res.put("message", "Successfully enrolled " + enrollmentID + " and imported into the wallet");

        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }
}
