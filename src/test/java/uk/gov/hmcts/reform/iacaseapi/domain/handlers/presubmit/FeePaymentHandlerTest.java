package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.APPEAL_TYPE;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.ASYLUM_SUPPORT_DOCUMENT;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.ASYLUM_SUPPORT_REFERENCE;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.DECISION_HEARING_FEE_OPTION;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.EA_HU_APPEAL_TYPE_PAYMENT_OPTION;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.FEE_REMISSION_TYPE;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.HEARING_DECISION_SELECTED;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.HOME_OFFICE_WAIVER_DOCUMENT;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.IS_FEE_PAYMENT_ENABLED;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.LEGAL_AID_ACCOUNT_NUMBER;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.PAYMENT_STATUS;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.PA_APPEAL_TYPE_PAYMENT_OPTION;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.REMISSION_CLAIM;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.REMISSION_TYPE;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.RP_DC_APPEAL_HEARING_OPTION;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.SECTION17_DOCUMENT;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.SECTION20_DOCUMENT;

import java.util.Arrays;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AppealType;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.RemissionType;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.PaymentStatus;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.YesOrNo;
import uk.gov.hmcts.reform.iacaseapi.domain.service.FeatureToggler;
import uk.gov.hmcts.reform.iacaseapi.domain.service.FeePayment;

@MockitoSettings(strictness = Strictness.LENIENT)
@ExtendWith(MockitoExtension.class)
@SuppressWarnings("unchecked")
class FeePaymentHandlerTest {

    @Mock private FeePayment<AsylumCase> feePayment;
    @Mock private Callback<AsylumCase> callback;
    @Mock private CaseDetails<AsylumCase> caseDetails;
    @Mock private AsylumCase asylumCase;
    @Mock private FeatureToggler featureToggler;


    private FeePaymentHandler feePaymentHandler;

    @BeforeEach
    public void setUp() {

        feePaymentHandler =
            new FeePaymentHandler(true, feePayment, featureToggler);
    }

