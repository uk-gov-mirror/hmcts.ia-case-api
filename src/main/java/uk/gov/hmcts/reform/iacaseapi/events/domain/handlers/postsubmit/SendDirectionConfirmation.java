package uk.gov.hmcts.reform.iacaseapi.events.domain.handlers.postsubmit;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iacaseapi.shared.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.events.domain.entities.Callback;
import uk.gov.hmcts.reform.iacaseapi.events.domain.entities.PostSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.shared.domain.entities.ccd.EventId;
import uk.gov.hmcts.reform.iacaseapi.events.domain.entities.CallbackStage;
import uk.gov.hmcts.reform.iacaseapi.events.domain.handlers.PostSubmitCallbackHandler;

@Component
public class SendDirectionConfirmation implements PostSubmitCallbackHandler<AsylumCase> {

    public boolean canHandle(
        CallbackStage callbackStage,
        Callback<AsylumCase> callback
    ) {
        return callbackStage == CallbackStage.SUBMITTED
               && callback.getEventId() == EventId.SEND_DIRECTION;
    }

    public PostSubmitCallbackResponse handle(
        CallbackStage callbackStage,
        Callback<AsylumCase> callback
    ) {
        if (!canHandle(callbackStage, callback)) {
            throw new IllegalStateException("Cannot handle ccd event");
        }

        PostSubmitCallbackResponse postSubmitResponse =
            new PostSubmitCallbackResponse();

        postSubmitResponse.setConfirmationHeader("# You have sent a direction");
        postSubmitResponse.setConfirmationBody(
            "#### What happens next\n\n"
            + "Wait for the appellant to complete the direction. "
            + "If they request a time extension, you will be notified by email."
        );

        return postSubmitResponse;
    }
}