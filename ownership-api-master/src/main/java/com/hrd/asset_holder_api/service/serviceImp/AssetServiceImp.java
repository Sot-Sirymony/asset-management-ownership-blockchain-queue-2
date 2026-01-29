package com.hrd.asset_holder_api.service.serviceImp;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.hrd.asset_holder_api.exception.NotFoundException;
import com.hrd.asset_holder_api.helper.GatewayHelper;
import com.hrd.asset_holder_api.model.entity.Asset;
import com.hrd.asset_holder_api.model.entity.User;
import com.hrd.asset_holder_api.model.request.AssetTrasferRequest;
import com.hrd.asset_holder_api.model.response.UserRequestResponse;
import com.hrd.asset_holder_api.repository.UserRepository;
import com.hrd.asset_holder_api.service.AssetService;
import com.hrd.asset_holder_api.utils.GetCurrentUser;
import lombok.AllArgsConstructor;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.hyperledger.fabric.gateway.*;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Optional;

@Service
@AllArgsConstructor
public class AssetServiceImp implements AssetService {

    private static final ObjectMapper MAPPER = new ObjectMapper();
    private final UserRepository userRepository;
    private static int i=10;

    @Override
    public JsonNode getAssetById(String id) {
        Integer userId = GetCurrentUser.currentId();
        UserRequestResponse user = userRepository.findUserById(userId);
        try (Gateway gateway = GatewayHelper.connect(user.getUsername())) {

            Network network = gateway.getNetwork("channel-org");
            Contract contract = network.getContract("basic");

            byte[] result = contract.submitTransaction("QueryAsset", id);

            String assetJson = new String(result, StandardCharsets.UTF_8);
            JsonNode assetNode = MAPPER.readTree(assetJson);

            return assetNode;

        } catch (ContractException e) {
            System.err.println("Failed to evaluate transaction: " + e.getMessage());
        }
        catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    @Override
    public JsonNode createAsset(Asset asset) {
        Integer userId = GetCurrentUser.currentId();
        UserRequestResponse user = userRepository.findUserById(userId);
        try (Gateway gateway = GatewayHelper.connect(user.getUsername())) {
            Network network = gateway.getNetwork("channel-org");
            Contract contract = network.getContract("basic");
            asset.setAssetId("asset"+i);
            asset.setUsername(asset.getAssignTo().toString());
            i++;
            contract.submitTransaction("CreateAsset",
                    asset.getAssetId(),
                    asset.getAssetName(),
                    asset.getUnit(),
                    asset.getCondition(),
                    asset.getAttachment(),
                    asset.getAssignTo().toString(),
                    asset.getUsername(),
                    asset.getDepName(),
                    String.valueOf(asset.getQty())
            );
            byte[] result = contract.submitTransaction("QueryAsset", asset.getAssetId());
            System.out.println(asset.getAssetId());
            String assetJson = new String(result, StandardCharsets.UTF_8);
            JsonNode assetNode = MAPPER.readTree(assetJson);

            return assetNode;

        } catch (ContractException e) {
            System.err.println("Failed to evaluate transaction: " + e.getMessage());
            i++;
        }
        catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    @Override
    public JsonNode updateAsset(String id, Asset asset) {
        Integer userId = GetCurrentUser.currentId();
        UserRequestResponse user = userRepository.findUserById(userId);
        try (Gateway gateway = GatewayHelper.connect(user.getUsername())) {
            Network network = gateway.getNetwork("channel-org");
            Contract contract = network.getContract("basic");
            asset.setUsername(asset.getAssignTo().toString());
            contract.submitTransaction("UpdateAsset",
                    id,
                    asset.getAssetName(),
                    asset.getUnit(),
                    asset.getCondition(),
                    asset.getAttachment(),
                    asset.getAssignTo().toString(),
                    asset.getUsername(),
                    asset.getDepName(),
                    String.valueOf(asset.getQty())
            );
            byte[] result = contract.submitTransaction("QueryAsset", id);

            String assetJson = new String(result, StandardCharsets.UTF_8);
            JsonNode assetNode = MAPPER.readTree(assetJson);

            return assetNode;
        } catch (ContractException e) {
            System.err.println("Failed to evaluate transaction: " + e.getMessage());
        }
        catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    @Override
    public Boolean deleteAsset(String id) {
        Integer userId = GetCurrentUser.currentId();
        UserRequestResponse user = userRepository.findUserById(userId);
        try (Gateway gateway = GatewayHelper.connect(user.getUsername())) {
            Network network = gateway.getNetwork("channel-org");
            Contract contract = network.getContract("basic");

            contract.submitTransaction("QueryAsset", id);
            contract.submitTransaction("DeleteAsset", id);

            return true;
        } catch (ContractException e) {
            System.err.println("Failed to evaluate transaction: " + e.getMessage());
            return false;
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public JsonNode getAllAsset() {
        Integer userId = GetCurrentUser.currentId();
        UserRequestResponse userResponse = userRepository.findUserById(userId);
        try (Gateway gateway = GatewayHelper.connect(userResponse.getUsername())) {

            Network network = gateway.getNetwork("channel-org");
            Contract contract = network.getContract("basic");


            ArrayNode assetsWithUserInfo = MAPPER.createArrayNode();

            //check user to display data
            Integer currentUserId = GetCurrentUser.currentId();
            UserRequestResponse currentUser = userRepository.findUserById(currentUserId);
            System.out.println("current user"+ currentUser);
            User userRoles = userRepository.findUserByUsername(currentUser.getUsername());
            boolean isAdmin = userRoles.getRoles().equals("ADMIN");

            byte[] result = contract.submitTransaction("QueryAllAssets");

            String assetJson = new String(result, StandardCharsets.UTF_8);
            JsonNode assetNode = MAPPER.readTree(assetJson);
            for (JsonNode asset : assetNode) {
                if (!asset.has("asset_id") || asset.get("asset_id").asText().isEmpty()) {
                    continue;
                }

                ObjectNode assetWithUserInfo = MAPPER.createObjectNode();
                String username = asset.get("assign_to").asText();
                System.out.println(username);
                int userid = Integer.parseInt(username);
                if (isAdmin || (userid ==(currentUser.getUserId()))) {
                    // Add basic asset information
                    assetWithUserInfo.put("assetId", asset.has("asset_id") ? asset.get("asset_id").asText() : null);
                    assetWithUserInfo.put("assetName", asset.has("asset_name") ? asset.get("asset_name").asText() : null);
                    assetWithUserInfo.put("qty", asset.has("qty") ? asset.get("qty").asText() : null);
                    assetWithUserInfo.put("condition", asset.has("condition") ? asset.get("condition").asText() : null);
                    assetWithUserInfo.put("attachment", asset.has("attachment") ? asset.get("attachment").asText() : null);
                    assetWithUserInfo.put("assignDate", asset.has("created_at") ? asset.get("created_at").asText() : null);
                    assetWithUserInfo.put("depName", asset.has("dep_name") ? asset.get("dep_name").asText() : null);

                    UserRequestResponse user = userRepository.findUserById(userid);
                    System.out.println(user);
                    if (user != null) {
                        ObjectNode userJson = MAPPER.createObjectNode();
                        userJson.put("userId", user.getUserId() != null ? user.getUserId().toString() : null);
                        userJson.put("fullName", user.getFullName() != null ? user.getFullName() : "");
                        userJson.put("profileImg", user.getProfileImg() != null ? user.getProfileImg() : "");

                        if (user.getDepartment() != null) {
                            userJson.put("department", user.getDepartment().getDep_name() != null ? user.getDepartment().getDep_name() : "");
                        } else {
                            userJson.put("department", "");
                        }
                        assetWithUserInfo.set("assignTo", userJson);
                    }
                    assetsWithUserInfo.add(assetWithUserInfo);
                }
            }

            return assetsWithUserInfo;

        } catch (ContractException e) {
            System.err.println("Failed to evaluate transaction: " + e.getMessage());
        }
        catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    @Override
    public Boolean trasfterAsset(String id, AssetTrasferRequest assetTrasferRequest) {
        Integer userId = GetCurrentUser.currentId();
        UserRequestResponse user = userRepository.findUserById(userId);
        try (Gateway gateway = GatewayHelper.connect(user.getUsername())) {
            Network network = gateway.getNetwork("channel-org");
            Contract contract = network.getContract("basic");
            contract.submitTransaction("TransferAsset",
                    id,
                    assetTrasferRequest.getNewAssignTo().toString()
            );
            return true;
        } catch (ContractException e) {
            System.err.println("Failed to evaluate transaction: " + e.getMessage());
            throw new NotFoundException("Id " + id + " not found");
        }
        catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    @Override
    public JsonNode getHistoryById(String id) {
        Integer userId = GetCurrentUser.currentId();
        UserRequestResponse user = userRepository.findUserById(userId);
        try (Gateway gateway = GatewayHelper.connect(user.getUsername())) {

            Network network = gateway.getNetwork("channel-org");
            Contract contract = network.getContract("basic");

            byte[] result = contract.submitTransaction("GetAssetHistory", id);

            String assetJson = new String(result, StandardCharsets.UTF_8);
            JsonNode assetNode = MAPPER.readTree(assetJson);

            return assetNode;

        } catch (ContractException e) {
            System.err.println("Failed to evaluate transaction: " + e.getMessage());
        }
        catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

//    @Override
//    public JsonNode getAllAssetHistroy() {
//        String url = "http://admin:password@127.0.0.1:5985/channel-org_basic/_all_docs?include_docs=true";
//        System.out.println("url" + url);
//        Integer userId = GetCurrentUser.currentId();
//        UserRequestResponse userResponse = userRepository.findUserById(userId);
//        try (Gateway gateway = GatewayHelper.connect(userResponse.getUsername())){
//            List<JsonNode> results = new ArrayList<>();
//
//            URL obj = new URL(url);
//            HttpURLConnection connection = (HttpURLConnection) obj.openConnection();
//
//            connection.setRequestMethod("GET");
//            connection.setRequestProperty("Accept", "application/json");
//
//            String userCredentials = "admin:password";
//            String basicAuth = "Basic " + Base64.getEncoder().encodeToString(userCredentials.getBytes());
//            connection.setRequestProperty("Authorization", basicAuth);
//
//            int responseCode = connection.getResponseCode();
//            System.out.println("Response Code: " + responseCode);
//
//            if (responseCode == HttpURLConnection.HTTP_OK) {
//                BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
//                String inputLine;
//                StringBuilder response = new StringBuilder();
//                while ((inputLine = in.readLine()) != null) {
//                    response.append(inputLine);
//                }
//                in.close();
//
//                ObjectMapper mapper = new ObjectMapper();
//                JsonNode rootNode = mapper.readTree(String.valueOf(response));
//
//                JsonNode rows = rootNode.get("rows");
//
//                for (JsonNode row : rows) {
//                    String id = row.get("id").asText();
//
//                    if (!id.equals("\u0000􏿿initialized")) {
//                        System.out.println("Processing ID: " + id);
//
//                        Network network = gateway.getNetwork("channel-org");
//                        Contract contract = network.getContract("basic");
//
//                        byte[] resultBytes = contract.submitTransaction("GetAssetHistory", id);
//                        String assetJson = new String(resultBytes, StandardCharsets.UTF_8);
//
//                        JsonNode historyResponse = MAPPER.readTree(assetJson);
//                        System.out.println("histpro"+historyResponse);
//                        for (JsonNode asset : historyResponse) {
//                            if (!asset.has("asset_name") || asset.get("asset_name").asText().isEmpty()) {
//                                continue;
//                            }
//                        }
//                        results.add(historyResponse);
//
//                    }
//                }
//
//                return MAPPER.valueToTree(results);
//
//            } else {
//                System.out.println("GET request failed");
//            }
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//
////        Integer userId = GetCurrentUser.currentId();
////        UserRequestResponse userResponse = userRepository.findUserById(userId);
////        try (Gateway gateway = GatewayHelper.connect(userResponse.getUsername())) {
////
////            Network network = gateway.getNetwork("channel-org");
////            Contract contract = network.getContract("basic");
////
////            byte[] result = contract.submitTransaction("GetAllAssetHistory");
////
////            String assetJson = new String(result, StandardCharsets.UTF_8);
////            JsonNode historyResponse = MAPPER.readTree(assetJson);
////
////            ArrayNode deletedItems = MAPPER.createArrayNode();
////            ArrayNode notDeletedItems = MAPPER.createArrayNode();
////            boolean allAssetsProcessed = false;
////            for (JsonNode entry : historyResponse) {
////                JsonNode isDelete = entry.get("isDelete");
////                JsonNode assetId = entry.get("assetId");
////                if (assetId != null && assetId.asText().equals("ALL_ASSETS")) {
////                    if (!allAssetsProcessed) {
////                        JsonNode valueArray = entry.get("value");
////                        if (valueArray != null && valueArray.isArray()) {
////                            for (JsonNode asset : valueArray) {
////                                JsonNode individualAssetIdNode = asset.get("assetId");
////                                if (individualAssetIdNode != null) {
////                                    String individualAssetId = individualAssetIdNode.asText();
////                                    try {
////                                        // Retrieve the history of each individual asset
////                                        byte[] result1 = contract.submitTransaction("GetAssetHistory", individualAssetId);
////                                        String assetsJson = new String(result1, StandardCharsets.UTF_8);
////                                        JsonNode assetNode = MAPPER.readTree(assetsJson);
////                                        notDeletedItems.add(assetNode);
////                                    } catch (ContractException e) {
////                                        System.err.println("Failed to retrieve asset history for assetId " + individualAssetId + ": " + e.getMessage());
////                                    }
////                                }
////                            }
////                        }
////                        allAssetsProcessed = true;
////                    }
////                } else {
////                    // Process non-"ALL_ASSETS" entries as normal
////                    if (isDelete != null && isDelete.asBoolean()) {
////                        JsonNode valueNode = entry.get("value");
////                        if (valueNode != null) {
////                            deletedItems.add(valueNode);
////                        }
////                    } else {
////                        notDeletedItems.add(entry);
////                    }
////                }
////            }
////
////            ObjectNode result1 = MAPPER.createObjectNode();
////            result1.set("deletedItems", deletedItems);
////            result1.set("notDeletedItems", notDeletedItems);
////
////            return result1;
////
////        } catch (ContractException e) {
////            System.err.println("Failed to evaluate transaction: " + e.getMessage());
////        }
////        catch (Exception e) {
////            e.printStackTrace();
////        }
//
//        return null;
//    }

    @Override
    public JsonNode getAllAssetHistroy() {
        String url = "http://admin:password@127.0.0.1:5985/channel-org_basic/_all_docs?include_docs=true";
        Integer userId = GetCurrentUser.currentId();
        UserRequestResponse userResponse = userRepository.findUserById(userId);

        try (Gateway gateway = GatewayHelper.connect(userResponse.getUsername())) {
            // Get current user's role
            User currentUser = userRepository.findUserByUsername(userResponse.getUsername());
            boolean isAdmin = currentUser.getRoles().equals("ADMIN");

            List<JsonNode> results = new ArrayList<>();

            // Fetch all asset history from CouchDB or chaincode
            URL obj = new URL(url);
            HttpURLConnection connection = (HttpURLConnection) obj.openConnection();
            connection.setRequestMethod("GET");
            connection.setRequestProperty("Accept", "application/json");

            String userCredentials = "admin:password";
            String basicAuth = "Basic " + Base64.getEncoder().encodeToString(userCredentials.getBytes());
            connection.setRequestProperty("Authorization", basicAuth);

            int responseCode = connection.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                StringBuilder response = new StringBuilder();
                String inputLine;

                while ((inputLine = in.readLine()) != null) {
                    response.append(inputLine);
                }
                in.close();

                JsonNode rootNode = MAPPER.readTree(response.toString());
                JsonNode rows = rootNode.get("rows");

                for (JsonNode row : rows) {
                    String id = row.get("id").asText();
                    if (!id.equals("\u0000􏿿initialized")) {
                        Network network = gateway.getNetwork("channel-org");
                        Contract contract = network.getContract("basic");

                        byte[] resultBytes = contract.submitTransaction("GetAssetHistory", id);
                        String assetHistoryJson = new String(resultBytes, StandardCharsets.UTF_8);

                        JsonNode historyResponse = MAPPER.readTree(assetHistoryJson);

                        for (JsonNode historyEntry : historyResponse) {
                            if (!historyEntry.has("asset_id") || historyEntry.get("asset_id").asText().isEmpty()) {
                                continue;
                            }

                            // Add assignTo user details
                            ObjectNode historyWithUserInfo = MAPPER.createObjectNode();
                            String username = historyEntry.get("assign_to").asText();
                            int assignToUserId = Integer.parseInt(username);

                            if (isAdmin || assignToUserId == currentUser.getUserId()) {
                                historyWithUserInfo.setAll((ObjectNode) historyEntry);

                                UserRequestResponse assignedUser = userRepository.findUserById(assignToUserId);
                                if (assignedUser != null) {
                                    ObjectNode userJson = MAPPER.createObjectNode();
                                    userJson.put("userId", assignedUser.getUserId().toString());
                                    userJson.put("fullName", assignedUser.getFullName());
                                    userJson.put("profileImg", assignedUser.getProfileImg());

                                    if (assignedUser.getDepartment() != null) {
                                        userJson.put("department", assignedUser.getDepartment().getDep_name());
                                    } else {
                                        userJson.put("department", "");
                                    }
                                    historyWithUserInfo.set("assignTo", userJson);
                                }

                                results.add(historyWithUserInfo);
                            }
                        }
                    }
                }
                return MAPPER.valueToTree(results);
            } else {
                System.out.println("GET request failed with response code: " + responseCode);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }



}