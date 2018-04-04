package com.sprinthive.origination.client;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.sprinthive.origination.model.EmploymentType;
import com.sprinthive.origination.model.FileMetadata;
import com.sprinthive.origination.model.LeadAggregate;
import com.sprinthive.origination.model.LeadUpdate;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.net.URI;
import java.time.LocalDate;
import java.util.Collections;
import java.util.Map;

public class RestTemplateOriginationV1Client implements OriginationServiceClient {
    public static final String CONTEXT_PATH = "/v1/lead";

    private RestTemplate restTemplate;
    private String serviceUrl;
    private String originationV1Endpoint;

    public RestTemplateOriginationV1Client(RestTemplate restTemplate, String serviceUrl) {
       this.restTemplate = restTemplate;
       this.serviceUrl = serviceUrl;
       this.originationV1Endpoint = serviceUrl + CONTEXT_PATH;
    }

    @Override
    public void patchKycStatus(String leadId, String asyncRefId, String asyncRefValue, LeadUpdate leadUpdate) {
        MultiValueMap<String, String> headers = new LinkedMultiValueMap<>();
        headers.put("X-ASYNC-TASK-REF-KEY", Collections.singletonList(asyncRefId));
        headers.put("X-ASYNC-TASK-REF-VALUE", Collections.singletonList(asyncRefValue));

        HttpEntity<LeadUpdate> customStatePatch = new HttpEntity<>(leadUpdate, headers);

        URI uri = URI.create(String.format("%s/%s/kycStatus", originationV1Endpoint, leadId));
        restTemplate.postForEntity(uri, customStatePatch, String.class);
    }

    @Override
    public void patchAsyncTaskRef(String leadId, String asyncTaskRefId, String value) {
        URI uri = URI.create(String.format("%s/%s/asyncTaskRef/%s", originationV1Endpoint, leadId, asyncTaskRefId));
        restTemplate.postForEntity(uri, value, String.class);
    }

    @Override
    public void patchCustomState(String leadId, LeadUpdate leadUpdate, String asyncRefId, String asyncRefValue) {
        MultiValueMap<String, String> headers = new LinkedMultiValueMap<>();
        headers.put("X-ASYNC-TASK-REF-KEY", Collections.singletonList(asyncRefId));
        headers.put("X-ASYNC-TASK-REF-VALUE", Collections.singletonList(asyncRefValue));

        HttpEntity<LeadUpdate> customStatePatch = new HttpEntity<>(leadUpdate, headers);

        URI uri = URI.create(String.format("%s/%s/metadata/customState", originationV1Endpoint, leadId));
        restTemplate.postForEntity(uri, customStatePatch, String.class);
    }

    @Override
    public LeadAggregate createLead(String product) {
        URI uri = URI.create(String.format("%s", originationV1Endpoint));
        return restTemplate.postForEntity(uri, LeadAggregate.builder().product(product).build(), LeadAggregate.class).getBody();
    }

    @Override
    public LeadAggregate getLead(String leadId) {
        URI uri = URI.create(String.format("%s/%s", originationV1Endpoint, leadId));
        return restTemplate.getForEntity(uri, LeadAggregate.class).getBody();
    }

    @Override
    public void submit(String leadId) {
        URI uri = URI.create(String.format("%s/%s/submit", originationV1Endpoint, leadId));
        restTemplate.postForEntity(uri, null, String.class);
    }

    @Override
    public void setLeadDob(String leadId, LocalDate dateOfBirth) {
        URI uri = URI.create(String.format("%s/%s/dateOfBirth", originationV1Endpoint, leadId));
        restTemplate.postForEntity(uri, LeadAggregate.builder().dateOfBirth(dateOfBirth).build(), String.class);
    }

    @Override
    public void setProduct(String leadId, String product) {
        URI uri = URI.create(String.format("%s/%s/product", originationV1Endpoint, leadId));
        restTemplate.postForEntity(uri, LeadAggregate.builder().product(product).build(), String.class);
    }

    @Override
    public void setIdNumber(String leadId, String idNumber) {
        URI uri = URI.create(String.format("%s/%s/idNumber", originationV1Endpoint, leadId));
        restTemplate.postForEntity(uri, LeadAggregate.builder().idNumber(idNumber).build(), String.class);
    }

    @Override
    public void setLastName(String leadId, String lastName) {
        URI uri = URI.create(String.format("%s/%s/lastName", originationV1Endpoint, leadId));
        restTemplate.postForEntity(uri, LeadAggregate.builder().lastName(lastName).build(), String.class);
    }

    @Override
    public void setMaidenName(String leadId, String maidenName) {
        URI uri = URI.create(String.format("%s/%s/maidenName", originationV1Endpoint, leadId));
        restTemplate.postForEntity(uri, LeadAggregate.builder().maidenName(maidenName).build(), String.class);
    }

    @Override
    public void setMaritalStatus(String leadId, String maritalStatus) {
        URI uri = URI.create(String.format("%s/%s/maritalStatus", originationV1Endpoint, leadId));
        restTemplate.postForEntity(uri, LeadAggregate.builder().maritalStatus(maritalStatus).build(), String.class);
    }

    @Override
    public void setFirstName(String leadId, String firstName) {
        URI uri = URI.create(String.format("%s/%s/firstName", originationV1Endpoint, leadId));
        restTemplate.postForEntity(uri, LeadAggregate.builder().firstName(firstName).build(), String.class);
    }

