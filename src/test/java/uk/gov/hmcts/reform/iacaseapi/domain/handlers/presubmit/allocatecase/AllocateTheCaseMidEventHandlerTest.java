package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit.allocatecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.CASE_WORKER_LOCATION_LIST;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.CASE_WORKER_NAME_LIST;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;
import lombok.Builder;
import lombok.Value;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.DynamicList;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.roleassignment.Assignment;
import uk.gov.hmcts.reform.iacaseapi.domain.service.CaseWorkerService;
import uk.gov.hmcts.reform.iacaseapi.domain.service.FeatureToggler;

@MockitoSettings(strictness = Strictness.LENIENT)
@ExtendWith(MockitoExtension.class)
class AllocateTheCaseMidEventHandlerTest {

    @Mock
    private FeatureToggler featureToggle;
    @Mock
    private CaseWorkerService caseWorkerService;
    @InjectMocks
    private AllocateTheCaseMidEventHandler handler;
    @Mock
    private Callback<AsylumCase> callback;
    @Mock
    private CaseDetails<AsylumCase> caseDetails;

    @ParameterizedTest
    @MethodSource("scenarioProvider")
    void canHandle(Scenario scenario) {

        when(callback.getEvent()).thenReturn(scenario.event);
        when(featureToggle.getValue("allocate-a-case-feature", false))
            .thenReturn(scenario.featureToggleResponse);

        boolean actualResult = handler.canHandle(scenario.preSubmitCallbackStage, callback);

        assertThat(actualResult).isEqualTo(scenario.expectedResult);
    }

    private static List<Scenario> scenarioProvider() {
        List<Scenario> scenarios = new ArrayList<>();

        Stream.of(Event.values()).forEach(event -> {

            if (Event.ALLOCATE_THE_CASE.equals(event)) {

                Scenario scenarioWithMidEvent = Scenario.builder()
                    .preSubmitCallbackStage(PreSubmitCallbackStage.MID_EVENT)
                    .featureToggleResponse(true)
                    .event(event)
                    .expectedResult(true)
                    .build();

                Scenario scenarioWithMidEventAndFalseToggle = Scenario.builder()
                    .preSubmitCallbackStage(PreSubmitCallbackStage.MID_EVENT)
                    .featureToggleResponse(false)
                    .event(event)
                    .expectedResult(false)
                    .build();

                Scenario scenarioWithAboutToSubmit = Scenario.builder()
                    .preSubmitCallbackStage(PreSubmitCallbackStage.ABOUT_TO_SUBMIT)
                    .featureToggleResponse(true)
                    .event(event)
                    .expectedResult(false)
                    .build();

                Scenario scenarioWithAboutToStart = Scenario.builder()
                    .preSubmitCallbackStage(PreSubmitCallbackStage.ABOUT_TO_START)
                    .featureToggleResponse(true)
                    .event(event)
                    .expectedResult(false)
                    .build();

                scenarios.add(scenarioWithMidEvent);
                scenarios.add(scenarioWithMidEventAndFalseToggle);
                scenarios.add(scenarioWithAboutToSubmit);
                scenarios.add(scenarioWithAboutToStart);
            } else {

                Scenario scenarioWithMidEvent = Scenario.builder()
                    .preSubmitCallbackStage(PreSubmitCallbackStage.MID_EVENT)
                    .featureToggleResponse(true)
                    .event(event)
                    .expectedResult(false)
                    .build();

                scenarios.add(scenarioWithMidEvent);

            }

        });

        return scenarios;
    }

    @Value
    @Builder
    private static class Scenario {
        Event event;
        boolean featureToggleResponse;
        PreSubmitCallbackStage preSubmitCallbackStage;
        boolean expectedResult;

    }

    @Test
    void should_not_allow_null_arguments() {
        assertThatThrownBy(() -> handler.canHandle(null, callback))
            .hasMessage("callbackStage must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> handler.handle(PreSubmitCallbackStage.MID_EVENT, null))
            .hasMessage("callback must not be null")
            .isExactlyInstanceOf(NullPointerException.class);
    }

    @Test
    void handle() {
        when(callback.getEvent()).thenReturn(Event.ALLOCATE_THE_CASE);
        when(featureToggle.getValue("allocate-a-case-feature", false))
            .thenReturn(true);

        mockCaseDetails();
        mockCaseWorkerService();

        PreSubmitCallbackResponse<AsylumCase> actualResult =
            handler.handle(PreSubmitCallbackStage.MID_EVENT, callback);

        Optional<DynamicList> caseWorkerNameList = actualResult.getData().read(
            CASE_WORKER_NAME_LIST,
            DynamicList.class
        );

        assertThat(caseWorkerNameList).isPresent();
        DynamicList expectedCaseWorkerNameList =
            new DynamicList(
                new uk.gov.hmcts.reform.iacaseapi.domain.entities.Value("", ""),
                List.of(new uk.gov.hmcts.reform.iacaseapi.domain.entities.Value(
                    "some actor id", "some caseworker name"
                ))
            );
        assertThat(caseWorkerNameList.get()).isEqualTo(expectedCaseWorkerNameList);
    }

    private void mockCaseWorkerService() {
        Assignment roleAssignment = Assignment.builder()
            .actorId("some actor id")
            .build();
        when(caseWorkerService.getRoleAssignmentsPerLocationAndClassification(
            "some location id",
            "PUBLIC")
        ).thenReturn(List.of(roleAssignment));

        when(caseWorkerService.getCaseWorkerNameForActorId("some actor id"))
            .thenReturn("some caseworker name");
    }

    private void mockCaseDetails() {
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        AsylumCase asylumCase = new AsylumCase();
        asylumCase.write(CASE_WORKER_LOCATION_LIST, "some location id");
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(caseDetails.getSecurityClassification()).thenReturn("PUBLIC");
    }

    @ParameterizedTest
    @MethodSource("exceptionHandleScenarioProvider")
    void handling_should_throw_exception_if_cannot_handle(ExceptionHandleScenario scenario) {
        when(callback.getEvent()).thenReturn(scenario.getEvent());

        assertThatThrownBy(() -> handler.handle(scenario.getPreSubmitCallbackStage(), callback))
            .hasMessage("Cannot handle callback")
            .isExactlyInstanceOf(IllegalStateException.class);
    }

    private static Stream<ExceptionHandleScenario> exceptionHandleScenarioProvider() {
        ExceptionHandleScenario scenarioWithWrongEvent = ExceptionHandleScenario.builder()
            .event(Event.ADD_CASE_NOTE)
            .preSubmitCallbackStage(PreSubmitCallbackStage.MID_EVENT)
            .build();

        ExceptionHandleScenario scenarioWithWrongCallbackStage = ExceptionHandleScenario.builder()
            .event(Event.ALLOCATE_THE_CASE)
            .preSubmitCallbackStage(PreSubmitCallbackStage.ABOUT_TO_SUBMIT)
            .build();

        return Stream.of(scenarioWithWrongEvent, scenarioWithWrongCallbackStage);
    }

    @Value
    @Builder
    private static class ExceptionHandleScenario {
        PreSubmitCallbackStage preSubmitCallbackStage;
        Event event;
    }
}