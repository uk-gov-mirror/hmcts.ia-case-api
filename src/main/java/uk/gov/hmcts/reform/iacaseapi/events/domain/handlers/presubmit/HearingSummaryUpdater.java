package uk.gov.hmcts.reform.iacaseapi.events.domain.handlers.presubmit;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iacaseapi.shared.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.shared.domain.entities.DocumentWithMetadata;
import uk.gov.hmcts.reform.iacaseapi.events.domain.entities.Callback;
import uk.gov.hmcts.reform.iacaseapi.events.domain.entities.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.shared.domain.entities.ccd.EventId;
import uk.gov.hmcts.reform.iacaseapi.events.domain.entities.CallbackStage;
import uk.gov.hmcts.reform.iacaseapi.events.domain.handlers.PreSubmitCallbackHandler;
import uk.gov.hmcts.reform.iacaseapi.events.domain.service.DocumentAppender;

@Component
public class HearingSummaryUpdater implements PreSubmitCallbackHandler<AsylumCase> {

    private final DocumentAppender documentAppender;

    public HearingSummaryUpdater(
        @Autowired DocumentAppender documentAppender
    ) {
        this.documentAppender = documentAppender;
    }

    public boolean canHandle(
        CallbackStage callbackStage,
        Callback<AsylumCase> callback
    ) {
        return callbackStage == CallbackStage.ABOUT_TO_SUBMIT
               && callback.getEventId() == EventId.CREATE_HEARING_SUMMARY;
    }

    public PreSubmitCallbackResponse<AsylumCase> handle(
        CallbackStage callbackStage,
        Callback<AsylumCase> callback
    ) {
        if (!canHandle(callbackStage, callback)) {
            throw new IllegalStateException("Cannot handle ccd event");
        }

        AsylumCase asylumCase =
            callback
                .getCaseDetails()
                .getCaseData();

        PreSubmitCallbackResponse<AsylumCase> preSubmitResponse =
            new PreSubmitCallbackResponse<>(asylumCase);

        DocumentWithMetadata hearingSummary =
            asylumCase
                .getHearingSummary()
                .orElseThrow(() -> new IllegalStateException("hearingSummary not present"));

        asylumCase
            .getCaseArgument()
            .orElseThrow(() -> new IllegalStateException("caseArgument not present"))
            .setHearingSummary(hearingSummary);

        documentAppender.append(asylumCase, hearingSummary);

        asylumCase.clearHearingSummary();

        return preSubmitResponse;
    }
}