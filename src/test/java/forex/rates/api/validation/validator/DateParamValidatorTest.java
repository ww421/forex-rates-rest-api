package forex.rates.api.validation.validator;

import forex.rates.api.service.DateTimeProviderService;
import forex.rates.api.validation.annotation.Date;
import junitparams.JUnitParamsRunner;
import junitparams.Parameters;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import java.lang.annotation.Annotation;
import java.time.LocalDate;
import java.util.Optional;

import static java.util.Optional.of;
import static java.util.Optional.ofNullable;
import static org.junit.Assert.*;

@RunWith(JUnitParamsRunner.class)
public class DateParamValidatorTest {

    private static final LocalDate TODAY = LocalDate.of(2001, 1, 1);
    private static final Annotation INVALID_ANNOTATION = () -> InvalidAnnotation.class;
    private static final Annotation VALID_ANNOTATION = () -> Date.class;
    private static final Class<?> INVALID_PARAMETER_TYPE = Integer.class;
    private static final Class<?> VALID_PARAMETER_TYPE = String.class;

    private @interface InvalidAnnotation {}

    private @Mock DateTimeProviderService dateTimeProviderService;

    private DateParamValidator dateParamValidator;

    @Before
    public void before() {
	MockitoAnnotations.initMocks(this);
	dateParamValidator = new DateParamValidator(dateTimeProviderService);
	Mockito.when(dateTimeProviderService.getTodaysDateAsString()).thenReturn(TODAY.toString());
	Mockito.when(dateTimeProviderService.getTodaysDate()).thenReturn(TODAY);
    }

    @Test
    @Parameters
    public void shouldSupport(Class<?> givenParameterType, Annotation[] givenAnnotations) throws Exception {
	// When
	boolean result = dateParamValidator.supports(givenParameterType, givenAnnotations);

	// Then
	assertTrue(result);
    }

    public Object[] parametersForShouldSupport() {
	return new Object[]{
		new Object[]{VALID_PARAMETER_TYPE, new Annotation[]{INVALID_ANNOTATION, VALID_ANNOTATION}}
	};
    }

    @Test
    @Parameters
    public void shouldNotSupport(Class<?> givenParameterType, Annotation[] givenAnnotations) throws Exception {
	// When
	boolean result = dateParamValidator.supports(givenParameterType, givenAnnotations);

	// Then
	assertFalse(result);
    }

    public Object[] parametersForShouldNotSupport() {
	return new Object[]{
		new Object[]{INVALID_PARAMETER_TYPE, null},
		new Object[]{INVALID_PARAMETER_TYPE, new Annotation[]{}},
		new Object[]{INVALID_PARAMETER_TYPE, new Annotation[]{INVALID_ANNOTATION}},
		new Object[]{VALID_PARAMETER_TYPE, new Annotation[]{}},
		new Object[]{VALID_PARAMETER_TYPE, new Annotation[]{INVALID_ANNOTATION}}
	};
    }

    @Test
    @Parameters
    public void shouldBeValidAndNotNull(String given) throws Exception {
	// Given
	Optional<String> givenOptional = ofNullable(given);

	// When
	String result = dateParamValidator.validate(givenOptional);

	// Then
	assertNotNull(result);
    }

    public Object[] parametersForShouldBeValidAndNotNull() {
	return new Object[]{
		new Object[]{null},
		new Object[]{TODAY.minusDays(1).toString()},
		new Object[]{TODAY.toString()}
	};
    }

    @Test(expected = IllegalArgumentException.class)
    @Parameters
    public void shouldNotBeValidAndThrowException(String given) throws Exception {
	// Given
	Optional<String> givenOptional = of(given);

	// When
	String result = dateParamValidator.validate(givenOptional);
    }

    public Object[] parametersForShouldNotBeValidAndThrowException() {
	return new Object[]{
		new Object[]{""},
		new Object[]{"01-01-2001"},
		new Object[]{TODAY.plusDays(1).toString()}
	};
    }

}