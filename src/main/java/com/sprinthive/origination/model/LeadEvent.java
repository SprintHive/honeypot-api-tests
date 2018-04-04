package com.sprinthive.origination.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Builder;
import lombok.RequiredArgsConstructor;
import lombok.Value;

@Value
@Builder(toBuilder = true)
@RequiredArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class LeadEvent {
    private String entityId;
    private String entityKey;
    private String type;
    private LeadAggregate payload;
}
