package uk.gov.hmcts.reform.iacaseapi.domain.handlers.postsubmit;

import static java.util.Objects.requireNonNull;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PostSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.handlers.PostSubmitCallbackHandler;

@Component
public class RequestResponseAmendConfirmation implements PostSubmitCallbackHandler<AsylumCase> {

    public boolean canHandle(Callback<AsylumCase> callback) {
        requireNonNull(callback, "callback must not be null");

        return callback.getEvent() == Event.REQUEST_RESPONSE_AMEND;
    }

    public PostSubmitCallbackResponse handle(Callback<AsylumCase> callback) {
        if (!canHandle(callback)) {
            throw new IllegalStateException("Cannot handle callback");
        }

        final AsylumCase asylumCase = callback.getCaseDetails().getCaseData();
        final String hoAmendResponseReadyInstructStatus =
            asylumCase.read(AsylumCaseFieldDefinition.HOME_OFFICE_AMEND_RESPONSE_INSTRUCT_STATUS,
                String.class).orElse("");

        PostSubmitCallbackResponse postSubmitResponse =
                new PostSubmitCallbackResponse();

        if (hoAmendResponseReadyInstructStatus.equalsIgnoreCase("FAIL")) {

            postSubmitResponse.setConfirmationBody(
                "![Respondent notification failed confirmation]"
                + "(https://raw.githubusercontent.com/hmcts/ia-appeal-frontend/master/app/assets/images/respondent_notification_failed.svg)\n"
                + "#### Do this next\n\n"
                + "Contact the respondent to tell them what has changed, including any action they need to take.\n"
            );
        } else {

            String directionsTabUrl =
                "/case/IA/Asylum/"
                + callback.getCaseDetails().getId()
                + "#directions";

            postSubmitResponse.setConfirmationHeader("# You have sent a direction");
            postSubmitResponse.setConfirmationBody(
                "#### What happens next\n\n"
                + "You can see the status of the direction in the "
                + "[directions tab](" + directionsTabUrl + ")"
            );
        }

        return postSubmitResponse;
    }
}
