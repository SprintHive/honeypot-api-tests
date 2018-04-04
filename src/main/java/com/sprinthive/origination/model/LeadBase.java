package com.sprinthive.origination.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Version;

import java.time.Instant;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public abstract class LeadBase {
    @Id
    protected String leadId;
    @Version
    protected Long version;
    protected String idNumber;
    protected String product;
    protected Instant creationDate;
    protected LocalDate dateOfBirth;
    protected String firstName;
    protected String lastName;
    protected String maidenName;
    protected String maritalStatus;
    protected Integer age;
    protected Double loanAmount;
    protected Integer repaymentPeriod;
    protected Integer numDependents;
    protected String cellNumber;
    protected String emailAddress;
    protected Double grossMonthlyIncome;
    protected EmploymentType employmentType;
    protected String jobType;
    protected Boolean termsAndConditionsAccepted;
    protected Map<String, Integer> monthlyLivingExpenses;
    protected List<FileMetadata> proofOfIncome;
    protected List<FileMetadata> proofOfIdentity;
    protected Map<String, String> asyncTaskRefs;
    protected Map<String, Map<String, String>> productData;
    protected LeadMetadata metadata;

    public synchronized void putCustomState(String key, JsonNode value){
        LeadMetadata metadata = getMetadata();
        if(metadata == null){
            metadata = LeadMetadata.builder().customState(new HashMap<>()).build();
            setMetadata(metadata);
        }
        metadata.putCustomState(key, value);
    }

    private synchronized Map<String, String> ensureAsyncTaskRef(){
        Map<String, String> asyncTaskRefs = getAsyncTaskRefs();
        if (asyncTaskRefs == null) {
            asyncTaskRefs = new HashMap<>();
            setAsyncTaskRefs(asyncTaskRefs);
        }
        return asyncTaskRefs;
    }

    public synchronized void putAsyncTaskRef(String key, String value){
        ensureAsyncTaskRef().put(key, value);
    }

    public synchronized void putAllAsyncTaskRefs(Map<String, String> asyncTaskRefs) {
        ensureAsyncTaskRef().putAll(asyncTaskRefs);
    }
}
