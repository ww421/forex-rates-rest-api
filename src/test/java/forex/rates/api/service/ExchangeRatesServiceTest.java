package forex.rates.api.service;

import forex.rates.api.model.ExchangeRates;
import forex.rates.api.model.ExchangeRatesRequest;
import forex.rates.api.model.Rates;
import forex.rates.api.model.entity.CurrencyDefinition;
import forex.rates.api.model.entity.CurrencyRate;
import forex.rates.api.repository.CurrencyRatesRepository;
import forex.rates.api.service.impl.ExchangeRatesServiceImpl;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;

import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

public class ExchangeRatesServiceTest {

    private final LocalDate DATE_2010_01_01 = LocalDate.of(2010, 1, 1);
    private final CurrencyDefinition USD_DEFINITION = createCurrencyDefinition("USD", 4);
    private final CurrencyDefinition PLN_DEFINITION = createCurrencyDefinition("PLN", 4);
    private final CurrencyRate USD_RATE = createCurrencyRate(USD_DEFINITION, new BigDecimal("1.4902"), DATE_2010_01_01);
    private final CurrencyRate PLN_RATE = createCurrencyRate(PLN_DEFINITION, new BigDecimal("1.1002"), DATE_2010_01_01);

    private @Mock CurrencyRatesRepository currencyRatesRepository;
    private ExchangeRatesService service;

    @Before
    public void before() {
	MockitoAnnotations.initMocks(this);
	service = new ExchangeRatesServiceImpl(currencyRatesRepository);
    }

    @Test
    public void shouldGetRatesForUSD_withBaseEUR() throws Exception {
	// Given
	List<CurrencyRate> currencyRates = new ArrayList<>();
	currencyRates.add(createCurrencyRate(USD_DEFINITION, new BigDecimal("1.4902"), DATE_2010_01_01));
	when(currencyRatesRepository.findAllByDateAndCurrenciesIn(Arrays.asList("USD"), DATE_2010_01_01)).thenReturn(currencyRates);
	ExchangeRatesRequest request = new ExchangeRatesRequest("EUR", "2010-01-01", new String[]{"USD"});

	// When
	ExchangeRates result = service.perform(request);

	// Then
	assertThat(result.getBase()).isEqualTo("EUR");
	assertThat(result.getStartDate()).isEqualTo(DATE_2010_01_01);
	assertThat(result.getEndDate()).isEqualTo(DATE_2010_01_01);
	assertThat(result.getRatesByDate().get(DATE_2010_01_01)).isEqualTo(createRates("USD", "1.4902"));
    }

    @Test
    public void shouldGetRatesForEUR_withBaseUSD() throws Exception {
	// Given
	when(currencyRatesRepository.findAllByDateAndCurrenciesIn(singletonList(""), DATE_2010_01_01)).thenReturn(Collections.emptyList());
	when(currencyRatesRepository.findOneByDateAndCurrenciesIn("USD", DATE_2010_01_01)).thenReturn(USD_RATE);
	ExchangeRatesRequest request = new ExchangeRatesRequest("USD", "2010-01-01", new String[]{"EUR"});

	// When
	ExchangeRates result = service.perform(request);

	// Then
	assertThat(result.getBase()).isEqualTo("USD");
	assertThat(result.getStartDate()).isEqualTo(DATE_2010_01_01);
	assertThat(result.getEndDate()).isEqualTo(DATE_2010_01_01);
	assertThat(result.getRatesByDate().get(DATE_2010_01_01)).isEqualTo(createRates("EUR", "0.6711"));
    }

