package forex.rates.api.service;

import forex.rates.api.model.ExchangeRates;

import java.time.LocalDate;
import java.util.List;

public interface ExchangeRatesService {

    ExchangeRates getExchangeRatesFor(String base, List<String> currencies, String date);

}
