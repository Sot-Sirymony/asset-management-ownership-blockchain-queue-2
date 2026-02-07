package com.hrd.asset_holder_api.model.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class Dashboard {
    public Integer totalUser;
    public Integer totalAssetRequest;
    public Integer totalReportIssue;
    public Integer totalDepartment;

}