    @Test
    public void shouldGetRatesForUSD_withBasePLN() throws Exception {
	// Given
	List<CurrencyRate> currencyRates = new ArrayList<>();
	currencyRates.add(createCurrencyRate(USD_DEFINITION, new BigDecimal("1.4902"), DATE_2010_01_01));
	when(currencyRatesRepository.findAllByDateAndCurrenciesIn(singletonList("USD"), DATE_2010_01_01)).thenReturn(currencyRates);
	when(currencyRatesRepository.findOneByDateAndCurrenciesIn("PLN", DATE_2010_01_01)).thenReturn(PLN_RATE);
	ExchangeRatesRequest request = new ExchangeRatesRequest("PLN", "2010-01-01", new String[]{"USD"});

	// When
	ExchangeRates result = service.perform(request);

	// Then
	assertThat(result.getBase()).isEqualTo("PLN");
	assertThat(result.getStartDate()).isEqualTo(DATE_2010_01_01);
	assertThat(result.getEndDate()).isEqualTo(DATE_2010_01_01);
	assertThat(result.getRatesByDate().get(DATE_2010_01_01)).isEqualTo(createRates("USD", "1.3544"));
    }

    @Test
    public void shouldGetRatesForUSDPLN_withBaseEUR() throws Exception {
	// Given
	List<CurrencyRate> currencyRates = new ArrayList<>(Arrays.asList(USD_RATE, PLN_RATE));
	when(currencyRatesRepository.findAllByDateAndCurrenciesIn(Arrays.asList("USD", "PLN"), DATE_2010_01_01)).thenReturn(currencyRates);
	ExchangeRatesRequest request = new ExchangeRatesRequest("EUR", "2010-01-01", new String[]{"USD", "PLN"});

	// When
	ExchangeRates result = service.perform(request);

	// Then
	assertThat(result.getBase()).isEqualTo("EUR");
	assertThat(result.getStartDate()).isEqualTo(DATE_2010_01_01);
	assertThat(result.getEndDate()).isEqualTo(DATE_2010_01_01);
	assertThat(result.getRatesByDate().get(DATE_2010_01_01)).isEqualTo(createRates("USD", "1.4902", "PLN", "1.1002"));
    }

    @Test
    public void shouldGetRatesForEURUSD_withBasePLN() throws Exception {
	// Given
	List<CurrencyRate> currencyRates = new ArrayList<>(Arrays.asList(USD_RATE));
	when(currencyRatesRepository.findOneByDateAndCurrenciesIn("PLN", DATE_2010_01_01)).thenReturn(PLN_RATE);
	when(currencyRatesRepository.findAllByDateAndCurrenciesIn(Arrays.asList("USD"), DATE_2010_01_01)).thenReturn(currencyRates);
	ExchangeRatesRequest request = new ExchangeRatesRequest("PLN", "2010-01-01", new String[]{"EUR", "USD"});

	// When
	ExchangeRates result = service.perform(request);

	// Then
	assertThat(result.getBase()).isEqualTo("PLN");
	assertThat(result.getStartDate()).isEqualTo(DATE_2010_01_01);
	assertThat(result.getEndDate()).isEqualTo(DATE_2010_01_01);
	assertThat(result.getRatesByDate().get(DATE_2010_01_01)).isEqualTo(createRates("EUR", "0.9089", "USD", "1.3544"));
    }

    private CurrencyRate createCurrencyRate(CurrencyDefinition currencyDef, BigDecimal exchangeRate, LocalDate date) {
	CurrencyRate currencyRate = new CurrencyRate();
	currencyRate.setExchangeRate(exchangeRate);
	currencyRate.setCurrency(currencyDef);
	currencyRate.setDate(date);
	return currencyRate;
    }

    private CurrencyDefinition createCurrencyDefinition(String codeName, int precision) {
	CurrencyDefinition currencyDefinition = new CurrencyDefinition();
	currencyDefinition.setCodeName(codeName);
	currencyDefinition.setPrecision(precision);
	return currencyDefinition;
    }

    private Rates createRates(String... currencyValuePairs) {
	Rates expectedRates = new Rates();
	Iterator<String> iterator = Arrays.stream(currencyValuePairs).iterator();
	while (iterator.hasNext()) {
	    expectedRates.addRate(iterator.next(), new BigDecimal(iterator.next()));
	}
	return expectedRates;
    }

}
