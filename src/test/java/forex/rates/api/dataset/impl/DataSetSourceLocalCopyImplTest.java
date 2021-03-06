package forex.rates.api.dataset.impl;

import forex.rates.api.dataset.DataSetContext;
import forex.rates.api.http.client.HttpClient;
import forex.rates.api.service.DateTimeProviderService;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.io.File;
import java.io.InputStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class DataSetSourceLocalCopyImplTest {

    private final String EXPECTED_STRING = "EXPECTED_STRING";

    private @Mock DataSetContext dataSetContext;
    private @Mock HttpClient httpClient;
    private @Mock DateTimeProviderService dateTimeProviderService;

    private File actualFile;

    @Before
    public void setUpMocks() throws Exception {
	MockitoAnnotations.initMocks(this);
	InputStream expectedInputStream = IOUtils.toInputStream(EXPECTED_STRING, "UTF-8");
	when(httpClient.getInputStream(anyString())).thenReturn(expectedInputStream);
	when(dateTimeProviderService.getCurrentTimestamp()).thenReturn(1L);
	when(dataSetContext.getSourceLocalCopyPath()).thenReturn("src/test/resources");
	when(dataSetContext.getSourceLocalCopyPrefix()).thenReturn("testDataSetSource");
	when(dataSetContext.getSourceLocalCopyExtension()).thenReturn("xml");
    }

    @Test
    public void shouldSaveExpectedInputStreamToTheFile() throws Exception {
	// Given
	DataSetSourceLocalCopyImpl dataSetSourceLocalCopy =
		new DataSetSourceLocalCopyImpl(dataSetContext, httpClient, dateTimeProviderService);

	// When
	actualFile = dataSetSourceLocalCopy.getFile();

	// Then
	assertThat(actualFile).isNotNull();
	byte[] actualBytes = FileUtils.readFileToByteArray(actualFile);
	assertThat(actualBytes).containsExactly(EXPECTED_STRING.getBytes());
	verify(dataSetContext).getSourceUrl();
    }

    @After
    public void deleteFile() throws Exception {
	actualFile.delete();
    }

}