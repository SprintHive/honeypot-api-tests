package com.sprinthive.origination.client;

import com.sprinthive.origination.model.EmploymentType;
import com.sprinthive.origination.model.FileMetadata;
import com.sprinthive.origination.model.LeadAggregate;
import com.sprinthive.origination.model.LeadUpdate;
import org.springframework.cloud.netflix.feign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.util.Map;

@FeignClient(name = "OriginationService")
public interface OriginationServiceClient {
    @RequestMapping(method = RequestMethod.POST, value = "/v1/lead/{leadId}/kycStatus")
    void patchKycStatus(@PathVariable("leadId") String leadId,
                        @RequestHeader("X-ASYNC-TASK-REF-KEY") String asyncRefId,
                        @RequestHeader("X-ASYNC-TASK-REF-VALUE") String asyncRefValue,
                        LeadUpdate leadUpdate);

    @RequestMapping(method = RequestMethod.POST, value = "/v1/lead/{leadId}/asyncTaskRef/{asyncTaskRefId}")
    void patchAsyncTaskRef(@PathVariable("leadId") String leadId,
                           @PathVariable("asyncTaskRefId") String asyncTaskRefId,
                           String value);

    @RequestMapping(method = RequestMethod.POST, value = "/v1/lead/{leadId}/metadata/customState")
    void patchCustomState(@PathVariable("leadId") String leadId,
                          LeadUpdate leadUpdate,
                          @RequestHeader("X-ASYNC-TASK-REF-KEY") String asyncRefId,
                          @RequestHeader("X-ASYNC-TASK-REF-VALUE") String asyncRefValue);

    LeadAggregate createLead(String product);
    LeadAggregate getLead(String leadId);

    void submit(String leadId);
    void setLeadDob(String leadId, LocalDate dateOfBirth);
    void setProduct(String leadId, String product);
    void setIdNumber(String leadId, String idNumber);
    void setLastName(String leadId, String lastName);
    void setMaidenName(String leadId, String maidenName);
    void setMaritalStatus(String leadId, String maritalStatus);
    void setFirstName(String leadId, String firstName);
    void setLoanAmount(String leadId, Double loanAmount);
    void setRepaymentPeriod(String leadId, int repaymentPeriod);
    void setNumDependents(String leadId, int numDependents);
    void setCellNumber(String leadId, String cellNumber);
    void setEmailAddress(String leadId, String emailAddress);
    void setGrossMonthlyIncome(String leadId, Double grossMonthlyIncome);
    void setEmploymentType(String leadId, EmploymentType employmentType);
    void setJobType(String leadId, String jobType);
    void setTermsAndConditionsAccepted(String leadId, boolean accepted);
    void setMonthlyLivingExpense(String leadId, String expense, int value);
    void addProofOfIncome(String leadId, String fileName, byte[] data, FileMetadata.FileType type);
    void removeProofOfIncome(String leadId, String fileId);
    void addProofOfIdentity(String leadId, String fileName, byte[] data, FileMetadata.FileType type);
    void removeProofOfIdentity(String leadId, String fileId);
    void setLatest(String leadId, String idNumber, LeadAggregate lead);
}

