package ru.extoozy.currency.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.extoozy.currency.service.CbrService;

import java.math.BigDecimal;

@RestController
@RequestMapping("/api/v1/currency")
@RequiredArgsConstructor
public class CurrencyController {

    private final CbrService currencyService;

    @GetMapping("/rate/{code}")
    public BigDecimal currencyRate(@PathVariable("code") String code) {
        return currencyService.requestByCurrencyCode(code);
    }

}
