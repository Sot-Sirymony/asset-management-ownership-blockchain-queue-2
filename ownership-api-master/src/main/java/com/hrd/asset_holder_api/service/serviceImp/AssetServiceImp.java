package com.hrd.asset_holder_api.service.serviceImp;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.hrd.asset_holder_api.exception.NotFoundException;
import com.hrd.asset_holder_api.helper.GatewayHelperV1;
import com.hrd.asset_holder_api.model.entity.Asset;
import com.hrd.asset_holder_api.model.entity.User;
import com.hrd.asset_holder_api.model.request.AssetTrasferRequest;
import com.hrd.asset_holder_api.model.response.UserRequestResponse;
import com.hrd.asset_holder_api.repository.UserRepository;
import com.hrd.asset_holder_api.service.AssetService;
import com.hrd.asset_holder_api.utils.GetCurrentUser;
import lombok.AllArgsConstructor;
import org.hyperledger.fabric.gateway.Contract;
import org.hyperledger.fabric.gateway.ContractException;
import org.hyperledger.fabric.gateway.Gateway;
import org.hyperledger.fabric.gateway.Network;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

@Service
@AllArgsConstructor
public class AssetServiceImp implements AssetService {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    private static final String CHAINCODE =
            System.getenv().getOrDefault("FABRIC_CHAINCODE", "basic");

    private static final String COUCHDB_BASE_URL =
            System.getenv().getOrDefault("COUCHDB_BASE_URL", "http://couchdb0:5984");

    private static final String COUCHDB_USER =
            System.getenv().getOrDefault("COUCHDB_USER", "admin");

    private static final String COUCHDB_PASS =
            System.getenv().getOrDefault("COUCHDB_PASS", "password");

    private final UserRepository userRepository;

    private static int i = 10;

    // ---- Fabric helpers: always use GatewayHelperV1 (channel from env) ----
    private Network fabricNetwork(Gateway gateway) {
        return GatewayHelperV1.getNetwork(gateway);
    }

    private Contract fabricContract(Gateway gateway) {
        return fabricNetwork(gateway).getContract(CHAINCODE);
    }

    private UserRequestResponse currentUserResponse() {
        Integer userId = GetCurrentUser.currentId();
        return userRepository.findUserById(userId);
    }

    @Override
    public JsonNode getAssetById(String id) {
        UserRequestResponse user = currentUserResponse();

        try (Gateway gateway = GatewayHelperV1.connect(user.getUsername())) {
            Contract contract = fabricContract(gateway);

            // QUERY => evaluateTransaction
            byte[] result = contract.evaluateTransaction("QueryAsset", id);
            return MAPPER.readTree(new String(result, StandardCharsets.UTF_8));

        } catch (ContractException e) {
            throw new NotFoundException("Asset not found: " + id + " (" + e.getMessage() + ")");
        } catch (Exception e) {
            throw new RuntimeException("Failed to get asset by id: " + id, e);
        }
    }

    @Override
    public JsonNode createAsset(Asset asset) {
        UserRequestResponse user = currentUserResponse();

        try (Gateway gateway = GatewayHelperV1.connect(user.getUsername())) {
            Contract contract = fabricContract(gateway);

            asset.setAssetId("asset" + i);
            asset.setUsername(String.valueOf(asset.getAssignTo()));
            i++;

            // WRITE => submitTransaction
            contract.submitTransaction(
                    "CreateAsset",
                    asset.getAssetId(),
                    asset.getAssetName(),
                    asset.getUnit(),
                    asset.getCondition(),
                    asset.getAttachment(),
                    String.valueOf(asset.getAssignTo()),
                    asset.getUsername(),
                    asset.getDepName(),
                    String.valueOf(asset.getQty())
            );

            // QUERY after create
            byte[] result = contract.evaluateTransaction("QueryAsset", asset.getAssetId());
            return MAPPER.readTree(new String(result, StandardCharsets.UTF_8));

        } catch (Exception e) {
            throw new RuntimeException("Failed to create asset", e);
        }
    }

