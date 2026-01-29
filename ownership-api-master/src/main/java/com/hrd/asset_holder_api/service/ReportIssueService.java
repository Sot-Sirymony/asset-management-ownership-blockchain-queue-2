package com.hrd.asset_holder_api.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.hrd.asset_holder_api.model.entity.ReportIssue;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface ReportIssueService {
    Boolean createIssue(ReportIssue reportIssue);

    Boolean deleteIssue(String id);

    Boolean updateIssue(String id, ReportIssue reportIssue);

    JsonNode getIssueById(String id);

    JsonNode getAllIssue();
}
