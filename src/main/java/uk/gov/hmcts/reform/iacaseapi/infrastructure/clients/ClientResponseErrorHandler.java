package uk.gov.hmcts.reform.iacaseapi.infrastructure.clients;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.web.client.DefaultResponseErrorHandler;

@Slf4j
public class ClientResponseErrorHandler extends DefaultResponseErrorHandler  {

    @Override
    public boolean hasError(ClientHttpResponse response) throws IOException {
        HttpStatus httpStatus = response.getStatusCode();
        return httpStatus.is4xxClientError() || httpStatus.is5xxServerError();
    }

    @Override
    public void handleError(URI url, HttpMethod method, ClientHttpResponse response) throws IOException {

        String responseAsString = toString(response.getBody());
        log.error("URL: {}, HttpMethod: {}, ResponseBody: {}", url, method, responseAsString);

        throw new ClientIntegrationException(responseAsString);
    }

    @Override
    public void handleError(ClientHttpResponse response) throws IOException {

        String responseAsString = toString(response.getBody());
        log.error("ResponseBody: {}", responseAsString);

        throw new ClientIntegrationException(responseAsString);
    }

    String toString(InputStream inputStream) {

        if (inputStream != null) {
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
            return reader.lines().collect(Collectors.joining(""));
        }
        
        return "Error in reading the response body as it is null";
    }
}
