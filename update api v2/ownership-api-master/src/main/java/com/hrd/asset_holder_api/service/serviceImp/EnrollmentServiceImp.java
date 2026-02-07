package com.hrd.asset_holder_api.service.serviceImp;

import com.hrd.asset_holder_api.exception.NotFoundException;
import com.hrd.asset_holder_api.helper.IdentityHelper;
import com.hrd.asset_holder_api.model.entity.User;
import com.hrd.asset_holder_api.model.request.UserPassword;
import com.hrd.asset_holder_api.model.request.UserRegister;
import com.hrd.asset_holder_api.model.request.UserRequest;
import com.hrd.asset_holder_api.model.response.UserRequestResponse;
import com.hrd.asset_holder_api.model.response.UserResponse;
import com.hrd.asset_holder_api.repository.EnrollmentRepository;
import com.hrd.asset_holder_api.service.EnrollmentService;
import com.hrd.asset_holder_api.utils.GetCurrentUser;
import lombok.AllArgsConstructor;
import org.hyperledger.fabric.gateway.*;
import org.hyperledger.fabric.sdk.Enrollment;
import org.hyperledger.fabric.sdk.security.CryptoSuite;
import org.hyperledger.fabric.sdk.security.CryptoSuiteFactory;
import org.hyperledger.fabric_ca.sdk.HFCAClient;
import org.hyperledger.fabric_ca.sdk.RegistrationRequest;
import org.hyperledger.fabric_ca.sdk.exception.EnrollmentException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.ErrorResponseException;

import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

@Service
public class EnrollmentServiceImp implements EnrollmentService {

    private final EnrollmentRepository enrollmentRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${pemFile}")
    private String pemFile;

    public EnrollmentServiceImp(EnrollmentRepository enrollmentRepository, PasswordEncoder passwordEncoder) {
        this.enrollmentRepository = enrollmentRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public UserRegister registerUser(UserRegister user) {

        Properties props = new Properties();
        props.put("pemFile", pemFile);
        props.put("allowAllHostNames", "true");

        try {
            // Initialize HFCAClient
            HFCAClient caClient = HFCAClient.createNewInstance("https://localhost:7054", props);
            CryptoSuite cryptoSuite = CryptoSuiteFactory.getDefault().getCryptoSuite();
            caClient.setCryptoSuite(cryptoSuite);

            // Initialize wallet
            Wallet wallet = Wallets.newFileSystemWallet(Paths.get("wallet"));

            // Check if user identity already exists in the wallet
            if (wallet.get(user.getUsername()) != null) {
                String message = "An identity for the user \"" + user.getUsername() + "\" already exists in the wallet.";
                System.out.println(message);
//                return ResponseEntity.status(HttpStatus.CONFLICT).body(message);
            }

            // Check if admin identity exists in the wallet
            X509Identity adminIdentity = (X509Identity) wallet.get("admin");
            if (adminIdentity == null) {
                throw new NotFoundException("Admin needs to be enrolled and added to the wallet first");
            }

            org.hyperledger.fabric.sdk.User admin = IdentityHelper.getAdminIdentity();

            // Register the user, enroll the user, and import the new identity into the wallet.
            RegistrationRequest registrationRequest = new RegistrationRequest(user.getUsername());
            registrationRequest.setAffiliation("org1.department1");
            registrationRequest.setEnrollmentID(user.getUsername());

            String enrollmentSecret = caClient.register(registrationRequest, admin);
            Enrollment enrollment = caClient.enroll(user.getUsername(), enrollmentSecret);
            String certificate = enrollment.getCert();
            Identity userEnroll = Identities.newX509Identity("Org1MSP", enrollment);
            wallet.put(user.getUsername(), userEnroll);

            //hash password
            user.setPassword(passwordEncoder.encode(user.getPassword()));
            enrollmentRepository.createUser(user,certificate);
            user.setPassword(null);
            return user;

        } catch (Exception e) {
            String errorMessage = "Error during enrollment: " + e.getMessage();
            e.printStackTrace();
            throw new NotFoundException(errorMessage);
        }
    }

    @Override
    public List<UserResponse> getAllUser(Integer size, Integer page) {
        return enrollmentRepository.findAllUser(size,page);
    }

    @Override
    public UserRequest updateUser(Integer id,UserRequest userRequest) {
        return enrollmentRepository.updateUser(id,userRequest);
    }

    @Override
    public UserResponse getUserById(Integer id) {
        return enrollmentRepository.findUserById(id);
    }

    @Override
    public UserResponse getProfile() {
        Integer userId = GetCurrentUser.currentId();
        return enrollmentRepository.findUserById(userId);
    }

    @Override
    public boolean updateProfile(UserRequest userRequest) {
        Integer userId = GetCurrentUser.currentId();
        System.out.println("profile image" + userRequest.getProfile_img());
        return enrollmentRepository.updateProfile(userRequest, userId);
    }

    @Override
    public Boolean changePassword(UserPassword userPassword) {
        Integer userId = GetCurrentUser.currentId();
        String oldPassword = enrollmentRepository.findOldPassword(userId);

        if (!passwordEncoder.matches(userPassword.getOldPassword(), oldPassword)) {
            throw new NotFoundException("Old password does not match");
        }
        else if(!userPassword.getNewPassword().equals(userPassword.getConfirmPassword())) {
            throw new NotFoundException("New password does not match");
        }

        userPassword.setNewPassword(passwordEncoder.encode(userPassword.getNewPassword()));

        return enrollmentRepository.updateAdminPassword(userPassword,userId);
    }

    @Override
    public Boolean deleteUser(Integer id) {
        Integer adminId = GetCurrentUser.currentId();
        return enrollmentRepository.deleteUserById(id, adminId);
    }


}
