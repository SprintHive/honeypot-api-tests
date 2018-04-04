package com.sprinthive.origination.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.Builder;
import lombok.Data;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Data
@Builder(toBuilder = true)
@JsonIgnoreProperties(ignoreUnknown = true)
public class LeadMetadata {
    private LeadStatus leadStatus;
    private String kycStatusCode;
    private Map<String, JsonNode> customState;
    private Boolean locked;
    private String abTestingKey;

    public synchronized void putCustomState(String key, JsonNode value) {
        Map<String, JsonNode> customState = getCustomState();
        if(customState == null){
            customState = new HashMap<>();
            setCustomState(customState);
        }
        customState.put(key, value);

    }
}
