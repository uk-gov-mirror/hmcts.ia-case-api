package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.*;

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
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;

@MockitoSettings(strictness = Strictness.LENIENT)
@ExtendWith(MockitoExtension.class)
@SuppressWarnings("unchecked")
class LegalRepresentativeUpdateDetailsHandlerTest {

    private final String legalRepName = "John Doe";
    private final String legalRepEmailAddress = "john.doe@example.com";
    private final String legalRepReferenceNumber = "ABC-123";
    @Mock
    private Callback<AsylumCase> callback;
    @Mock
    private CaseDetails<AsylumCase> caseDetails;
    @Mock
    private AsylumCase asylumCase;
    private LegalRepresentativeUpdateDetailsHandler legalRepresentativeUpdateDetailsHandler;

    @BeforeEach
    public void setUp() {
        legalRepresentativeUpdateDetailsHandler = new LegalRepresentativeUpdateDetailsHandler();

        when(callback.getEvent()).thenReturn(Event.UPDATE_LEGAL_REPRESENTATIVES_DETAILS);
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);

        when(asylumCase.read(UPDATE_LEGAL_REP_NAME, String.class)).thenReturn(Optional.of(legalRepName));
        when(asylumCase.read(UPDATE_LEGAL_REP_EMAIL_ADDRESS, String.class))
            .thenReturn(Optional.of(legalRepEmailAddress));
        when(asylumCase.read(UPDATE_LEGAL_REP_REFERENCE_NUMBER, String.class))
            .thenReturn(Optional.of(legalRepReferenceNumber));
    }

    @Test
    void prepare_fields_test() {
        PreSubmitCallbackResponse<AsylumCase> callbackResponse =
            legalRepresentativeUpdateDetailsHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);

        assertNotNull(callbackResponse);
        assertEquals(asylumCase, callbackResponse.getData());

        verify(asylumCase).read(UPDATE_LEGAL_REP_NAME, String.class);
        verify(asylumCase).read(UPDATE_LEGAL_REP_EMAIL_ADDRESS, String.class);
        verify(asylumCase).read(UPDATE_LEGAL_REP_REFERENCE_NUMBER, String.class);

        verify(asylumCase, times(1)).clear(eq(UPDATE_LEGAL_REP_COMPANY));
        verify(asylumCase, times(1)).clear(eq(UPDATE_LEGAL_REP_NAME));
        verify(asylumCase, times(1)).clear(eq(UPDATE_LEGAL_REP_EMAIL_ADDRESS));
        verify(asylumCase, times(1)).clear(eq(UPDATE_LEGAL_REP_REFERENCE_NUMBER));

        verify(asylumCase, times(1)).write(eq(LEGAL_REP_NAME), eq(legalRepName));
        verify(asylumCase, times(1)).write(eq(LEGAL_REPRESENTATIVE_EMAIL_ADDRESS), eq(legalRepEmailAddress));
        verify(asylumCase, times(1)).write(eq(LEGAL_REP_REFERENCE_NUMBER), eq(legalRepReferenceNumber));

        verify(asylumCase, times(1)).clear(eq(CHANGE_ORGANISATION_REQUEST_FIELD));
    }

    @Test
    void handling_should_throw_if_cannot_actually_handle() {

        assertThatThrownBy(
            () -> legalRepresentativeUpdateDetailsHandler.handle(PreSubmitCallbackStage.ABOUT_TO_START, callback))
            .hasMessage("Cannot handle callback")
            .isExactlyInstanceOf(IllegalStateException.class);
    }

    @Test
    void it_can_handle_callback() {

        for (Event event : Event.values()) {
            when(callback.getEvent()).thenReturn(event);

            for (PreSubmitCallbackStage callbackStage : PreSubmitCallbackStage.values()) {
                boolean canHandle = legalRepresentativeUpdateDetailsHandler.canHandle(callbackStage, callback);

                if (event == Event.UPDATE_LEGAL_REPRESENTATIVES_DETAILS
                    && callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT) {
                    assertTrue(canHandle);
                } else {
                    assertFalse(canHandle);
                }
            }

            reset(callback);
        }
    }

    @Test
    void should_not_allow_null_arguments() {

        assertThatThrownBy(() -> legalRepresentativeUpdateDetailsHandler.canHandle(null, callback))
            .hasMessage("callbackStage must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(
            () -> legalRepresentativeUpdateDetailsHandler.canHandle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, null))
            .hasMessage("callback must not be null")
            .isExactlyInstanceOf(NullPointerException.class);
    }
}