    @Override
    public JsonNode updateAsset(String id, Asset asset) {
        UserRequestResponse user = currentUserResponse();

        try (Gateway gateway = GatewayHelperV1.connect(user.getUsername())) {
            Contract contract = fabricContract(gateway);

            asset.setUsername(String.valueOf(asset.getAssignTo()));

            contract.submitTransaction(
                    "UpdateAsset",
                    id,
                    asset.getAssetName(),
                    asset.getUnit(),
                    asset.getCondition(),
                    asset.getAttachment(),
                    String.valueOf(asset.getAssignTo()),
                    asset.getUsername(),
                    asset.getDepName(),
                    String.valueOf(asset.getQty())
            );

            byte[] result = contract.evaluateTransaction("QueryAsset", id);
            return MAPPER.readTree(new String(result, StandardCharsets.UTF_8));

        } catch (ContractException e) {
            throw new NotFoundException("Asset not found: " + id + " (" + e.getMessage() + ")");
        } catch (Exception e) {
            throw new RuntimeException("Failed to update asset: " + id, e);
        }
    }

    @Override
    public Boolean deleteAsset(String id) {
        UserRequestResponse user = currentUserResponse();

        try (Gateway gateway = GatewayHelperV1.connect(user.getUsername())) {
            Contract contract = fabricContract(gateway);

            // ensure exists
            contract.evaluateTransaction("QueryAsset", id);

            contract.submitTransaction("DeleteAsset", id);
            return true;

        } catch (ContractException e) {
            throw new NotFoundException("Asset not found: " + id + " (" + e.getMessage() + ")");
        } catch (Exception e) {
            throw new RuntimeException("Failed to delete asset: " + id, e);
        }
    }

    @Override
    public JsonNode getAllAsset() {
        UserRequestResponse userResponse = currentUserResponse();

        try (Gateway gateway = GatewayHelperV1.connect(userResponse.getUsername())) {

            Contract contract = fabricContract(gateway);

            ArrayNode assetsWithUserInfo = MAPPER.createArrayNode();

            UserRequestResponse currentUser = userRepository.findUserById(GetCurrentUser.currentId());
            User currentUserEntity = userRepository.findUserByUsername(currentUser.getUsername());
            boolean isAdmin = "ADMIN".equals(currentUserEntity.getRoles());

            byte[] result = contract.evaluateTransaction("QueryAllAssets");
            JsonNode assetNode = MAPPER.readTree(new String(result, StandardCharsets.UTF_8));

            for (JsonNode asset : assetNode) {
                if (!asset.hasNonNull("asset_id") || asset.get("asset_id").asText().isBlank()) continue;
                if (!asset.hasNonNull("assign_to")) continue;

                int userId = Integer.parseInt(asset.get("assign_to").asText());

                if (!isAdmin && userId != currentUser.getUserId()) continue;

                ObjectNode assetWithUserInfo = MAPPER.createObjectNode();
                assetWithUserInfo.put("assetId", asset.path("asset_id").asText(null));
                assetWithUserInfo.put("assetName", asset.path("asset_name").asText(null));
                assetWithUserInfo.put("qty", asset.path("qty").asText(null));
                assetWithUserInfo.put("condition", asset.path("condition").asText(null));
                assetWithUserInfo.put("attachment", asset.path("attachment").asText(null));
                assetWithUserInfo.put("assignDate", asset.path("created_at").asText(null));
                assetWithUserInfo.put("depName", asset.path("dep_name").asText(null));

                UserRequestResponse assignedUser = userRepository.findUserById(userId);
                if (assignedUser != null) {
                    ObjectNode userJson = MAPPER.createObjectNode();
                    userJson.put("userId", String.valueOf(assignedUser.getUserId()));
                    userJson.put("fullName", assignedUser.getFullName() == null ? "" : assignedUser.getFullName());
                    userJson.put("profileImg", assignedUser.getProfileImg() == null ? "" : assignedUser.getProfileImg());
                    userJson.put("department",
                            assignedUser.getDepartment() != null && assignedUser.getDepartment().getDep_name() != null
                                    ? assignedUser.getDepartment().getDep_name()
                                    : "");
                    assetWithUserInfo.set("assignTo", userJson);
                }

                assetsWithUserInfo.add(assetWithUserInfo);
            }

            return assetsWithUserInfo;

        } catch (Exception e) {
            throw new RuntimeException("Failed to get all assets", e);
        }
    }

    @Override
    public Boolean trasfterAsset(String id, AssetTrasferRequest req) {
        UserRequestResponse user = currentUserResponse();

        try (Gateway gateway = GatewayHelperV1.connect(user.getUsername())) {
            Contract contract = fabricContract(gateway);

            contract.submitTransaction("TransferAsset", id, String.valueOf(req.getNewAssignTo()));
            return true;

        } catch (ContractException e) {
            throw new NotFoundException("Id " + id + " not found (" + e.getMessage() + ")");
        } catch (Exception e) {
            throw new RuntimeException("Failed to transfer asset: " + id, e);
        }
    }

