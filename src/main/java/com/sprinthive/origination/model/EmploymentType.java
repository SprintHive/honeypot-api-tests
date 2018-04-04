package com.sprinthive.origination.model;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class EmploymentType {
    private String type;
    private String subType;
}
