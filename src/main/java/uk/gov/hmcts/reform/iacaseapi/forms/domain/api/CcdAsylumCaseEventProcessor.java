package uk.gov.hmcts.reform.iacaseapi.forms.domain.api;

import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import com.google.common.collect.ImmutableMap;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.iacaseapi.forms.domain.entities.Event;
import uk.gov.hmcts.reform.iacaseapi.forms.domain.entities.EventWithCaseData;
import uk.gov.hmcts.reform.iacaseapi.shared.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.shared.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacaseapi.shared.domain.security.AccessTokenProvider;
import uk.gov.hmcts.reform.iacaseapi.shared.domain.security.UserAndRolesProvider;

@Service
public class CcdAsylumCaseEventProcessor {

    private static final String START_EVENT_PATH_TEMPLATE = "/caseworkers/{uid}/jurisdictions/{jid}/case-types/{ctid}/cases/{cid}/event-triggers/{etid}/token";
    private static final String COMPLETE_EVENT_PATH_TEMPLATE = "/caseworkers/{uid}/jurisdictions/{jid}/case-types/{ctid}/cases/{cid}/events";
    private static final String SERVICE_AUTHORIZATION = "ServiceAuthorization";

    private final String ccdBaseUrl;
    private final RestTemplate restTemplate;
    private final AccessTokenProvider accessTokenProvider;
    private final AuthTokenGenerator serviceAuthorizationTokenGenerator;
    private final UserAndRolesProvider userAndRolesProvider;

    public CcdAsylumCaseEventProcessor(
        @Value("${ccdApi.baseUrl}") String ccdBaseUrl,
        @Autowired RestTemplate restTemplate,
        @Autowired AccessTokenProvider accessTokenProvider,
        @Autowired AuthTokenGenerator serviceAuthorizationTokenGenerator,
        @Autowired UserAndRolesProvider userAndRolesProvider
    ) {
        this.ccdBaseUrl = ccdBaseUrl;
        this.restTemplate = restTemplate;
        this.accessTokenProvider = accessTokenProvider;
        this.serviceAuthorizationTokenGenerator = serviceAuthorizationTokenGenerator;
        this.userAndRolesProvider = userAndRolesProvider;
    }

    @Retryable
    public EventWithCaseData<AsylumCase> startEvent(
        final String caseId,
        Event event
    ) {
        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.AUTHORIZATION, accessTokenProvider.getAccessToken());
        headers.add(SERVICE_AUTHORIZATION, serviceAuthorizationTokenGenerator.generate());
        headers.set(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_UTF8_VALUE);
        headers.set(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_UTF8_VALUE);

        HttpEntity<?> request =
            new HttpEntity<>(headers);

        StartEvent startEvent = restTemplate
            .exchange(
                ccdBaseUrl + START_EVENT_PATH_TEMPLATE,
                HttpMethod.GET,
                request,
                new ParameterizedTypeReference<StartEvent>() {},
                ImmutableMap.of(
                    "uid", userAndRolesProvider.getUserId(),
                    "jid", "IA",
                    "ctid", "Asylum",
                    "cid", caseId,
                    "etid", event.getId()
                )
            )
            .getBody();

        return new EventWithCaseData<>(
            event,
            startEvent.getToken(),
            startEvent.getCaseDetails().getCaseData()
        );
    }

    @Retryable
    public CaseDetails<AsylumCase> completeEvent(
        final String caseId,
        EventWithCaseData<AsylumCase> eventWithCaseData
    ) {
        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.AUTHORIZATION, accessTokenProvider.getAccessToken());
        headers.add(SERVICE_AUTHORIZATION, serviceAuthorizationTokenGenerator.generate());
        headers.set(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_UTF8_VALUE);
        headers.set(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_UTF8_VALUE);

        HttpEntity<EventWithCaseData<AsylumCase>> request =
            new HttpEntity<>(eventWithCaseData, headers);

        return restTemplate
            .exchange(
                ccdBaseUrl + COMPLETE_EVENT_PATH_TEMPLATE,
                HttpMethod.POST,
                request,
                new ParameterizedTypeReference<CaseDetails<AsylumCase>>() {},
                ImmutableMap.of(
                    "uid", userAndRolesProvider.getUserId(),
                    "jid", "IA",
                    "ctid", "Asylum",
                    "cid", caseId
                )
            )
            .getBody();
    }

    @JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
    private static class StartEvent {

        private CaseDetails<AsylumCase> caseDetails;
        private String eventId;
        private String token;

        private StartEvent() {
            // noop -- for deserializer
        }

        public CaseDetails<AsylumCase> getCaseDetails() {
            return caseDetails;
        }

        public String getEventId() {
            return eventId;
        }

        public String getToken() {
            return token;
        }
    }
}