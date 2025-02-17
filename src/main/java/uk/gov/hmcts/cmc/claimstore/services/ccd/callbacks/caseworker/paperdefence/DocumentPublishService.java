package uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks.caseworker.paperdefence;

import com.google.common.collect.ImmutableList;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.cmc.ccd.domain.CCDBulkPrintDetails;
import uk.gov.hmcts.cmc.ccd.domain.CCDCase;
import uk.gov.hmcts.cmc.ccd.domain.CCDCollectionElement;
import uk.gov.hmcts.cmc.ccd.domain.CCDDocument;
import uk.gov.hmcts.cmc.ccd.mapper.BulkPrintDetailsMapper;
import uk.gov.hmcts.cmc.claimstore.documents.BulkPrintHandler;
import uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks.PrintableDocumentService;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.cmc.domain.models.bulkprint.BulkPrintDetails;
import uk.gov.hmcts.reform.sendletter.api.Document;

import java.time.LocalDate;

@Service
public class DocumentPublishService {
    private final PaperResponseLetterService paperResponseLetterService;
    private final PrintableDocumentService printableDocumentService;
    private final BulkPrintHandler bulkPrintHandler;
    private final BulkPrintDetailsMapper bulkPrintDetailsMapper;

    @Autowired
    public DocumentPublishService(
        PaperResponseLetterService paperResponseLetterService,
        PrintableDocumentService printableDocumentService,
        BulkPrintHandler bulkPrintHandler,
        BulkPrintDetailsMapper bulkPrintDetailsMapper
    ) {
        this.paperResponseLetterService = paperResponseLetterService;
        this.printableDocumentService = printableDocumentService;
        this.bulkPrintHandler = bulkPrintHandler;
        this.bulkPrintDetailsMapper = bulkPrintDetailsMapper;
    }

    public CCDCase publishDocuments(
        CCDCase ccdCase,
        Claim claim,
        String authorisation,
        LocalDate extendedResponseDeadline
    ) {
        CCDDocument coverLetter = paperResponseLetterService
            .createCoverLetter(ccdCase, authorisation, extendedResponseDeadline);

        Document coverDoc = printableDocumentService.process(coverLetter, authorisation);

        CCDDocument oconForm = paperResponseLetterService
            .createOconForm(ccdCase, claim, authorisation, extendedResponseDeadline);

        Document formDoc = printableDocumentService.process(oconForm, authorisation);

        BulkPrintDetails bulkPrintDetails = bulkPrintHandler.printPaperDefence(claim, coverDoc, formDoc, authorisation);
        CCDCase updated = addToBulkPrintDetails(ccdCase, bulkPrintDetails);
        return paperResponseLetterService
            .addCoverLetterToCaseWithDocuments(updated, claim, coverLetter, authorisation);
    }

    private CCDCase addToBulkPrintDetails(
        CCDCase ccdCase,
        BulkPrintDetails input
    ) {
        ImmutableList.Builder<CCDCollectionElement<CCDBulkPrintDetails>> printDetails = ImmutableList.builder();
        printDetails.addAll(ccdCase.getBulkPrintDetails());
        if (input != null) {
            printDetails.add(bulkPrintDetailsMapper.to(input));
        }
        return ccdCase.toBuilder().bulkPrintDetails(printDetails.build()).build();
    }
}