    @Override
    public JsonNode getHistoryById(String id) {
        UserRequestResponse user = currentUserResponse();

        try (Gateway gateway = GatewayHelperV1.connect(user.getUsername())) {
            Contract contract = fabricContract(gateway);

            byte[] result = contract.evaluateTransaction("GetAssetHistory", id);
            return MAPPER.readTree(new String(result, StandardCharsets.UTF_8));

        } catch (ContractException e) {
            throw new NotFoundException("Asset not found: " + id + " (" + e.getMessage() + ")");
        } catch (Exception e) {
            throw new RuntimeException("Failed to get history for asset: " + id, e);
        }
    }

    @Override
    public JsonNode getAllAssetHistroy() {
        String channel = System.getenv().getOrDefault("FABRIC_CHANNEL", "mychannel");
        String couchDbName = channel + "_" + CHAINCODE;
        String url = COUCHDB_BASE_URL + "/" + couchDbName + "/_all_docs?include_docs=true";

        UserRequestResponse userResponse = currentUserResponse();

        try (Gateway gateway = GatewayHelperV1.connect(userResponse.getUsername())) {

            User currentUser = userRepository.findUserByUsername(userResponse.getUsername());
            boolean isAdmin = "ADMIN".equals(currentUser.getRoles());

            // ---- 1) Read all docs from CouchDB ----
            HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
            connection.setRequestMethod("GET");
            connection.setRequestProperty("Accept", "application/json");

            String credentials = COUCHDB_USER + ":" + COUCHDB_PASS;
            String basicAuth = "Basic " + Base64.getEncoder()
                    .encodeToString(credentials.getBytes(StandardCharsets.UTF_8));
            connection.setRequestProperty("Authorization", basicAuth);

            int code = connection.getResponseCode();
            if (code != HttpURLConnection.HTTP_OK) {
                throw new RuntimeException("CouchDB request failed: " + code + " url=" + url);
            }

            StringBuilder sb = new StringBuilder();
            try (BufferedReader br = new BufferedReader(
                    new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8))) {
                String line;
                while ((line = br.readLine()) != null) sb.append(line);
            }

            JsonNode rows = MAPPER.readTree(sb.toString()).path("rows");

            // ---- 2) Query Fabric history using correct channel/chaincode ----
            Contract contract = fabricContract(gateway);
            List<JsonNode> results = new ArrayList<>();

            for (JsonNode row : rows) {
                String id = row.path("id").asText(null);
                if (id == null || id.isBlank()) continue;

                // Skip couchdb system docs like "\u0000...."
                if (id.charAt(0) == '\u0000') continue;

                byte[] resultBytes = contract.evaluateTransaction("GetAssetHistory", id);
                JsonNode history = MAPPER.readTree(new String(resultBytes, StandardCharsets.UTF_8));

                for (JsonNode entry : history) {
                    if (!entry.hasNonNull("asset_id") || entry.get("asset_id").asText().isBlank()) continue;
                    if (!entry.hasNonNull("assign_to")) continue;

                    int assignToUserId = Integer.parseInt(entry.get("assign_to").asText());
                    if (!isAdmin && assignToUserId != currentUser.getUserId()) continue;

                    ObjectNode out = MAPPER.createObjectNode();
                    out.setAll((ObjectNode) entry);

                    UserRequestResponse assignedUser = userRepository.findUserById(assignToUserId);
                    if (assignedUser != null) {
                        ObjectNode userJson = MAPPER.createObjectNode();
                        userJson.put("userId", String.valueOf(assignedUser.getUserId()));
                        userJson.put("fullName", assignedUser.getFullName() == null ? "" : assignedUser.getFullName());
                        userJson.put("profileImg", assignedUser.getProfileImg() == null ? "" : assignedUser.getProfileImg());
                        userJson.put("department",
                                assignedUser.getDepartment() != null && assignedUser.getDepartment().getDep_name() != null
                                        ? assignedUser.getDepartment().getDep_name()
                                        : "");
                        out.set("assignTo", userJson);
                    }

                    results.add(out);
                }
            }

            return MAPPER.valueToTree(results);

        } catch (Exception e) {
            throw new RuntimeException("Failed to get all asset history", e);
        }
    }
}
