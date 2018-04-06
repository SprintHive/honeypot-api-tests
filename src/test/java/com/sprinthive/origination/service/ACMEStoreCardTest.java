package com.sprinthive.origination.service;

import com.sprinthive.origination.client.OriginationServiceClient;
import com.sprinthive.origination.client.RestTemplateOriginationV1Client;
import com.sprinthive.origination.model.FileMetadata;
import com.sprinthive.origination.model.LeadAggregate;
import com.sprinthive.origination.model.LeadMetadata;
import com.sprinthive.origination.model.LeadStatus;
import org.junit.Before;
import org.junit.Test;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.nio.ByteBuffer;
import java.time.Instant;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

import static org.assertj.core.api.Assertions.fail;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class ACMEStoreCardTest {
    private OriginationServiceClient originationService;

    private static String productName = "ACMEStoreCard";

    @Before
    public void setup() {
        originationService = new RestTemplateOriginationV1Client(new RestTemplate(), "http://127.0.0.1:8080");
    }

    // Condition match sets state to success
    @Test
    public void testKycSuccessWhenRequiredFieldsPresent() throws Exception {
        LeadAggregate lead = originationService.createLead(productName);
        String stateKey = "kyc";

        originationService.setLastName(lead.getLeadId(), "Jimmy");
        originationService.setFirstName(lead.getLeadId(), "Little");
        originationService.setIdNumber(lead.getLeadId(), "7709125081078");

        Thread.sleep(1200);

        lead = originationService.getLead(lead.getLeadId());
        assertEquals("success", lead.getMetadata().getCustomState().get(stateKey).get("statusCode").asText());
        assertNotNull(lead.getMetadata().getCustomState().get(stateKey).get("refId").asText());
        assertNotNull(lead.getMetadata().getCustomState().get(stateKey).get("lastUpdated").asText());
    }

    // Condition criteria field updated resets evaluation
    @Test
    public void testKycStatusResetAfterDependentFieldUpdated() throws Exception {
        LeadAggregate lead = originationService.createLead(productName);
        String stateKey = "kyc";

        originationService.setLastName(lead.getLeadId(), "Jimmy");
        originationService.setFirstName(lead.getLeadId(), "Little");
        originationService.setIdNumber(lead.getLeadId(), "7709125081078");

        Thread.sleep(1200);

        lead = originationService.getLead(lead.getLeadId());
        assertEquals("success", lead.getMetadata().getCustomState().get(stateKey).get("statusCode").asText());
        assertNotNull(lead.getMetadata().getCustomState().get(stateKey).get("refId").asText());
        assertNotNull(lead.getMetadata().getCustomState().get(stateKey).get("lastUpdated").asText());

        originationService.setLastName(lead.getLeadId(), "Jimmy");
        lead = originationService.getLead(lead.getLeadId());
        assertEquals("pending", lead.getMetadata().getCustomState().get(stateKey).get("statusCode").asText());
    }

    // A failed state propagates the failure to dependant state evaluations
    @Test
    public void testApplicationDeniedAfterAffordabilityFails() throws Exception {
        LeadAggregate lead = originationService.createLead(productName);

        originationService.setLastName(lead.getLeadId(), "Jimmy");
        originationService.setFirstName(lead.getLeadId(), "Little");
        originationService.setIdNumber(lead.getLeadId(), "7709125081078");
        originationService.setGrossMonthlyIncome(lead.getLeadId(), 1000D);
        originationService.addProofOfIncome(lead.getLeadId(), "bankStatement.pdf",
                ByteBuffer.allocate(0).array(), FileMetadata.FileType.BANK_STATEMENT);
        originationService.setMonthlyLivingExpense(lead.getLeadId(), "rent", 8000);
        originationService.setRepaymentPeriod(lead.getLeadId(), 6);
        originationService.setLoanAmount(lead.getLeadId(),  10000D);
        originationService.setMaritalStatus(lead.getLeadId(), "not-married");

        Thread.sleep(1200);

        lead = originationService.getLead(lead.getLeadId());
        assertEquals("denied", lead.getMetadata().getLeadStatus().getStatusCode());
        assertEquals("pending", lead.getMetadata().getCustomState().get("contract").get("statusCode").asText());
        assertEquals("success", lead.getMetadata().getCustomState().get("kyc").get("statusCode").asText());
        assertEquals("verified", lead.getMetadata().getCustomState().get("creditProfileVerification").get("statusCode").asText());
        assertEquals("verified", lead.getMetadata().getCustomState().get("incomeVerification").get("statusCode").asText());
        assertEquals("failed", lead.getMetadata().getCustomState().get("affordability").get("statusCode").asText());
    }

    // An update resets any states that depend on the updated field and the reset is propagated to dependent states
    @Test
    public void testApplicationCanBeApprovedAfterInitiallyBeingDenied() throws Exception {
        LeadAggregate lead = originationService.createLead(productName);

        originationService.setLastName(lead.getLeadId(), "Jimmy");
        originationService.setFirstName(lead.getLeadId(), "Little");
        originationService.setIdNumber(lead.getLeadId(), "7709125081078");
        originationService.setGrossMonthlyIncome(lead.getLeadId(), 1000D);
        originationService.addProofOfIncome(lead.getLeadId(), "bankStatement.pdf",
                ByteBuffer.allocate(0).array(), FileMetadata.FileType.BANK_STATEMENT);
        originationService.addProofOfIdentity(lead.getLeadId(), "id.pdf",
                ByteBuffer.allocate(0).array(), FileMetadata.FileType.DRIVERS_LICENSE);
        originationService.setRepaymentPeriod(lead.getLeadId(), 6);
        originationService.setLoanAmount(lead.getLeadId(),  10000D);
        originationService.setMaritalStatus(lead.getLeadId(), "not-married");
        originationService.setMonthlyLivingExpense(lead.getLeadId(), "rent", 7000);

        Thread.sleep(1500);

        lead = originationService.getLead(lead.getLeadId());
        assertEquals("denied", lead.getMetadata().getLeadStatus().getStatusCode());
        assertEquals("failed", lead.getMetadata().getCustomState().get("affordability").get("statusCode").asText());

        originationService.setGrossMonthlyIncome(lead.getLeadId(), 100000D);

        Thread.sleep(1500);

        lead = originationService.getLead(lead.getLeadId());
        assertEquals("approved", lead.getMetadata().getLeadStatus().getStatusCode());
        assertEquals("generated", lead.getMetadata().getCustomState().get("contract").get("statusCode").asText());
        assertEquals("verified", lead.getMetadata().getCustomState().get("affordability").get("statusCode").asText());
    }

    // An update resets any states that depend on the updated field and the reset is propagated to dependent states
    @Test
    public void testApplicationPendingAfterPostApprovalUpdate() throws Exception {
        LeadAggregate lead = originationService.createLead(productName);

        originationService.setLastName(lead.getLeadId(), "Jimmy");
        originationService.setFirstName(lead.getLeadId(), "Little");
        originationService.setIdNumber(lead.getLeadId(), "7709125081078");
        originationService.setGrossMonthlyIncome(lead.getLeadId(), 30000D);
        originationService.addProofOfIncome(lead.getLeadId(), "bankStatement.pdf",
                ByteBuffer.allocate(0).array(), FileMetadata.FileType.BANK_STATEMENT);
        originationService.addProofOfIdentity(lead.getLeadId(), "id.pdf",
                ByteBuffer.allocate(0).array(), FileMetadata.FileType.DRIVERS_LICENSE);
        originationService.setRepaymentPeriod(lead.getLeadId(), 6);
        originationService.setLoanAmount(lead.getLeadId(),  10000D);
        originationService.setMaritalStatus(lead.getLeadId(), "not-married");
        originationService.setMonthlyLivingExpense(lead.getLeadId(), "rent", 7000);

        Thread.sleep(2000);

        lead = originationService.getLead(lead.getLeadId());
        assertEquals("approved", lead.getMetadata().getLeadStatus().getStatusCode());
        assertEquals("verified", lead.getMetadata().getCustomState().get("incomeVerification").get("statusCode").asText());
        assertEquals("verified", lead.getMetadata().getCustomState().get("affordability").get("statusCode").asText());
        assertEquals("approved", lead.getMetadata().getCustomState().get("creditDecision").get("statusCode").asText());
        assertEquals("generated", lead.getMetadata().getCustomState().get("contract").get("statusCode").asText());
        assertNotNull(lead.getMetadata().getCustomState().get("contract").get("evidenceId").asText());

        originationService.setGrossMonthlyIncome(lead.getLeadId(),50000D);

        lead = originationService.getLead(lead.getLeadId());

        assertEquals("pending", lead.getMetadata().getLeadStatus().getStatusCode());
        assertEquals("pending", lead.getMetadata().getCustomState().get("incomeVerification").get("statusCode").asText());
        assertEquals("pending", lead.getMetadata().getCustomState().get("affordability").get("statusCode").asText());
        assertEquals("pending", lead.getMetadata().getCustomState().get("creditDecision").get("statusCode").asText());
        assertEquals("pending", lead.getMetadata().getCustomState().get("contract").get("statusCode").asText());
        assertTrue(lead.getMetadata().getCustomState().get("contract").get("evidenceId").isNull());
    }

    // An update resets any states that depend on the updated field and the reset is propagated to dependent states
    @Test
    public void testApplicationDeniedAfterPostApprovalUpdate() throws Exception {
        LeadAggregate lead = originationService.createLead(productName);

        originationService.setLastName(lead.getLeadId(), "Jimmy");
        originationService.setFirstName(lead.getLeadId(), "Little");
        originationService.setIdNumber(lead.getLeadId(), "7709125081078");
        originationService.setGrossMonthlyIncome(lead.getLeadId(), 30000D);
        originationService.addProofOfIncome(lead.getLeadId(), "bankStatement.pdf",
                ByteBuffer.allocate(0).array(), FileMetadata.FileType.BANK_STATEMENT);
        originationService.addProofOfIdentity(lead.getLeadId(), "id.pdf",
                ByteBuffer.allocate(0).array(), FileMetadata.FileType.DRIVERS_LICENSE);
        originationService.setRepaymentPeriod(lead.getLeadId(), 6);
        originationService.setLoanAmount(lead.getLeadId(),  10000D);
        originationService.setMaritalStatus(lead.getLeadId(), "not-married");
        originationService.setMonthlyLivingExpense(lead.getLeadId(), "rent", 7000);

        Thread.sleep(2000);

        lead = originationService.getLead(lead.getLeadId());
        assertEquals("approved", lead.getMetadata().getLeadStatus().getStatusCode());
        assertEquals("verified", lead.getMetadata().getCustomState().get("incomeVerification").get("statusCode").asText());
        assertEquals("verified", lead.getMetadata().getCustomState().get("affordability").get("statusCode").asText());
        assertEquals("approved", lead.getMetadata().getCustomState().get("creditDecision").get("statusCode").asText());
        assertEquals("generated", lead.getMetadata().getCustomState().get("contract").get("statusCode").asText());

        originationService.setGrossMonthlyIncome(lead.getLeadId(), 5000D);

        Thread.sleep(2000);

        lead = originationService.getLead(lead.getLeadId());
        assertEquals("denied", lead.getMetadata().getLeadStatus().getStatusCode());
        assertEquals("failed", lead.getMetadata().getCustomState().get("affordability").get("statusCode").asText());
        assertEquals("denied", lead.getMetadata().getCustomState().get("creditDecision").get("statusCode").asText());
        assertEquals("pending", lead.getMetadata().getCustomState().get("contract").get("statusCode").asText());
        assertTrue(lead.getMetadata().getCustomState().get("contract").get("evidenceId").isNull());
    }

    @Test
    public void testUpdateFailsAfterLeadLocked() throws Exception {
        LeadAggregate lead = originationService.createLead(productName);

        originationService.setLastName(lead.getLeadId(), "Jimmy");
        originationService.setFirstName(lead.getLeadId(), "Little");
        originationService.setIdNumber(lead.getLeadId(), "7709125081078");
        originationService.setGrossMonthlyIncome(lead.getLeadId(), 30000D);
        originationService.addProofOfIncome(lead.getLeadId(), "bankStatement.pdf",
                ByteBuffer.allocate(0).array(), FileMetadata.FileType.BANK_STATEMENT);
        originationService.addProofOfIdentity(lead.getLeadId(), "id.pdf",
                ByteBuffer.allocate(0).array(), FileMetadata.FileType.DRIVERS_LICENSE);
        originationService.setRepaymentPeriod(lead.getLeadId(), 6);
        originationService.setLoanAmount(lead.getLeadId(),  10000D);
        originationService.setMaritalStatus(lead.getLeadId(), "not-married");
        originationService.setMonthlyLivingExpense(lead.getLeadId(), "rent", 7000);

        Thread.sleep(2000);

        lead = originationService.getLead(lead.getLeadId());
        assertEquals("approved", lead.getMetadata().getLeadStatus().getStatusCode());
        assertTrue(lead.getMetadata().getCustomState().containsKey("contract"));
        assertEquals("generated", lead.getMetadata().getCustomState().get("contract").get("statusCode").asText());

        originationService.setTermsAndConditionsAccepted(lead.getLeadId(), true);

        try {
            originationService.setMonthlyLivingExpense(lead.getLeadId(), "coffee", 5000);
            fail("Expected HTTP 400 because lead is locked");
        } catch (HttpClientErrorException e) {
            assertEquals(400, e.getRawStatusCode());
        }
    }

    // All criteria met results in application taken up
    @Test
    public void testApplicationHappyCase() throws Exception {
        LeadAggregate lead = originationService.createLead(productName);

        originationService.setLastName(lead.getLeadId(), "Jimmy");
        originationService.setFirstName(lead.getLeadId(), "Little");
        originationService.setIdNumber(lead.getLeadId(), "7709125081078");
        originationService.setGrossMonthlyIncome(lead.getLeadId(), 30000D);
        originationService.addProofOfIncome(lead.getLeadId(), "bankStatement.pdf",
                ByteBuffer.allocate(0).array(), FileMetadata.FileType.BANK_STATEMENT);
        originationService.addProofOfIdentity(lead.getLeadId(), "id.pdf",
                ByteBuffer.allocate(0).array(), FileMetadata.FileType.DRIVERS_LICENSE);
        originationService.setRepaymentPeriod(lead.getLeadId(), 6);
        originationService.setLoanAmount(lead.getLeadId(),  10000D);
        originationService.setMaritalStatus(lead.getLeadId(), "not-married");
        originationService.setMonthlyLivingExpense(lead.getLeadId(), "rent", 7000);

        Thread.sleep(2000);

        lead = originationService.getLead(lead.getLeadId());
        assertEquals("approved", lead.getMetadata().getLeadStatus().getStatusCode());
        assertTrue(lead.getMetadata().getCustomState().containsKey("contract"));
        assertEquals("generated", lead.getMetadata().getCustomState().get("contract").get("statusCode").asText());
        assertNotNull(lead.getMetadata().getCustomState().get("contract").get("evidenceId").asText());
        assertEquals("success", lead.getMetadata().getCustomState().get("kyc").get("statusCode").asText());
        assertEquals("verified", lead.getMetadata().getCustomState().get("creditProfileVerification").get("statusCode").asText());
        assertEquals("verified", lead.getMetadata().getCustomState().get("bankAccountVerification").get("statusCode").asText());
        assertEquals("verified", lead.getMetadata().getCustomState().get("creditProfileVerification").get("statusCode").asText());
        assertEquals("verified", lead.getMetadata().getCustomState().get("incomeVerification").get("statusCode").asText());
        assertEquals("approved", lead.getMetadata().getCustomState().get("creditDecision").get("statusCode").asText());
        assertEquals("verified", lead.getMetadata().getCustomState().get("affordability").get("statusCode").asText());
        assertEquals(true, lead.getMetadata().getCustomState().get("productRulesMet").asBoolean());

        originationService.setTermsAndConditionsAccepted(lead.getLeadId(), true);

        lead = originationService.getLead(lead.getLeadId());
        assertEquals("taken-up", lead.getMetadata().getLeadStatus().getStatusCode());
        assertEquals("signed", lead.getMetadata().getCustomState().get("contract").get("statusCode").asText());
        assertEquals(true, lead.getMetadata().getLocked());
    }


    // Old Tests
    @Test
    public void testCreditProfileVerification() throws Exception {
        LeadAggregate lead = originationService.createLead(productName);
        String stateKey = "creditProfileVerification";

        originationService.setLastName(lead.getLeadId(), "Jimmy");
        originationService.setFirstName(lead.getLeadId(), "Little");
        originationService.setIdNumber(lead.getLeadId(), "7709125081078");

        Thread.sleep(1200);

        lead = originationService.getLead(lead.getLeadId());
        assertEquals("verified", lead.getMetadata().getCustomState().get(stateKey).get("statusCode").asText());
        assertNotNull(lead.getMetadata().getCustomState().get(stateKey).get("refId").asText());
        assertNotNull(lead.getMetadata().getCustomState().get(stateKey).get("lastUpdated").asText());
    }

    @Test
    public void testBankAccountVerification() throws Exception {
        LeadAggregate lead = originationService.createLead(productName);
        String stateKey = "bankAccountVerification";

        originationService.setLastName(lead.getLeadId(), "Jimmy");
        originationService.setFirstName(lead.getLeadId(), "Little");
        originationService.setIdNumber(lead.getLeadId(), "7709125081078");

        Thread.sleep(1200);

        lead = originationService.getLead(lead.getLeadId());
        assertEquals("verified", lead.getMetadata().getCustomState().get(stateKey).get("statusCode").asText());
        assertNotNull(lead.getMetadata().getCustomState().get(stateKey).get("refId").asText());
        assertNotNull(lead.getMetadata().getCustomState().get(stateKey).get("lastUpdated").asText());
    }

    @Test
    public void testIncomeVerification() throws Exception {
        LeadAggregate lead = originationService.createLead(productName);
        String stateKey = "incomeVerification";

        originationService.setGrossMonthlyIncome(lead.getLeadId(), 30000D);
        originationService.addProofOfIncome(lead.getLeadId(), "bankStatement.pdf",
                ByteBuffer.allocate(0).array(), FileMetadata.FileType.BANK_STATEMENT);

        Thread.sleep(1200);

        lead = originationService.getLead(lead.getLeadId());
        assertEquals("verified", lead.getMetadata().getCustomState().get(stateKey).get("statusCode").asText());
        assertNotNull(lead.getMetadata().getCustomState().get(stateKey).get("refId").asText());
        assertNotNull(lead.getMetadata().getCustomState().get(stateKey).get("lastUpdated").asText());
    }

    @Test
    public void testAffordabilityCheck() throws Exception {
        LeadAggregate lead = originationService.createLead(productName);
        String stateKey = "affordability";

        originationService.setLastName(lead.getLeadId(), "Jimmy");
        originationService.setFirstName(lead.getLeadId(), "Little");
        originationService.setIdNumber(lead.getLeadId(), "7709125081078");
        originationService.setGrossMonthlyIncome(lead.getLeadId(), 30000D);
        originationService.addProofOfIncome(lead.getLeadId(), "bankStatement.pdf",
                ByteBuffer.allocate(0).array(), FileMetadata.FileType.BANK_STATEMENT);
        originationService.setMonthlyLivingExpense(lead.getLeadId(), "rent", 8000);
        originationService.setRepaymentPeriod(lead.getLeadId(), 6);
        originationService.setLoanAmount(lead.getLeadId(),  10000D);

        Thread.sleep(1200);

        lead = originationService.getLead(lead.getLeadId());
        assertEquals("verified", lead.getMetadata().getCustomState().get(stateKey).get("statusCode").asText());
        assertNotNull(lead.getMetadata().getCustomState().get(stateKey).get("refId").asText());
        assertNotNull(lead.getMetadata().getCustomState().get(stateKey).get("lastUpdated").asText());
    }

    @Test
    public void testCreditDecisioning() throws Exception {
        LeadAggregate lead = originationService.createLead(productName);
        String stateKey = "creditDecision";

        originationService.setLastName(lead.getLeadId(), "Jimmy");
        originationService.setFirstName(lead.getLeadId(), "Little");
        originationService.setIdNumber(lead.getLeadId(), "7709125081078");
        originationService.setGrossMonthlyIncome(lead.getLeadId(), 30000D);
        originationService.addProofOfIncome(lead.getLeadId(), "bankStatement.pdf",
                ByteBuffer.allocate(0).array(), FileMetadata.FileType.BANK_STATEMENT);
        originationService.setMonthlyLivingExpense(lead.getLeadId(), "rent", 8000);
        originationService.setRepaymentPeriod(lead.getLeadId(), 6);
        originationService.setLoanAmount(lead.getLeadId(),  10000D);

        Thread.sleep(1500);

        lead = originationService.getLead(lead.getLeadId());
        assertEquals("approved", lead.getMetadata().getCustomState().get(stateKey).get("statusCode").asText());
        assertNotNull(lead.getMetadata().getCustomState().get(stateKey).get("refId").asText());
        assertNotNull(lead.getMetadata().getCustomState().get(stateKey).get("lastUpdated").asText());
    }
    @Test
    public void testProductRulesMet() throws Exception {
        LeadAggregate lead = originationService.createLead(productName);
        String stateKey = "productRulesMet";

        originationService.setMaritalStatus(lead.getLeadId(), "not-married");

        Thread.sleep(500);

        lead = originationService.getLead(lead.getLeadId());
        assertEquals(true, lead.getMetadata().getCustomState().get(stateKey).asBoolean());
    }

    @Test
    public void testProductRulesNotMet() throws Exception {
        LeadAggregate lead = originationService.createLead(productName);
        String stateKey = "productRulesMet";

        originationService.setMaritalStatus(lead.getLeadId(), "married");

        Thread.sleep(500);

        lead = originationService.getLead(lead.getLeadId());
        assertEquals(false, lead.getMetadata().getCustomState().get(stateKey).asBoolean());
    }


    private LeadAggregate populatedLead() {
        return LeadAggregate.builder()
                .leadId(UUID.randomUUID().toString())
                .creationDate(Instant.now())
                .firstName("Jonny")
                .lastName("Just Jonny")
                .age(25)
                .dateOfBirth(LocalDate.now().minusYears(25))
                .idNumber(LocalDate.now().minusYears(25).format(DateTimeFormatter.ofPattern("yyMMdd")) + "5001888")
                .loanAmount(15000D)
                .product(productName)
                .metadata(LeadMetadata.builder().leadStatus(LeadStatus.builder().statusCode("pending").build()).build())
                .build();
    }
}