    @Override
    public void setLoanAmount(String leadId, Double loanAmount) {
        URI uri = URI.create(String.format("%s/%s/loanAmount", originationV1Endpoint, leadId));
        restTemplate.postForEntity(uri, LeadAggregate.builder().loanAmount(loanAmount).build(), String.class);
    }

    @Override
    public void setRepaymentPeriod(String leadId, int repaymentPeriod) {
        URI uri = URI.create(String.format("%s/%s/repaymentPeriod", originationV1Endpoint, leadId));
        restTemplate.postForEntity(uri, LeadAggregate.builder().repaymentPeriod(repaymentPeriod).build(), String.class);
    }

    @Override
    public void setNumDependents(String leadId, int numDependents) {
        URI uri = URI.create(String.format("%s/%s/numDependents", originationV1Endpoint, leadId));
        restTemplate.postForEntity(uri, LeadAggregate.builder().numDependents(numDependents).build(), String.class);
    }

    @Override
    public void setCellNumber(String leadId, String cellNumber) {
        URI uri = URI.create(String.format("%s/%s/cellNumber", originationV1Endpoint, leadId));
        restTemplate.postForEntity(uri, LeadAggregate.builder().cellNumber(cellNumber).build(), String.class);
    }

    @Override
    public void setEmailAddress(String leadId, String emailAddress) {
        URI uri = URI.create(String.format("%s/%s/emailAddress", originationV1Endpoint, leadId));
        restTemplate.postForEntity(uri, LeadAggregate.builder().emailAddress(emailAddress).build(), String.class);
    }

    @Override
    public void setGrossMonthlyIncome(String leadId, Double grossMonthlyIncome) {
        URI uri = URI.create(String.format("%s/%s/grossMonthlyIncome", originationV1Endpoint, leadId));
        restTemplate.postForEntity(uri, LeadAggregate.builder().grossMonthlyIncome(grossMonthlyIncome).build(), String.class);
    }

    @Override
    public void setEmploymentType(String leadId, EmploymentType employmentType) {
        URI uri = URI.create(String.format("%s/%s/employmentType", originationV1Endpoint, leadId));
        restTemplate.postForEntity(uri, LeadAggregate.builder().employmentType(employmentType).build(), String.class);
    }

    @Override
    public void setJobType(String leadId, String jobType) {
        URI uri = URI.create(String.format("%s/%s/jobType", originationV1Endpoint, leadId));
        restTemplate.postForEntity(uri, LeadAggregate.builder().jobType(jobType).build(), String.class);
    }

    @Override
    public void setTermsAndConditionsAccepted(String leadId, boolean accepted) {
        URI uri = URI.create(String.format("%s/%s/termsAndConditionsAccepted", originationV1Endpoint, leadId));
        restTemplate.postForEntity(uri, LeadAggregate.builder().termsAndConditionsAccepted(accepted).build(), String.class);
    }

    @Override
    public void setMonthlyLivingExpense(String leadId, String expense, int value) {
        URI uri = URI.create(String.format("%s/%s/monthlyLivingExpense/%s", originationV1Endpoint, leadId, expense));
        restTemplate.postForEntity(uri, JsonNodeFactory.instance.objectNode().put("value", value), String.class);
    }

    @Override
    public void addProofOfIncome(String leadId, String fileName, byte[] data, FileMetadata.FileType type) {
        MultiValueMap<String, Object> paramsMap = new LinkedMultiValueMap<>();
        ByteArrayResource fileResource = new ByteArrayResource(data){
            @Override
            public String getFilename(){
                return fileName;
            }
        };
        paramsMap.add("file", fileResource);
        paramsMap.add("type", type.getTypeCode());

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);

        HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(paramsMap, headers);
        URI uri = URI.create(String.format("%s/%s/proofOfIncome/upload/%s", originationV1Endpoint, leadId, type.getTypeCode()));
        restTemplate.postForObject(uri, requestEntity, String.class);
    }

    @Override
    public void removeProofOfIncome(String leadId, String fileId) {
        URI uri = URI.create(String.format("%s/%s/proofOfIncome/%s", originationV1Endpoint, leadId, fileId));
        restTemplate.delete(uri);
    }

    @Override
    public void addProofOfIdentity(String leadId, String fileName, byte[] data, FileMetadata.FileType type) {
        MultiValueMap<String, Object> paramsMap = new LinkedMultiValueMap<>();
        ByteArrayResource fileResource = new ByteArrayResource(data){
            @Override
            public String getFilename(){
                return fileName;
            }
        };
        paramsMap.add("file", fileResource);
        paramsMap.add("type", type.getTypeCode());

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);

        HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(paramsMap, headers);
        URI uri = URI.create(String.format("%s/%s/proofOfIdentity/upload/%s", originationV1Endpoint, leadId, type.getTypeCode()));
        restTemplate.postForObject(uri, requestEntity, String.class);

    }

    @Override
    public void removeProofOfIdentity(String leadId, String fileId) {
        URI uri = URI.create(String.format("%s/%s/proofOfIdentity/%s", originationV1Endpoint, leadId, fileId));
        restTemplate.delete(uri);
    }

    @Override
    public void setLatest(String leadId, String idNumber, LeadAggregate lead) {
        URI uri = URI.create(String.format("%s/%s/setLatestByIdNumber/%s", originationV1Endpoint, leadId, idNumber));
        restTemplate.postForEntity(uri, lead, String.class);
    }
}
