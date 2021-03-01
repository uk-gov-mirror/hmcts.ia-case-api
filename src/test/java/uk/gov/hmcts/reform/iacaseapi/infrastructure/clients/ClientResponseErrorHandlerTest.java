package uk.gov.hmcts.reform.iacaseapi.infrastructure.clients;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.io.*;
import java.net.URI;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.client.ClientHttpResponse;

@ExtendWith(MockitoExtension.class)
class ClientResponseErrorHandlerTest {

    @Mock private ClientHttpResponse response;

    private ClientResponseErrorHandler clientResponseErrorHandler;

    @BeforeEach
    void setUp() {

        clientResponseErrorHandler = new ClientResponseErrorHandler();
    }

    @Test
    void test_4xx_client_error() throws Exception {

        when(response.getStatusCode()).thenReturn(HttpStatus.NOT_FOUND);

        assertTrue(clientResponseErrorHandler.hasError(response));
    }

    @Test
    void test_5xx_server_error() throws Exception {

        when(response.getStatusCode()).thenReturn(HttpStatus.INTERNAL_SERVER_ERROR);

        assertTrue(clientResponseErrorHandler.hasError(response));
    }

    @Test
    void test_for_no_4xx_or_5xx_errors() throws Exception {

        when(response.getStatusCode()).thenReturn(HttpStatus.FOUND);

        assertFalse(clientResponseErrorHandler.hasError(response));
    }

    @Test
    void test_handle_error_with_url_and_http_method() throws Exception {

        URI uri = new URI("http://localhost");
        when(response.getBody()).thenReturn(new ByteArrayInputStream("Api callback error".getBytes()));

        assertThatThrownBy(() -> clientResponseErrorHandler.handleError(uri, HttpMethod.POST, response))
            .isExactlyInstanceOf(ClientIntegrationException.class);
    }

    @Test
    void test_handle_error_with_url_and_http_method_and_no_body() throws Exception {

        URI uri = new URI("http://localhost");
        when(response.getBody()).thenReturn(null);

        assertThatThrownBy(() -> clientResponseErrorHandler.handleError(uri, HttpMethod.POST, response))
            .isExactlyInstanceOf(ClientIntegrationException.class);
    }

    @Test
    void test_handle_error_with_response_only() throws Exception {

        when(response.getBody()).thenReturn(new ByteArrayInputStream("Api callback error".getBytes()));

        assertThatThrownBy(() -> clientResponseErrorHandler.handleError(response))
            .isExactlyInstanceOf(ClientIntegrationException.class);
    }

    @Test
    void test_handle_error_with_response_only_no_body() throws Exception {

        when(response.getBody()).thenReturn(null);

        assertThatThrownBy(() -> clientResponseErrorHandler.handleError(response))
            .isExactlyInstanceOf(ClientIntegrationException.class);
    }
}
