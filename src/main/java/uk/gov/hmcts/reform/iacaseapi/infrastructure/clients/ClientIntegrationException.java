package uk.gov.hmcts.reform.iacaseapi.infrastructure.clients;

public class ClientIntegrationException extends RuntimeException {

    public ClientIntegrationException(String message) {
        super(message);
    }
}
