package forex.rates.api.service.impl;

import forex.rates.api.model.ExchangeRates;
import forex.rates.api.model.ExchangeRatesRequest;
import forex.rates.api.model.Rates;
import forex.rates.api.model.entity.CurrencyDefinition;
import forex.rates.api.model.entity.CurrencyRate;
import forex.rates.api.repository.CurrencyRatesRepository;
import forex.rates.api.service.ExchangeRatesService;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ExchangeRatesServiceImpl implements ExchangeRatesService {

    private final String BASE_CURRENCY = "EUR";
    private final CurrencyRatesRepository currencyRatesRepository;

    public ExchangeRatesServiceImpl(CurrencyRatesRepository currencyRatesRepository) {
	this.currencyRatesRepository = currencyRatesRepository;
    }

    @Override
    public ExchangeRates perform(ExchangeRatesRequest request) {
	Map<LocalDate, Rates> ratesByDate = new HashMap<>();
	List<String> requestedCurrencies = new ArrayList<>(request.getCurrencies());

	// Remove data set's reference currency (if exists) as there is no such value in db.
	// If TRUE is returned, rate for this currency should be calculated out of actual base requested by user.
	boolean baseCurrencyRemoved = requestedCurrencies.remove(BASE_CURRENCY);
	List<CurrencyRate> requestedCurrencyRates = currencyRatesRepository.findAllByDateAndCurrenciesIn(requestedCurrencies, request.getStartDate());

	Rates rates = new Rates();
	BigDecimal baseExchangeRate = null;

	if (isNotDataSetsBaseCurrency(request)) {
	    CurrencyRate baseCurrencyRate = currencyRatesRepository.findOneByDateAndCurrenciesIn(request.getBase(), request.getStartDate());
	    baseExchangeRate = inverse(baseCurrencyRate.getExchangeRate(), baseCurrencyRate.getCurrency().getPrecision());

	    if (baseCurrencyRemoved) {
		rates.addRate(BASE_CURRENCY, baseExchangeRate);
	    }
	}

	for (CurrencyRate currencyRate : requestedCurrencyRates) {
	    CurrencyDefinition currencyDefinition = currencyRate.getCurrency();
	    BigDecimal exchangeRate = currencyRate.getExchangeRate();

	    if (baseExchangeRate != null) {
		exchangeRate = multiply(exchangeRate, baseExchangeRate, currencyDefinition.getPrecision());
	    }

	    rates.addRate(currencyDefinition.getCodeName(), exchangeRate);
	}

	ratesByDate.put(request.getStartDate(), rates);

	ExchangeRates result = new ExchangeRates();
	result.setStartDate(request.getStartDate());
	result.setEndDate(request.getEndDate());
	result.setBase(request.getBase());
	result.setRatesByDate(ratesByDate);
	return result;
    }

    private boolean isNotDataSetsBaseCurrency(ExchangeRatesRequest request) {
	return !request.getBase().equals(BASE_CURRENCY);
    }

    private BigDecimal multiply(BigDecimal first, BigDecimal second, int precision) {
	return first.multiply(second, new MathContext(precision + 1, RoundingMode.HALF_UP));
    }

    private BigDecimal inverse(BigDecimal exchangeRate, int precision) {
	return BigDecimal.ONE.divide(exchangeRate, precision, BigDecimal.ROUND_HALF_UP);
    }

}