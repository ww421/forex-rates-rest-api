package forex.rates.api.dataset.impl.ecb;

import forex.rates.api.dataset.DataSetContext;
import forex.rates.api.dataset.DataSetUpdate;
import forex.rates.api.dataset.CurrencyRateFactory;
import forex.rates.api.http.client.HttpClient;
import forex.rates.api.model.entity.CurrencyDefinition;
import forex.rates.api.model.entity.CurrencyRate;
import forex.rates.api.service.CurrencyDefinitionService;
import forex.rates.api.service.DateTimeProviderService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.IntStream;

import static java.util.stream.Collectors.toMap;

@Profile("european-central-bank")
@Component
@Slf4j
public class DataSetUpdateEcbImpl implements DataSetUpdate {

    private final HttpClient httpClient;
    private final DataSetContext dataSetContext;
    private final CurrencyRateFactory currencyRateFactory;
    private final CurrencyDefinitionService currencyDefinitionService;
    private final DateTimeProviderService dateTimeProviderService;

    public DataSetUpdateEcbImpl(HttpClient httpClient, DataSetContext dataSetContext, CurrencyRateFactory currencyRateFactory,
				CurrencyDefinitionService currencyDefinitionService, DateTimeProviderService dateTimeProviderService) {
	this.httpClient = httpClient;
	this.dataSetContext = dataSetContext;
	this.currencyRateFactory = currencyRateFactory;
	this.currencyDefinitionService = currencyDefinitionService;
	this.dateTimeProviderService = dateTimeProviderService;
    }

    @Override
    public List<CurrencyRate> getNewCurrencyRates() {
	List<CurrencyRate> currencyRates = new ArrayList<>();
	LocalDate ratesDate = dateTimeProviderService.getTodaysDate();

	try {
	    SAXParserFactory factory = SAXParserFactory.newInstance();
	    SAXParser saxParser = factory.newSAXParser();

	    DefaultHandler handler = new DefaultHandler() {
		private static final String CUBE_TAG = "Cube";

		public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
		    if (isInsideCubeTag(qName)) {
			Map<String, String> attributesMap = mapAttributes(attributes);
			String currency = attributesMap.get("currency");
			if (currency != null) {
			    CurrencyDefinition currencyDefinition = currencyDefinitionService.getOneByCodeName(currency);
			    Map.Entry<String, String> entry = createEntry(attributesMap, ratesDate);
			    CurrencyRate currencyRate = currencyRateFactory.getCurrencyRate(currencyDefinition, entry);
			    currencyRates.add(currencyRate);
			}
		    }
		}

		private boolean isInsideCubeTag(String qName) {
		    return CUBE_TAG.equalsIgnoreCase(qName);
		}

	    };

	    try (InputStream inputStream = getInputStream()) {
		saxParser.parse(inputStream, handler);
	    } catch (IOException e) {
		log.warn("Failed to get input stream", e);
	    }

	} catch (Exception e) {
	    e.printStackTrace();
	}

	return currencyRates;
    }

    private Map.Entry<String, String> createEntry(Map<String, String> attributesMap, LocalDate ratesDate) {
	Map<String, String> rate = Collections.singletonMap(ratesDate.toString(), attributesMap.get("rate"));
	return rate.entrySet().iterator().next();
    }

    private Map<String, String> mapAttributes(Attributes attributes) {
	return IntStream.range(0, attributes.getLength())
		.boxed()
		.collect(toMap(attributes::getLocalName, attributes::getValue));
    }

    private InputStream getInputStream() throws IOException {
	return httpClient.getInputStream(dataSetContext.getUpdateUrl());
    }

}
