package com.sprinthive.origination.model;

import java.util.Map;

public interface RequestMetadata {
    String getAbTestingKey();

    void setAbTestingKey(String abTestingKey);

    Map<String, String> getHeaders();

    void setHeaders(Map<String, String> headers);
}
