package ru.extoozy.currency.service;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.extoozy.currency.client.HttpCurrencyDateRateClient;
import ru.extoozy.currency.schema.ValCurs;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import java.io.StringReader;
import java.math.BigDecimal;
import java.text.NumberFormat;
import java.text.ParseException;
import java.time.Duration;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

@Service
public class CbrService {

    private final HttpCurrencyDateRateClient currencyClient;

    private final Cache<LocalDate, Map<String, BigDecimal>> cache;

    @Autowired
    public CbrService(HttpCurrencyDateRateClient currencyClient) {
        this.cache = CacheBuilder.newBuilder()
                .expireAfterWrite(Duration.of(5, ChronoUnit.SECONDS))
                .build();
        this.currencyClient = currencyClient;
    }

    public BigDecimal requestByCurrencyCode(String code) {
        try {
            return cache.get(LocalDate.now(), this::callAllByCurrentDate).get(code);
        } catch (ExecutionException e) {
            throw new RuntimeException(e);
        }
    }

    private Map<String, BigDecimal> callAllByCurrentDate() {
        var xml = currencyClient.requestByDate(LocalDate.now());
        ValCurs response = unmarshall(xml);
        return response.getValute().stream()
                .collect(Collectors.toMap(
                                ValCurs.Valute::getCharCode,
                                valute -> parseWithLocale(valute.getValue())
                        )
                );
    }

    private ValCurs unmarshall(String xml) {
        try (StringReader reader = new StringReader(xml)) {
            JAXBContext context = JAXBContext.newInstance(ValCurs.class);
            return (ValCurs) context.createUnmarshaller().unmarshal(reader);
        } catch (JAXBException e) {
            throw new RuntimeException(e);
        }
    }


    private BigDecimal parseWithLocale(String currency) {
        try {
            double v = NumberFormat.getNumberInstance(Locale.getDefault()).parse(currency).doubleValue();
            return BigDecimal.valueOf(v);
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
    }

}
