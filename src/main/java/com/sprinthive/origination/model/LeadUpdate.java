package com.sprinthive.origination.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Builder;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@JsonIgnoreProperties(ignoreUnknown = true)
@NoArgsConstructor
public class LeadUpdate extends LeadBase {

    @Builder(toBuilder = true)

    public LeadUpdate(String leadId, Long version, String idNumber, String product, Instant creationDate,
                         LocalDate dateOfBirth, String firstName, String lastName, String maidenName, String maritalStatus,
                         Integer age, Double loanAmount, Integer repaymentPeriod, Integer numDependents, String cellNumber,
                         String emailAddress, Double grossMonthlyIncome, EmploymentType employmentType, String jobType,
                         Boolean termsAndConditionsAccepted, Map<String, Integer> monthlyLivingExpenses,
                         List<FileMetadata> proofOfIncome, List<FileMetadata> proofOfIdentity, Map<String, String> asyncTaskRefs,
                         Map<String, Map<String, String>> productData,
                         LeadMetadata metadata) {
        super(leadId, version, idNumber, product, creationDate, dateOfBirth, firstName, lastName, maidenName,
                maritalStatus, age, loanAmount, repaymentPeriod, numDependents, cellNumber, emailAddress,
                grossMonthlyIncome, employmentType, jobType, termsAndConditionsAccepted, monthlyLivingExpenses,
                proofOfIncome, proofOfIdentity, asyncTaskRefs, productData, metadata);
    }
}
