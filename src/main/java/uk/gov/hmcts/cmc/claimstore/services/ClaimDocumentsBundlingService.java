package uk.gov.hmcts.cmc.claimstore.services;

import org.springframework.beans.factory.annotation.Autowired;
import uk.gov.hmcts.cmc.claimstore.documents.*;
import uk.gov.hmcts.cmc.claimstore.documents.output.PDF;
import uk.gov.hmcts.cmc.claimstore.documents.questionnaire.ClaimantDirectionsQuestionnairePdfService;
import uk.gov.hmcts.cmc.claimstore.services.document.DocumentManagementService;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.cmc.domain.models.ClaimDocument;
import uk.gov.hmcts.cmc.domain.models.ClaimDocumentType;

import java.util.*;
import java.util.stream.Stream;

public class ClaimDocumentsBundlingService {

    private final DocumentManagementService documentManagementService;

    @Autowired
    public ClaimDocumentsBundlingService(
        DocumentManagementService documentManagementService
    ) {
        this.documentManagementService = documentManagementService;
    }

    public Map<String, List<PDF>> getBundledClaimDocuments(Claim claim, String authorisation, PDF coverLetter) {

        List<PDF> claimDocuments = new ArrayList<>();
        claimDocuments.add(coverLetter);
        Stream.of(ClaimDocumentType.values()).forEach(docType ->
            {
                Optional<ClaimDocument> claimDocument = claim.getClaimDocument(docType);
                byte[] doc = claimDocument.isPresent() ? documentManagementService.downloadDocument(authorisation, claimDocument.get()) : null;
                if (doc != null) {
                    claimDocuments.add(getPdf(doc));
                }
            });

        Map<String, List<PDF>> claimDocumentsMap = new HashMap<>();
        claimDocumentsMap.put(claim.getReferenceNumber(), claimDocuments);
        return claimDocumentsMap;
    }

    private PDF getPdf(byte[] documentInByte) {
        return null;

    }
}
