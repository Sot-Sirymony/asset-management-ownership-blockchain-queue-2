package com.hrd.asset_holder_api.service.serviceImp;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.hrd.asset_holder_api.exception.NotFoundException;
import com.hrd.asset_holder_api.helper.GatewayHelperV1;
import com.hrd.asset_holder_api.model.entity.ReportIssue;
import com.hrd.asset_holder_api.model.entity.User;
import com.hrd.asset_holder_api.model.response.UserRequestResponse;
import com.hrd.asset_holder_api.repository.UserRepository;
import com.hrd.asset_holder_api.service.ReportIssueService;
import com.hrd.asset_holder_api.utils.GetCurrentUser;
import lombok.AllArgsConstructor;
import org.hyperledger.fabric.gateway.Contract;
import org.hyperledger.fabric.gateway.ContractException;
import org.hyperledger.fabric.gateway.Gateway;
import org.hyperledger.fabric.gateway.Network;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;

@Service
@AllArgsConstructor
public class ReportIssueServiceImp implements ReportIssueService {
    private static final ObjectMapper MAPPER = new ObjectMapper();
    private final UserRepository userRepository;
    private static int i = 1;

    @Override
    public Boolean createIssue(ReportIssue reportIssue) {
        Integer userId = GetCurrentUser.currentId();
        UserRequestResponse user = userRepository.findUserById(userId);

        reportIssue.setReportId("report00"+i);
        i++;
        try (Gateway gateway = GatewayHelperV1.connect(user.getUsername())) {
            Network network = gateway.getNetwork("channel-org");
            Contract contract = network.getContract("basic");
            byte[] result = contract.submitTransaction("QueryAllAssets");

            String assetJson = new String(result, StandardCharsets.UTF_8);
            JsonNode assetNode = MAPPER.readTree(assetJson);
            boolean assetFound = false;
            if (assetNode.isArray()) {
                for (JsonNode asset : assetNode) {
                    String assetName = asset.get("asset_name").asText();
                    if (assetName.equals(reportIssue.getAssetName())) {
                        String assetId = asset.get("asset_id").asText();
                        reportIssue.setAssetId(assetId);
                        assetFound = true;
                        break;
                    }
                }
            }

            if (!assetFound) {
                System.err.println("No matching asset name found.");
                throw new NotFoundException("No matching asset name found.");
//                return false;
            }


            contract.submitTransaction("CreateReportIssue",
                    reportIssue.getReportId(),
                    reportIssue.getAssetId().toString(),
                    reportIssue.getAssetName(),
                    reportIssue.getProblem(),
                    reportIssue.getAttachment(),
                    userId.toString(),
                    user.getUsername()
            );
            return true;
        } catch (ContractException e) {
            System.err.println("Failed to evaluate transaction: " + e.getMessage());
//            throw new NotFoundException(e.getMessage());
            return false;
        }
        catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    @Override
    public Boolean deleteIssue(String id) {
        Integer userId = GetCurrentUser.currentId();
        UserRequestResponse user = userRepository.findUserById(userId);
        try (Gateway gateway = GatewayHelperV1.connect(user.getUsername())) {
            Network network = gateway.getNetwork("channel-org");
            Contract contract = network.getContract("basic");

            contract.submitTransaction("QueryReportIssue", id);
            contract.submitTransaction("DeleteReportIssue", id);

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
    public Boolean updateIssue(String id, ReportIssue reportIssue) {
        Integer userId = GetCurrentUser.currentId();
        UserRequestResponse user = userRepository.findUserById(userId);
        try (Gateway gateway = GatewayHelperV1.connect(user.getUsername())) {
            Network network = gateway.getNetwork("channel-org");
            Contract contract = network.getContract("basic");
            contract.submitTransaction("UpdateReportIssue",
                    id,
                    reportIssue.getAssetName(),
                    reportIssue.getProblem(),
                    reportIssue.getAttachment(),
                    userId.toString(),
                    user.getUsername()
            );

            return true;

        } catch (ContractException e) {
            System.err.println("Failed to evaluate transaction: " + e.getMessage());
        }
        catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    @Override
    public JsonNode getIssueById(String id) {
        Integer userId = GetCurrentUser.currentId();
        UserRequestResponse userResponse = userRepository.findUserById(userId);
        try (Gateway gateway = GatewayHelperV1.connect(userResponse.getUsername())) {

            Network network = gateway.getNetwork("channel-org");
            Contract contract = network.getContract("basic");

            byte[] result = contract.submitTransaction("QueryReportIssue", id);

            String assetJson = new String(result, StandardCharsets.UTF_8);
            JsonNode assetNode = MAPPER.readTree(assetJson);

            ArrayNode assetsWithUserInfo = MAPPER.createArrayNode();
            ObjectNode assetWithUserInfo = MAPPER.createObjectNode();

            //check user to display data
            Integer currentUserId = GetCurrentUser.currentId();
            UserRequestResponse currentUser = userRepository.findUserById(currentUserId);
            User userRoles = userRepository.findUserByUsername(currentUser.getUsername());
            boolean isAdmin = userRoles.getRoles().equals("ADMIN");

            assetWithUserInfo.put("assetName", assetNode.get("asset_name").asText());
            assetWithUserInfo.put("attachment", assetNode.get("attachment").asText());
            assetWithUserInfo.put("assignDate", assetNode.get("created_at").asText());

            String username = assetNode.get("username").asText();
            UserRequestResponse user = userRepository.findUserByName(username);
            if (isAdmin || username.equals(currentUser.getUsername())) {

                if (user != null) {
                    ObjectNode userJson = MAPPER.createObjectNode();
                    userJson.put("fullName", user.getFullName());
                    userJson.put("profileImg", user.getProfileImg());
                    userJson.put("email", user.getEmail());
                    assetWithUserInfo.set("username", userJson);
                }
                assetsWithUserInfo.add(assetWithUserInfo);
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
    public JsonNode getAllIssue() {
        Integer userId = GetCurrentUser.currentId();
        UserRequestResponse userResponse = userRepository.findUserById(userId);
        try (Gateway gateway = GatewayHelperV1.connect(userResponse.getUsername())) {

            Network network = gateway.getNetwork("channel-org");
            Contract contract = network.getContract("basic");

            byte[] result = contract.submitTransaction("QueryAllReportIssues");

            String assetJson = new String(result, StandardCharsets.UTF_8);
            JsonNode assetNode = MAPPER.readTree(assetJson);
            ArrayNode assetsWithUserInfo = MAPPER.createArrayNode();

            //check user to display data
            Integer currentUserId = GetCurrentUser.currentId();
            UserRequestResponse currentUser = userRepository.findUserById(currentUserId);
            User userRoles = userRepository.findUserByUsername(currentUser.getUsername());
            boolean isAdmin = userRoles.getRoles().equals("ADMIN");

            for (JsonNode asset : assetNode) {
                String username = asset.get("username").asText();
                if (isAdmin || username.equals(currentUser.getUsername())) {
                    System.out.println("username +"+username);
                    System.out.println("currentUser "+currentUser.getUsername());
                    ObjectNode assetWithUserInfo = MAPPER.createObjectNode();
                    assetWithUserInfo.put("reportId", asset.get("report_id").asText());
                    assetWithUserInfo.put("assetName", asset.has("asset_name") ? asset.get("asset_name").asText() : null);
                    assetWithUserInfo.put("attachment", asset.has("attachment") ? asset.get("attachment").asText() : null);
                    assetWithUserInfo.put("problem", asset.has("problem") ? asset.get("problem").asText() : null);
                    assetWithUserInfo.put("assignDate", asset.has("created_at") ? asset.get("created_at").asText() : null);

                    System.out.println("username +" + username);
                    UserRequestResponse user = userRepository.findUserByName(username);

                    if (user != null) {
                        ObjectNode userJson = MAPPER.createObjectNode();
                        userJson.put("fullName", user.getFullName());
                        userJson.put("profileImg", user.getProfileImg());
                        userJson.put("email", user.getEmail());
                        userJson.put("department", user.getDepartment().getDep_name());
                        assetWithUserInfo.set("username", userJson);
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


}
