package com.sprinthive.origination.client;

import feign.RequestInterceptor;
import feign.RequestTemplate;

import java.util.Map;

public class HeaderInjectingRequestInterceptor implements RequestInterceptor {
    private Map<String, String> headersToInject;

    public HeaderInjectingRequestInterceptor(Map<String, String> headersToInject) {
        this.headersToInject = headersToInject;
    }

    @Override
    public void apply(RequestTemplate template) {
        for (Map.Entry<String, String> header : headersToInject.entrySet()) {
            template.header(header.getKey(), header.getValue());
        }
    }
}
