package com.sprinthive.origination.service;

import com.sprinthive.origination.client.OriginationServiceClient;
import com.sprinthive.origination.client.RestTemplateOriginationV1Client;
import com.sprinthive.origination.model.FileMetadata;
import com.sprinthive.origination.model.LeadAggregate;
import com.sprinthive.origination.model.LeadMetadata;
import com.sprinthive.origination.model.LeadStatus;
import org.junit.Before;
import org.junit.Test;
import org.springframework.web.client.RestTemplate;

import java.nio.ByteBuffer;
import java.time.Instant;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class ACMEStoreCardTest {
    private OriginationServiceClient originationService;

    private static String productName = "ACMEStoreCard";

    @Before
    public void setup() {
        originationService = new RestTemplateOriginationV1Client(new RestTemplate(), "https://honeypot.sprinthive.tech/lead");
    }

    @Test
    public void testKycValidation() throws Exception {
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

    @Test
    public void testCreditProfileVerification() throws Exception {
        LeadAggregate lead = originationService.createLead(productName);
        String stateKey = "creditProfileVerification";

        originationService.setLastName(lead.getLeadId(), "Jimmy");
        originationService.setFirstName(lead.getLeadId(), "Little");
        originationService.setIdNumber(lead.getLeadId(), "7709125081078");

        Thread.sleep(100);

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

        Thread.sleep(100);

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

        Thread.sleep(100);

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

        Thread.sleep(100);

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

        Thread.sleep(100);

        lead = originationService.getLead(lead.getLeadId());
        assertEquals("approved", lead.getMetadata().getCustomState().get(stateKey).get("statusCode").asText());
        assertNotNull(lead.getMetadata().getCustomState().get(stateKey).get("refId").asText());
        assertNotNull(lead.getMetadata().getCustomState().get(stateKey).get("lastUpdated").asText());
    }

    @Test
    public void testApplicationApproval() throws Exception {
        LeadAggregate lead = originationService.createLead(productName);

        originationService.setLastName(lead.getLeadId(), "Jimmy");
        originationService.setFirstName(lead.getLeadId(), "Little");
        originationService.setIdNumber(lead.getLeadId(), "7709125081078");
        originationService.setGrossMonthlyIncome(lead.getLeadId(), 30000D);
        originationService.addProofOfIncome(lead.getLeadId(), "bankStatement.pdf",
                ByteBuffer.allocate(0).array(), FileMetadata.FileType.BANK_STATEMENT);
        originationService.setMonthlyLivingExpense(lead.getLeadId(), "rent", 8000);
        originationService.setRepaymentPeriod(lead.getLeadId(), 6);
        originationService.setLoanAmount(lead.getLeadId(),  10000D);
        originationService.setMaritalStatus(lead.getLeadId(), "not-married");

        Thread.sleep(1200);

        lead = originationService.getLead(lead.getLeadId());
        assertEquals("approved", lead.getMetadata().getLeadStatus().getStatusCode());
        assertTrue(lead.getMetadata().getCustomState().containsKey("contract"));
        assertEquals("generated", lead.getMetadata().getCustomState().get("contract").get("statusCode").asText());
    }

    @Test
    public void testApplicationPendingAfterPostApprovalUpdate() throws Exception {
        LeadAggregate lead = originationService.createLead(productName);

        originationService.setLastName(lead.getLeadId(), "Jimmy");
        originationService.setFirstName(lead.getLeadId(), "Little");
        originationService.setIdNumber(lead.getLeadId(), "7709125081078");
        originationService.setGrossMonthlyIncome(lead.getLeadId(), 30000D);
        originationService.addProofOfIncome(lead.getLeadId(), "bankStatement.pdf",
                ByteBuffer.allocate(0).array(), FileMetadata.FileType.BANK_STATEMENT);
        originationService.setMonthlyLivingExpense(lead.getLeadId(), "rent", 8000);
        originationService.setRepaymentPeriod(lead.getLeadId(), 6);
        originationService.setLoanAmount(lead.getLeadId(),  10000D);
        originationService.setMaritalStatus(lead.getLeadId(), "not-married");

        Thread.sleep(1200);

        lead = originationService.getLead(lead.getLeadId());
        assertEquals("approved", lead.getMetadata().getLeadStatus().getStatusCode());
        assertTrue(lead.getMetadata().getCustomState().containsKey("contract"));
        assertEquals("generated", lead.getMetadata().getCustomState().get("contract").get("statusCode").asText());

        originationService.setMaritalStatus(lead.getLeadId(), "married");

        lead = originationService.getLead(lead.getLeadId());
        assertEquals("pending", lead.getMetadata().getLeadStatus().getStatusCode());
    }

    @Test
    public void testProductRulesMet() throws Exception {
        LeadAggregate lead = originationService.createLead(productName);
        String stateKey = "productRulesMet";

        originationService.setMaritalStatus(lead.getLeadId(), "not-married");

        Thread.sleep(1200);

        lead = originationService.getLead(lead.getLeadId());
        assertEquals(true, lead.getMetadata().getCustomState().get(stateKey).asBoolean());
    }

    @Test
    public void testProductRulesNotMet() throws Exception {
        LeadAggregate lead = originationService.createLead(productName);
        String stateKey = "productRulesMet";

        originationService.setMaritalStatus(lead.getLeadId(), "married");

        Thread.sleep(1200);

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
