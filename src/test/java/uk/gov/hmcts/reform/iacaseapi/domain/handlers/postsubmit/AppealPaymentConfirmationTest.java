package uk.gov.hmcts.reform.iacaseapi.domain.handlers.postsubmit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.PAYMENT_ERROR_MESSAGE;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.PAYMENT_STATUS;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.PaymentStatus.FAILED;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.PaymentStatus.PAID;

import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.State;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PostSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.PaymentStatus;

@MockitoSettings(strictness = Strictness.LENIENT)
@ExtendWith(MockitoExtension.class)
@SuppressWarnings("unchecked")
class AppealPaymentConfirmationTest {

    @Mock
    private Callback<AsylumCase> callback;
    @Mock
    private CaseDetails<AsylumCase> caseDetails;
    @Mock
    private AsylumCase asylumCase;

    private AppealPaymentConfirmationProvider appealPaymentConfirmationProvider =
        new AppealPaymentConfirmationProvider();

    private AppealPaymentConfirmation appealPaymentConfirmation =
        new AppealPaymentConfirmation(appealPaymentConfirmationProvider);

    @BeforeEach
    public void setUp() {

        when(callback.getEvent()).thenReturn(Event.PAYMENT_APPEAL);
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
    }

    @Test
    void should_return_payment_success_confirmation_when_payment_paid_when_appeal_started() {

        when(asylumCase.read(PAYMENT_STATUS, PaymentStatus.class)).thenReturn(Optional.of(PAID));

        when(callback.getCaseDetails().getState()).thenReturn(State.APPEAL_STARTED);

        when(callback.getEvent()).thenReturn(Event.PAYMENT_APPEAL);

        PostSubmitCallbackResponse callbackResponse =
            appealPaymentConfirmation.handle(callback);

        assertNotNull(callbackResponse);
        assertTrue(callbackResponse.getConfirmationHeader().isPresent());
        assertTrue(callbackResponse.getConfirmationBody().isPresent());

        assertThat(
            callbackResponse.getConfirmationHeader().get())
            .contains("You have paid for the appeal \n# You still need to submit it");

        assertThat(
            callbackResponse.getConfirmationBody().get())
            .contains("Payment successful");

    }

    @Test
    void should_return_payment_success_confirmation_when_payment_paid_after_appeal_started() {

        when(asylumCase.read(PAYMENT_STATUS, PaymentStatus.class)).thenReturn(Optional.of(PAID));

        when(callback.getCaseDetails().getState()).thenReturn(State.CASE_BUILDING);

        when(callback.getEvent()).thenReturn(Event.PAYMENT_APPEAL);

        PostSubmitCallbackResponse callbackResponse =
            appealPaymentConfirmation.handle(callback);

        assertNotNull(callbackResponse);
        assertTrue(callbackResponse.getConfirmationHeader().isPresent());
        assertTrue(callbackResponse.getConfirmationBody().isPresent());

        assertThat(
            callbackResponse.getConfirmationHeader().get())
            .contains("You have paid for the appeal");

        assertThat(
            callbackResponse.getConfirmationBody().get())
            .contains("Payment successful");

        assertThat(
            callbackResponse.getConfirmationBody().get())
            .contains("You will receive a notification to confirm the payment has been made");

    }

    @Test
    void should_return_payment_failed_confirmation_when_payment_failed() {

        when(asylumCase.read(PAYMENT_STATUS, PaymentStatus.class)).thenReturn(Optional.of(FAILED));
        when(asylumCase.read(PAYMENT_ERROR_MESSAGE, String.class)).thenReturn(Optional.of("Your account is deleted"));

        when(callback.getEvent()).thenReturn(Event.PAYMENT_APPEAL);

        PostSubmitCallbackResponse callbackResponse =
            appealPaymentConfirmation.handle(callback);

        assertNotNull(callbackResponse);
        assertTrue(callbackResponse.getConfirmationHeader().isPresent());
        assertTrue(callbackResponse.getConfirmationBody().isPresent());

        assertThat(
            callbackResponse.getConfirmationHeader().get())
            .contains("");

        assertThat(
            callbackResponse.getConfirmationBody().get())
            .contains(
                "![Payment failed confirmation](https://raw.githubusercontent.com/hmcts/ia-appeal-frontend/master/app/assets/images/paymentFailed.png)\n");

        assertThat(
            callbackResponse.getConfirmationBody().get())
            .contains(
                "Call 01633 652 125 (option 3) or email MiddleOffice.DDServices@liberata.com to try to resolve the payment issue");

    }

    @Test
    void handling_should_throw_if_cannot_actually_handle() {

        when(callback.getEvent()).thenReturn(Event.BUILD_CASE);

        assertThatThrownBy(() -> appealPaymentConfirmation.handle(callback))
            .hasMessage("Cannot handle callback")
            .isExactlyInstanceOf(IllegalStateException.class);
    }

    @Test
    void it_can_handle_callback() {

        for (Event event : Event.values()) {

            when(callback.getEvent()).thenReturn(event);

            boolean canHandle = appealPaymentConfirmation.canHandle(callback);

            if (event == Event.PAYMENT_APPEAL) {

                assertTrue(canHandle);
            } else {
                assertFalse(canHandle);
            }

            reset(callback);
        }
    }

    @Test
    void should_not_allow_null_arguments() {

        assertThatThrownBy(() -> appealPaymentConfirmation.canHandle(null))
            .hasMessage("callback must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> appealPaymentConfirmation.handle(null))
            .hasMessage("callback must not be null")
            .isExactlyInstanceOf(NullPointerException.class);
    }
}
