package forex.rates.api.validation.validator;

import forex.rates.api.configuration.DataSetContext;
import forex.rates.api.service.AvailableCurrenciesService;
import forex.rates.api.validation.annotation.Base;
import org.springframework.stereotype.Component;

import java.lang.annotation.Annotation;
import java.util.Arrays;
import java.util.Optional;

@Component
public class BaseParamValidator implements ParamValidator<String> {

    private final DataSetContext dataSetContext;
    private final AvailableCurrenciesService availableCurrenciesService;

    private String message = "The base you requested is invalid: ";

    public BaseParamValidator(DataSetContext dataSetContext, AvailableCurrenciesService availableCurrenciesService) {
	this.dataSetContext = dataSetContext;
	this.availableCurrenciesService = availableCurrenciesService;
    }

    @Override
    public void setMessage(String message) {
	this.message = message;
    }

    @Override
    public boolean supports(Class<?> parameterType, Annotation... annotations) {
	if (String.class.isAssignableFrom(parameterType)) {
	    return Arrays.stream(annotations)
		    .map(Annotation::annotationType)
		    .anyMatch(Base.class::equals);
	}
	return false;
    }

    @Override
    public String validate(Optional<String> object) {
	return object.map(String::toUpperCase)
		.filter(this::isValidOrElseThrow)
		.orElse(getDefaultValue());
    }

    private boolean isValidOrElseThrow(String currency) {
	if (isNotInAvailableCurrencies(currency)) {
	    throw new IllegalArgumentException(message + currency);
	}
	return true;
    }

    private boolean isNotInAvailableCurrencies(String currency) {
	return !availableCurrenciesService.getCodeList().contains(currency);
    }

    private String getDefaultValue() {
	return dataSetContext.getBaseCurrency();
    }

}
