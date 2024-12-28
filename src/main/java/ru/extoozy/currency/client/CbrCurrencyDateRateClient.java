package ru.extoozy.currency.client;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;
import ru.extoozy.currency.config.CurrencyClientConfig;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Component
@RequiredArgsConstructor
public class CbrCurrencyDateRateClient implements HttpCurrencyDateRateClient {
    private static final String DATE_PATTERN = "dd/MM/yyyy";

    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern(DATE_PATTERN);

    private final CurrencyClientConfig clientConfig;

    @Override
    public String requestByDate(LocalDate date) {
        var baseUrl = clientConfig.getUrl();
        var client = HttpClient.newHttpClient();
        var url = buildUriRequest(baseUrl, date);
        try {
            HttpRequest request = HttpRequest.newBuilder().uri(url).build();
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            return response.body();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private URI buildUriRequest(String baseUrl, LocalDate date) {
        return UriComponentsBuilder.fromUriString(baseUrl)
                .queryParam("date_req", DATE_TIME_FORMATTER.format(date))
                .build()
                .toUri();
    }
}
