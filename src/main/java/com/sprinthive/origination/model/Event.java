package com.sprinthive.origination.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class Event {
    private String entityId;
    private String entityKey;
    private String type;
    private JsonNode payload;
}
