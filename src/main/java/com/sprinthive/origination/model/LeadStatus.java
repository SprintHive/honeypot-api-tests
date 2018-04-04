package com.sprinthive.origination.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder(toBuilder = true)
@JsonIgnoreProperties(ignoreUnknown = true)
public class LeadStatus {
    private String statusCode;
    private List<LeadProblem> problems;
}
