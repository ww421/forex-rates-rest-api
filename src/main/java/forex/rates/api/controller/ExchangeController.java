package forex.rates.api.controller;

import forex.rates.api.model.exchange.ExchangeRates;
import forex.rates.api.model.exchange.ExchangeTransactions;
import forex.rates.api.model.request.ExchangeRatesRequest;
import forex.rates.api.model.response.DailyExchangeResponse;
import forex.rates.api.model.response.SeriesExchangeResponse;
import forex.rates.api.service.ExchangeRatesService;
import forex.rates.api.validation.annotation.ValidAmount;
import forex.rates.api.validation.annotation.ValidBase;
import forex.rates.api.validation.annotation.ValidCurrencies;
import forex.rates.api.validation.annotation.ValidDate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;

@RestController
@RequestMapping("exchange")
public class ExchangeController {

    private final ExchangeRatesService exchangeRatesService;

    @Autowired
    public ExchangeController(ExchangeRatesService exchangeRatesService) {
	this.exchangeRatesService = exchangeRatesService;
    }

    @GetMapping("daily")
    public DailyExchangeResponse dailyExchangeResponse(
	    @ValidDate String date,
	    @ValidAmount String amount,
	    @ValidBase String from,
	    @ValidCurrencies String[] to
    ) {
	ExchangeRatesRequest exchangeRatesRequest = new ExchangeRatesRequest(from, date, to);
	ExchangeTransactions exchangeTransactions = createExchangeTransactions(amount, exchangeRatesRequest);
	return new DailyExchangeResponse(exchangeTransactions);
    }

    @GetMapping("series")
    public SeriesExchangeResponse seriesExchangeResponse(
	    @ValidDate @RequestParam String startDate,
	    @ValidDate @RequestParam String endDate,
	    @ValidAmount String amount,
	    @ValidBase String from,
	    @ValidCurrencies String[] to
    ) {
	ExchangeRatesRequest exchangeRatesRequest = new ExchangeRatesRequest(from, startDate, endDate, to);
	ExchangeTransactions exchangeTransactions = createExchangeTransactions(amount, exchangeRatesRequest);
	return new SeriesExchangeResponse(exchangeTransactions);
    }

    private ExchangeTransactions createExchangeTransactions(String amount, ExchangeRatesRequest exchangeRatesRequest) {
	ExchangeRates exchangeRates = exchangeRatesService.perform(exchangeRatesRequest);
	return new ExchangeTransactions(exchangeRates, new BigDecimal(amount));
    }

}