    @Test
    void should_make_feePayment_and_update_the_case() {

        Arrays.asList(
            Event.PAYMENT_APPEAL
        ).forEach(event -> {

            when(callback.getEvent()).thenReturn(event);
            when(callback.getCaseDetails()).thenReturn(caseDetails);
            when(caseDetails.getCaseData()).thenReturn(asylumCase);
            when(feePayment.aboutToSubmit(callback)).thenReturn(asylumCase);
            when(asylumCase.read(APPEAL_TYPE, AppealType.class))
                .thenReturn(Optional.of(AppealType.PA));

            PreSubmitCallbackResponse<AsylumCase> callbackResponse =
                feePaymentHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);

            assertNotNull(callbackResponse);
            assertEquals(asylumCase, callbackResponse.getData());

            verify(feePayment, times(1)).aboutToSubmit(callback);

            reset(callback);
            reset(feePayment);
        });
    }

    @Test
    void should_clear_other_when_pa_offline_payment() {

        Arrays.asList(
            Event.START_APPEAL
        ).forEach(event -> {

            when(callback.getEvent()).thenReturn(event);
            when(callback.getCaseDetails()).thenReturn(caseDetails);
            when(caseDetails.getCaseData()).thenReturn(asylumCase);
            when(feePayment.aboutToSubmit(callback)).thenReturn(asylumCase);
            when(asylumCase.read(APPEAL_TYPE,
                AppealType.class)).thenReturn(Optional.of(AppealType.PA));

            PreSubmitCallbackResponse<AsylumCase> callbackResponse =
                feePaymentHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);

            assertNotNull(callbackResponse);
            assertEquals(asylumCase, callbackResponse.getData());

            verify(feePayment, times(1)).aboutToSubmit(callback);
            verify(asylumCase, times(1)).write(PAYMENT_STATUS, PaymentStatus.PAYMENT_PENDING);
            verify(asylumCase, times(1)).write(IS_FEE_PAYMENT_ENABLED, YesOrNo.YES);
            verify(asylumCase, times(1)).clear(ASYLUM_SUPPORT_REFERENCE);
            verify(asylumCase, times(1)).clear(ASYLUM_SUPPORT_DOCUMENT);
            verify(asylumCase, times(1)).clear(LEGAL_AID_ACCOUNT_NUMBER);
            verify(asylumCase, times(1)).clear(LEGAL_AID_ACCOUNT_NUMBER);
            verify(asylumCase, times(1)).clear(SECTION17_DOCUMENT);
            verify(asylumCase, times(1)).clear(SECTION20_DOCUMENT);
            verify(asylumCase, times(1)).clear(HOME_OFFICE_WAIVER_DOCUMENT);
            verify(asylumCase, times(1)).clear(RP_DC_APPEAL_HEARING_OPTION);
            verify(asylumCase, times(1)).clear(REMISSION_CLAIM);
            verify(asylumCase, times(1)).clear(FEE_REMISSION_TYPE);
            reset(callback);
            reset(feePayment);
        });
    }

    @Test
    void should_clear_other_when_hu_offline_payment() {

        Arrays.asList(
            Event.START_APPEAL
        ).forEach(event -> {

            when(callback.getEvent()).thenReturn(event);
            when(callback.getCaseDetails()).thenReturn(caseDetails);
            when(caseDetails.getCaseData()).thenReturn(asylumCase);
            when(feePayment.aboutToSubmit(callback)).thenReturn(asylumCase);
            when(asylumCase.read(APPEAL_TYPE,
                AppealType.class)).thenReturn(Optional.of(AppealType.HU));

            PreSubmitCallbackResponse<AsylumCase> callbackResponse =
                feePaymentHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);

            assertNotNull(callbackResponse);
            assertEquals(asylumCase, callbackResponse.getData());

            verify(feePayment, times(1)).aboutToSubmit(callback);
            verify(asylumCase, times(1))
                .write(PAYMENT_STATUS, PaymentStatus.PAYMENT_PENDING);
            verify(asylumCase, times(1)).clear(PA_APPEAL_TYPE_PAYMENT_OPTION);
            reset(callback);
            reset(feePayment);
        });
    }

    @Test
    void should_clear_other_when_ea_offline_payment() {

        Arrays.asList(
            Event.START_APPEAL
        ).forEach(event -> {

            when(callback.getEvent()).thenReturn(event);
            when(callback.getCaseDetails()).thenReturn(caseDetails);
            when(caseDetails.getCaseData()).thenReturn(asylumCase);
            when(feePayment.aboutToSubmit(callback)).thenReturn(asylumCase);
            when(asylumCase.read(APPEAL_TYPE,
                AppealType.class)).thenReturn(Optional.of(AppealType.EA));

            PreSubmitCallbackResponse<AsylumCase> callbackResponse =
                feePaymentHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);

            assertNotNull(callbackResponse);
            assertEquals(asylumCase, callbackResponse.getData());

            verify(feePayment, times(1)).aboutToSubmit(callback);
            verify(asylumCase, times(1))
                .write(PAYMENT_STATUS, PaymentStatus.PAYMENT_PENDING);
            verify(asylumCase, times(1)).clear(PA_APPEAL_TYPE_PAYMENT_OPTION);
            reset(callback);
            reset(feePayment);
        });
    }

    @ParameterizedTest
    @ValueSource(strings = {"DC", "RP"})
    void should_clear_all_payment_details_for_non_payment_appeal_type(String type) {

        Arrays.asList(
            Event.START_APPEAL
        ).forEach(event -> {

            when(callback.getEvent()).thenReturn(event);
            when(callback.getCaseDetails()).thenReturn(caseDetails);
            when(caseDetails.getCaseData()).thenReturn(asylumCase);
            when(feePayment.aboutToSubmit(callback)).thenReturn(asylumCase);
            when(asylumCase.read(APPEAL_TYPE,
                AppealType.class)).thenReturn(Optional.of(AppealType.valueOf(type)));
            when(asylumCase.read(RP_DC_APPEAL_HEARING_OPTION, String.class))
                .thenReturn(Optional.of("decisionWithoutHearing"));

            PreSubmitCallbackResponse<AsylumCase> callbackResponse =
                feePaymentHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);

            assertNotNull(callbackResponse);
            assertEquals(asylumCase, callbackResponse.getData());

            verify(asylumCase, times(1))
                .write(DECISION_HEARING_FEE_OPTION, "decisionWithoutHearing");
            verify(asylumCase, times(1))
                .clear(HEARING_DECISION_SELECTED);
            verify(asylumCase, times(1))
                .clear(PA_APPEAL_TYPE_PAYMENT_OPTION);
            verify(asylumCase, times(1))
                .clear(EA_HU_APPEAL_TYPE_PAYMENT_OPTION);
            verify(asylumCase, times(1))
                .clear(PAYMENT_STATUS);
            verify(asylumCase, times(1)).clear(FEE_REMISSION_TYPE);
            verify(asylumCase, times(1)).clear(REMISSION_TYPE);
            verify(asylumCase, times(1)).clear(REMISSION_CLAIM);
            verify(asylumCase, times(1)).clear(ASYLUM_SUPPORT_REFERENCE);
            verify(asylumCase, times(1)).clear(ASYLUM_SUPPORT_DOCUMENT);
            verify(asylumCase, times(1)).clear(LEGAL_AID_ACCOUNT_NUMBER);
            verify(asylumCase, times(1)).clear(SECTION17_DOCUMENT);
            verify(asylumCase, times(1)).clear(SECTION20_DOCUMENT);
            verify(asylumCase, times(1)).clear(HOME_OFFICE_WAIVER_DOCUMENT);
            reset(callback);
            reset(feePayment);
        });
    }

    @Test
    void should_return_remission_for_asylum_support() {

        when(callback.getEvent()).thenReturn(Event.EDIT_APPEAL);
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(featureToggler.getValue("remissions-feature", false)).thenReturn(true);
        when(feePayment.aboutToSubmit(callback)).thenReturn(asylumCase);
        when(asylumCase.read(APPEAL_TYPE, AppealType.class))
            .thenReturn(Optional.of(AppealType.EA));
        when(asylumCase.read(REMISSION_TYPE, RemissionType.class))
            .thenReturn(Optional.of(RemissionType.HO_WAIVER_REMISSION));
        when(asylumCase.read(REMISSION_CLAIM, String.class))
            .thenReturn(Optional.of("asylumSupport"));

        PreSubmitCallbackResponse<AsylumCase> callbackResponse =
            feePaymentHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);

        assertNotNull(callbackResponse);
        assertEquals(asylumCase, callbackResponse.getData());

        verify(asylumCase, times(1)).write(FEE_REMISSION_TYPE, "Asylum support");
        verify(asylumCase, times(1)).clear(LEGAL_AID_ACCOUNT_NUMBER);
    }

    @Test
    void should_return_remission_for_legal_aid() {

        when(callback.getEvent()).thenReturn(Event.EDIT_APPEAL);
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(featureToggler.getValue("remissions-feature", false)).thenReturn(true);
        when(feePayment.aboutToSubmit(callback)).thenReturn(asylumCase);
        when(asylumCase.read(APPEAL_TYPE, AppealType.class))
            .thenReturn(Optional.of(AppealType.PA));
        when(asylumCase.read(REMISSION_TYPE, RemissionType.class))
            .thenReturn(Optional.of(RemissionType.HO_WAIVER_REMISSION));
        when(asylumCase.read(REMISSION_CLAIM, String.class))
            .thenReturn(Optional.of("legalAid"));

        PreSubmitCallbackResponse<AsylumCase> callbackResponse =
            feePaymentHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);

        assertNotNull(callbackResponse);
        assertEquals(asylumCase, callbackResponse.getData());

        verify(asylumCase, times(1)).write(FEE_REMISSION_TYPE, "Legal Aid");
        verify(asylumCase, times(1)).clear(ASYLUM_SUPPORT_REFERENCE);
        verify(asylumCase, times(1)).clear(ASYLUM_SUPPORT_DOCUMENT);
    }

    @Test
    void should_not_return_remission_for_remissions_not_enabled() {
        when(callback.getEvent()).thenReturn(Event.EDIT_APPEAL);
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(featureToggler.getValue("remissions-feature", false)).thenReturn(false);
        when(asylumCase.read(APPEAL_TYPE, AppealType.class))
            .thenReturn(Optional.of(AppealType.PA));
        when(feePayment.aboutToSubmit(callback)).thenReturn(asylumCase);

        PreSubmitCallbackResponse<AsylumCase> callbackResponse =
            feePaymentHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);

        assertNotNull(callbackResponse);
        assertEquals(asylumCase, callbackResponse.getData());

        verify(feePayment, times(1)).aboutToSubmit(callback);
        verify(asylumCase, times(0)).write(FEE_REMISSION_TYPE, "Legal Aid");
        verify(asylumCase, times(1)).clear(EA_HU_APPEAL_TYPE_PAYMENT_OPTION);
        verify(asylumCase, times(0)).clear(PA_APPEAL_TYPE_PAYMENT_OPTION);
        verify(asylumCase, times(1)).clear(RP_DC_APPEAL_HEARING_OPTION);
        verify(asylumCase, times(1)).clear(ASYLUM_SUPPORT_REFERENCE);
        verify(asylumCase, times(1)).clear(ASYLUM_SUPPORT_DOCUMENT);
    }

    @Test
    void it_cannot_handle_callback_if_feepayment_not_enabled() {

        FeePaymentHandler feePaymentHandlerWithDisabledPayment =
            new FeePaymentHandler(true, feePayment, featureToggler);

        assertThatThrownBy(
            () -> feePaymentHandlerWithDisabledPayment.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback))
            .hasMessage("Cannot handle callback")
            .isExactlyInstanceOf(IllegalStateException.class);
    }

    @Test
    void handling_should_throw_if_cannot_actually_handle() {

        assertThatThrownBy(() -> feePaymentHandler.handle(PreSubmitCallbackStage.ABOUT_TO_START, callback))
            .hasMessage("Cannot handle callback")
            .isExactlyInstanceOf(IllegalStateException.class);

        when(callback.getEvent()).thenReturn(Event.SEND_DIRECTION);
        assertThatThrownBy(() -> feePaymentHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback))
            .hasMessage("Cannot handle callback")
            .isExactlyInstanceOf(IllegalStateException.class);
    }

    @Test
    void it_can_handle_callback() {
        when(featureToggler.getValue("remissions-feature", false)).thenReturn(true);
        feePaymentHandler = new FeePaymentHandler(true, feePayment, featureToggler);


        for (Event event : Event.values()) {

            when(callback.getEvent()).thenReturn(event);

            for (PreSubmitCallbackStage callbackStage : PreSubmitCallbackStage.values()) {

                boolean canHandle = feePaymentHandler.canHandle(callbackStage, callback);

                if ((callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT)
                    && (callback.getEvent() == Event.START_APPEAL
                    || callback.getEvent() == Event.EDIT_APPEAL
                    || callback.getEvent() == Event.PAYMENT_APPEAL)) {

                    assertTrue(canHandle);
                } else {
                    assertFalse(canHandle);
                }
            }

            reset(callback);
        }
    }

    @Test
    void it_cannot_handle_callback_if_feePayment_not_enabled() {
        when(featureToggler.getValue("remissions-feature", false)).thenReturn(false);
        feePaymentHandler = new FeePaymentHandler(false, feePayment, featureToggler);

        for (Event event : Event.values()) {

            when(callback.getEvent()).thenReturn(event);

            for (PreSubmitCallbackStage callbackStage : PreSubmitCallbackStage.values()) {
                boolean canHandle = feePaymentHandler.canHandle(callbackStage, callback);

                assertFalse(canHandle);
            }

            reset(callback);
        }
    }

    @Test
    void should_not_allow_null_arguments() {

        assertThatThrownBy(() -> feePaymentHandler.canHandle(null, callback))
            .hasMessage("callbackStage must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> feePaymentHandler.canHandle(PreSubmitCallbackStage.ABOUT_TO_START, null))
            .hasMessage("callback must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> feePaymentHandler.canHandle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, null))
            .hasMessage("callback must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> feePaymentHandler.handle(null, callback))
            .hasMessage("callbackStage must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> feePaymentHandler.handle(PreSubmitCallbackStage.ABOUT_TO_START, null))
            .hasMessage("callback must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> feePaymentHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, null))
            .hasMessage("callback must not be null")
            .isExactlyInstanceOf(NullPointerException.class);
    }


    @ParameterizedTest
    @ValueSource(strings = {"DC", "RP"})
    void should_throw_for_missing_appeal_hearing_option(String type) {

        when(callback.getEvent()).thenReturn(Event.START_APPEAL);
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(feePayment.aboutToSubmit(callback)).thenReturn(asylumCase);
        when(asylumCase.read(APPEAL_TYPE,
            AppealType.class)).thenReturn(Optional.of(AppealType.valueOf(type)));

        assertThatThrownBy(() -> feePaymentHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback))
            .isExactlyInstanceOf(IllegalStateException.class)
            .hasMessage("Appeal hearing option is not present");
    }

    @Test
    public void should_return_data_for_valid_asylumSupport_remission_type() {

        when(callback.getEvent()).thenReturn(Event.EDIT_APPEAL);
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(featureToggler.getValue("remissions-feature", false)).thenReturn(true);
        when(feePayment.aboutToSubmit(callback)).thenReturn(asylumCase);
        when(asylumCase.read(APPEAL_TYPE, AppealType.class))
            .thenReturn(Optional.of(AppealType.PA));
        when(asylumCase.read(REMISSION_TYPE, RemissionType.class))
            .thenReturn(Optional.of(RemissionType.HO_WAIVER_REMISSION));
        when(asylumCase.read(REMISSION_CLAIM, String.class))
            .thenReturn(Optional.of("asylumSupport"));

        PreSubmitCallbackResponse<AsylumCase> callbackResponse =
            feePaymentHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);

        assertNotNull(callbackResponse);
        assertEquals(asylumCase, callbackResponse.getData());

        verify(asylumCase, times(1)).write(FEE_REMISSION_TYPE, "Asylum support");
        verify(asylumCase, times(1)).clear(LEGAL_AID_ACCOUNT_NUMBER);
        verify(asylumCase, times(1)).clear(SECTION17_DOCUMENT);
        verify(asylumCase, times(1)).clear(SECTION20_DOCUMENT);
        verify(asylumCase, times(1)).clear(HOME_OFFICE_WAIVER_DOCUMENT);

        verify(asylumCase, times(0)).clear(DECISION_HEARING_FEE_OPTION);
        verify(asylumCase, times(1)).clear(HEARING_DECISION_SELECTED);
        verify(asylumCase, times(1)).clear(EA_HU_APPEAL_TYPE_PAYMENT_OPTION);
        verify(asylumCase, times(1)).clear(PA_APPEAL_TYPE_PAYMENT_OPTION);
        verify(asylumCase, times(0)).clear(PAYMENT_STATUS);
    }

    @Test
    public void should_return_data_for_valid_legalAid_remission_type() {

        when(callback.getEvent()).thenReturn(Event.EDIT_APPEAL);
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(featureToggler.getValue("remissions-feature", false)).thenReturn(true);
        when(feePayment.aboutToSubmit(callback)).thenReturn(asylumCase);
        when(asylumCase.read(APPEAL_TYPE, AppealType.class))
            .thenReturn(Optional.of(AppealType.PA));
        when(asylumCase.read(REMISSION_TYPE, RemissionType.class))
            .thenReturn(Optional.of(RemissionType.HO_WAIVER_REMISSION));
        when(asylumCase.read(REMISSION_CLAIM, String.class))
            .thenReturn(Optional.of("legalAid"));

        PreSubmitCallbackResponse<AsylumCase> callbackResponse =
            feePaymentHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);

        assertNotNull(callbackResponse);
        assertEquals(asylumCase, callbackResponse.getData());

        verify(asylumCase, times(1)).write(FEE_REMISSION_TYPE, "Legal Aid");
        verify(asylumCase, times(1)).clear(ASYLUM_SUPPORT_REFERENCE);
        verify(asylumCase, times(1)).clear(ASYLUM_SUPPORT_DOCUMENT);
        verify(asylumCase, times(1)).clear(SECTION17_DOCUMENT);
        verify(asylumCase, times(1)).clear(SECTION20_DOCUMENT);
        verify(asylumCase, times(1)).clear(HOME_OFFICE_WAIVER_DOCUMENT);

        verify(asylumCase, times(0)).clear(DECISION_HEARING_FEE_OPTION);
        verify(asylumCase, times(1)).clear(HEARING_DECISION_SELECTED);
        verify(asylumCase, times(1)).clear(EA_HU_APPEAL_TYPE_PAYMENT_OPTION);
        verify(asylumCase, times(1)).clear(PA_APPEAL_TYPE_PAYMENT_OPTION);
        verify(asylumCase, times(0)).clear(PAYMENT_STATUS);

    }

    @Test
    public void should_return_data_for_valid_section17_remission_type() {

        when(callback.getEvent()).thenReturn(Event.EDIT_APPEAL);
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(featureToggler.getValue("remissions-feature", false)).thenReturn(true);
        when(feePayment.aboutToSubmit(callback)).thenReturn(asylumCase);
        when(asylumCase.read(APPEAL_TYPE, AppealType.class))
            .thenReturn(Optional.of(AppealType.PA));
        when(asylumCase.read(REMISSION_TYPE, RemissionType.class))
            .thenReturn(Optional.of(RemissionType.HO_WAIVER_REMISSION));
        when(asylumCase.read(REMISSION_CLAIM, String.class))
            .thenReturn(Optional.of("section17"));

        PreSubmitCallbackResponse<AsylumCase> callbackResponse =
            feePaymentHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);

        assertNotNull(callbackResponse);
        assertEquals(asylumCase, callbackResponse.getData());

        verify(asylumCase, times(1)).write(FEE_REMISSION_TYPE, "Section 17");
        verify(asylumCase, times(1)).clear(LEGAL_AID_ACCOUNT_NUMBER);
        verify(asylumCase, times(1)).clear(ASYLUM_SUPPORT_REFERENCE);
        verify(asylumCase, times(1)).clear(ASYLUM_SUPPORT_DOCUMENT);
        verify(asylumCase, times(1)).clear(SECTION20_DOCUMENT);
        verify(asylumCase, times(1)).clear(HOME_OFFICE_WAIVER_DOCUMENT);

        verify(asylumCase, times(0)).clear(DECISION_HEARING_FEE_OPTION);
        verify(asylumCase, times(1)).clear(HEARING_DECISION_SELECTED);
        verify(asylumCase, times(1)).clear(EA_HU_APPEAL_TYPE_PAYMENT_OPTION);
        verify(asylumCase, times(1)).clear(PA_APPEAL_TYPE_PAYMENT_OPTION);
        verify(asylumCase, times(0)).clear(PAYMENT_STATUS);
    }

    @Test
    public void should_return_data_for_valid_section20_remission_type() {

        when(callback.getEvent()).thenReturn(Event.EDIT_APPEAL);
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(featureToggler.getValue("remissions-feature", false)).thenReturn(true);
        when(feePayment.aboutToSubmit(callback)).thenReturn(asylumCase);
        when(asylumCase.read(APPEAL_TYPE, AppealType.class))
            .thenReturn(Optional.of(AppealType.PA));
        when(asylumCase.read(REMISSION_TYPE, RemissionType.class))
            .thenReturn(Optional.of(RemissionType.HO_WAIVER_REMISSION));
        when(asylumCase.read(REMISSION_CLAIM, String.class))
            .thenReturn(Optional.of("section20"));

        PreSubmitCallbackResponse<AsylumCase> callbackResponse =
            feePaymentHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);

        assertNotNull(callbackResponse);
        assertEquals(asylumCase, callbackResponse.getData());

        verify(asylumCase, times(1)).write(FEE_REMISSION_TYPE, "Section 20");
        verify(asylumCase, times(1)).clear(LEGAL_AID_ACCOUNT_NUMBER);
        verify(asylumCase, times(1)).clear(ASYLUM_SUPPORT_REFERENCE);
        verify(asylumCase, times(1)).clear(ASYLUM_SUPPORT_DOCUMENT);
        verify(asylumCase, times(1)).clear(SECTION17_DOCUMENT);
        verify(asylumCase, times(1)).clear(HOME_OFFICE_WAIVER_DOCUMENT);

        verify(asylumCase, times(0)).clear(DECISION_HEARING_FEE_OPTION);
        verify(asylumCase, times(1)).clear(HEARING_DECISION_SELECTED);
        verify(asylumCase, times(1)).clear(EA_HU_APPEAL_TYPE_PAYMENT_OPTION);
        verify(asylumCase, times(1)).clear(PA_APPEAL_TYPE_PAYMENT_OPTION);
        verify(asylumCase, times(0)).clear(PAYMENT_STATUS);
    }

    @Test
    public void should_return_data_for_valid_homeOfficeWaiver_remission_type() {

        when(callback.getEvent()).thenReturn(Event.EDIT_APPEAL);
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(featureToggler.getValue("remissions-feature", false)).thenReturn(true);
        when(feePayment.aboutToSubmit(callback)).thenReturn(asylumCase);
        when(asylumCase.read(APPEAL_TYPE, AppealType.class))
            .thenReturn(Optional.of(AppealType.PA));
        when(asylumCase.read(REMISSION_TYPE, RemissionType.class))
            .thenReturn(Optional.of(RemissionType.HO_WAIVER_REMISSION));
        when(asylumCase.read(REMISSION_CLAIM, String.class))
            .thenReturn(Optional.of("homeOfficeWaiver"));

        PreSubmitCallbackResponse<AsylumCase> callbackResponse =
            feePaymentHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);

        assertNotNull(callbackResponse);
        assertEquals(asylumCase, callbackResponse.getData());

        verify(asylumCase, times(1)).write(FEE_REMISSION_TYPE, "Home Office fee waiver");
        verify(asylumCase, times(1)).clear(LEGAL_AID_ACCOUNT_NUMBER);
        verify(asylumCase, times(1)).clear(ASYLUM_SUPPORT_REFERENCE);
        verify(asylumCase, times(1)).clear(ASYLUM_SUPPORT_DOCUMENT);
        verify(asylumCase, times(1)).clear(SECTION17_DOCUMENT);
        verify(asylumCase, times(1)).clear(SECTION20_DOCUMENT);

        verify(asylumCase, times(0)).clear(DECISION_HEARING_FEE_OPTION);
        verify(asylumCase, times(1)).clear(HEARING_DECISION_SELECTED);
        verify(asylumCase, times(1)).clear(EA_HU_APPEAL_TYPE_PAYMENT_OPTION);
        verify(asylumCase, times(1)).clear(PA_APPEAL_TYPE_PAYMENT_OPTION);
        verify(asylumCase, times(0)).clear(PAYMENT_STATUS);
    }

    @ParameterizedTest
    @ValueSource(strings = { "EA", "HU", "PA" })
    public void should_return_data_for_valid_help_with_fees_remission_type(String type) {

        when(callback.getEvent()).thenReturn(Event.START_APPEAL);
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(featureToggler.getValue("remissions-feature", false)).thenReturn(true);
        when(feePayment.aboutToSubmit(callback)).thenReturn(asylumCase);
        when(asylumCase.read(APPEAL_TYPE, AppealType.class))
            .thenReturn(Optional.of(AppealType.valueOf(type)));
        when(asylumCase.read(REMISSION_TYPE, RemissionType.class))
            .thenReturn(Optional.of(RemissionType.HELP_WITH_FEES));

        PreSubmitCallbackResponse<AsylumCase> callbackResponse =
            feePaymentHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);

        assertNotNull(callbackResponse);
        assertEquals(asylumCase, callbackResponse.getData());

        verify(asylumCase, times(1)).write(FEE_REMISSION_TYPE, "Help with Fees");
        verify(asylumCase, times(1)).clear(REMISSION_CLAIM);
        verify(asylumCase, times(1)).clear(ASYLUM_SUPPORT_REFERENCE);
        verify(asylumCase, times(1)).clear(ASYLUM_SUPPORT_DOCUMENT);
        verify(asylumCase, times(1)).clear(LEGAL_AID_ACCOUNT_NUMBER);
        verify(asylumCase, times(1)).clear(SECTION17_DOCUMENT);
        verify(asylumCase, times(1)).clear(SECTION20_DOCUMENT);
        verify(asylumCase, times(1)).clear(HOME_OFFICE_WAIVER_DOCUMENT);

        verify(asylumCase, times(0)).clear(DECISION_HEARING_FEE_OPTION);
        verify(asylumCase, times(1)).clear(HEARING_DECISION_SELECTED);
        verify(asylumCase, times(1)).clear(EA_HU_APPEAL_TYPE_PAYMENT_OPTION);
        verify(asylumCase, times(1)).clear(PA_APPEAL_TYPE_PAYMENT_OPTION);
        verify(asylumCase, times(0)).clear(PAYMENT_STATUS);
    }

    @ParameterizedTest
    @ValueSource(strings = { "EA", "HU", "PA" })
    public void should_return_data_for_valid_exceptional_circumstances_remission_type(String type) {

        when(callback.getEvent()).thenReturn(Event.START_APPEAL);
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(featureToggler.getValue("remissions-feature", false)).thenReturn(true);
        when(feePayment.aboutToSubmit(callback)).thenReturn(asylumCase);
        when(asylumCase.read(APPEAL_TYPE, AppealType.class))
            .thenReturn(Optional.of(AppealType.valueOf(type)));
        when(asylumCase.read(REMISSION_TYPE, RemissionType.class))
            .thenReturn(Optional.of(RemissionType.EXCEPTIONAL_CIRCUMSTANCES_REMISSION));

        PreSubmitCallbackResponse<AsylumCase> callbackResponse =
            feePaymentHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);

        assertNotNull(callbackResponse);
        assertEquals(asylumCase, callbackResponse.getData());

        verify(asylumCase, times(1)).write(FEE_REMISSION_TYPE, "Exceptional circumstances");
        verify(asylumCase, times(1)).clear(REMISSION_CLAIM);
        verify(asylumCase, times(1)).clear(ASYLUM_SUPPORT_REFERENCE);
        verify(asylumCase, times(1)).clear(ASYLUM_SUPPORT_DOCUMENT);
        verify(asylumCase, times(1)).clear(LEGAL_AID_ACCOUNT_NUMBER);
        verify(asylumCase, times(1)).clear(SECTION17_DOCUMENT);
        verify(asylumCase, times(1)).clear(SECTION20_DOCUMENT);
        verify(asylumCase, times(1)).clear(HOME_OFFICE_WAIVER_DOCUMENT);

        verify(asylumCase, times(0)).clear(DECISION_HEARING_FEE_OPTION);
        verify(asylumCase, times(1)).clear(HEARING_DECISION_SELECTED);
        verify(asylumCase, times(1)).clear(EA_HU_APPEAL_TYPE_PAYMENT_OPTION);
        verify(asylumCase, times(1)).clear(PA_APPEAL_TYPE_PAYMENT_OPTION);
        verify(asylumCase, times(0)).clear(PAYMENT_STATUS);
    }
}
