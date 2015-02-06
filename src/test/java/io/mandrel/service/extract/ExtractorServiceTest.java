package io.mandrel.service.extract;

import io.mandrel.common.WebPage;
import io.mandrel.common.content.Field;
import io.mandrel.common.content.FieldExtractor;
import io.mandrel.common.content.WebPageExtractor;
import io.mandrel.common.content.selector.SelectorService;
import io.mandrel.common.script.ScriptingService;
import io.mandrel.common.store.Document;
import io.mandrel.common.store.DocumentStore;

import java.io.ByteArrayInputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class ExtractorServiceTest {

	@Mock
	private ScriptingService scriptingService;

	@Mock
	private DocumentStore dataStore;

	private SelectorService selectorService = new SelectorService();

	private ExtractorService extractorService;

	@Before
	public void init() {
		extractorService = new ExtractorService(scriptingService, selectorService);
	}

	@Test(expected = NullPointerException.class)
	public void no_matching_pattern() throws MalformedURLException {

		// Arrange
		WebPage webPage = new WebPage(new URL("http://localhost"), 200, "Ok", null, null, null);
		WebPageExtractor extractor = new WebPageExtractor();

		// Actions
		extractorService.extractFormatThenStore(webPage, extractor);

		// Asserts
	}

	@Test(expected = NullPointerException.class)
	public void no_field() throws MalformedURLException {

		// Arrange
		WebPage webPage = new WebPage(new URL("http://localhost"), 200, "Ok", null, null, null);
		WebPageExtractor extractor = new WebPageExtractor();
		extractor.setMatchingPatternsAsString(Arrays.asList(".*"));

		// Actions
		extractorService.extractFormatThenStore(webPage, extractor);

		// Asserts
	}

	@Test(expected = NullPointerException.class)
	public void no_datastore() throws MalformedURLException {

		// Arrange
		WebPage webPage = new WebPage(new URL("http://localhost"), 200, "Ok", null, null, null);
		WebPageExtractor extractor = new WebPageExtractor();
		extractor.setMatchingPatternsAsString(Arrays.asList(".*"));
		Field field = new Field();
		field.setName("date");
		extractor.setFields(Arrays.asList(field));

		// Actions
		extractorService.extractFormatThenStore(webPage, extractor);

		// Asserts
	}

	@Test(expected = NullPointerException.class)
	public void no_field_extractor() throws MalformedURLException {

		// Arrange
		WebPage webPage = new WebPage(new URL("http://localhost"), 200, "Ok", null, null, null);
		WebPageExtractor extractor = new WebPageExtractor();
		extractor.setMatchingPatternsAsString(Arrays.asList(".*"));
		extractor.setDataStore(dataStore);
		Field field = new Field();
		field.setName("date");
		extractor.setFields(Arrays.asList(field));

		// Actions
		extractorService.extractFormatThenStore(webPage, extractor);

		// Asserts
	}

	@Test(expected = NullPointerException.class)
	public void no_field_extractor_type() throws MalformedURLException {

		// Arrange
		WebPage webPage = new WebPage(new URL("http://localhost"), 200, "Ok", null, null, null);
		WebPageExtractor extractor = new WebPageExtractor();
		extractor.setMatchingPatternsAsString(Arrays.asList(".*"));
		extractor.setDataStore(dataStore);
		Field field = new Field();
		field.setName("date");
		FieldExtractor fieldExtractor = new FieldExtractor();
		fieldExtractor.setValue("");
		field.setExtractor(fieldExtractor);
		extractor.setFields(Arrays.asList(field));

		// Actions
		extractorService.extractFormatThenStore(webPage, extractor);

		// Asserts
	}

	@Test(expected = NullPointerException.class)
	public void no_field_extractor_value() throws MalformedURLException {

		// Arrange
		WebPage webPage = new WebPage(new URL("http://localhost"), 200, "Ok", null, null, null);
		WebPageExtractor extractor = new WebPageExtractor();
		extractor.setMatchingPatternsAsString(Arrays.asList(".*"));
		extractor.setDataStore(dataStore);
		Field field = new Field();
		field.setName("date");
		FieldExtractor fieldExtractor = new FieldExtractor();
		fieldExtractor.setType("xpath");
		field.setExtractor(fieldExtractor);
		extractor.setFields(Arrays.asList(field));

		// Actions
		extractorService.extractFormatThenStore(webPage, extractor);

		// Asserts
	}

	@Test
	public void simple() throws MalformedURLException {

		// Arrange
		ByteArrayInputStream stream = new ByteArrayInputStream("<test><o>value1</o></test><test><o>value2</o></test>".getBytes());

		WebPage webPage = new WebPage(new URL("http://localhost"), 200, "Ok", null, null, stream);
		WebPageExtractor extractor = new WebPageExtractor();
		extractor.setMatchingPatternsAsString(Arrays.asList(".*"));
		extractor.setDataStore(dataStore);
		Field field = new Field();
		field.setName("date");
		FieldExtractor fieldExtractor = new FieldExtractor();
		fieldExtractor.setType("xpath");
		fieldExtractor.setValue("//test/o/text()");
		field.setExtractor(fieldExtractor);
		extractor.setFields(Arrays.asList(field));

		// Actions
		extractorService.extractFormatThenStore(webPage, extractor);

		// Asserts
		Document data = new Document();
		data.put("date", Arrays.asList("value1", "value2"));
		Mockito.verify(dataStore).save(data);
	}

	@Test
	public void simple_with_mutiple_extractors() throws MalformedURLException {

		// Arrange
		ByteArrayInputStream stream = new ByteArrayInputStream("<test><o>value1</o><t>key1</t></test><test><o>value2</o></test>".getBytes());

		WebPage webPage = new WebPage(new URL("http://localhost"), 200, "Ok", null, null, stream);
		WebPageExtractor extractor = new WebPageExtractor();
		extractor.setMatchingPatternsAsString(Arrays.asList(".*"));
		extractor.setDataStore(dataStore);

		Field dateField = new Field();
		dateField.setName("date");
		FieldExtractor dateFieldExtractor = new FieldExtractor();
		dateFieldExtractor.setType("xpath");
		dateFieldExtractor.setValue("//test/o/text()");
		dateField.setExtractor(dateFieldExtractor);

		Field keyField = new Field();
		keyField.setName("key");
		FieldExtractor keyFieldExtractor = new FieldExtractor();
		keyFieldExtractor.setType("xpath");
		keyFieldExtractor.setValue("//test/t/text()");
		keyField.setExtractor(keyFieldExtractor);

		extractor.setFields(Arrays.asList(dateField, keyField));

		// Actions
		extractorService.extractFormatThenStore(webPage, extractor);

		// Asserts
		Document data = new Document();
		data.put("date", Arrays.asList("value1", "value2"));
		data.put("key", Arrays.asList("key1"));
		Mockito.verify(dataStore).save(data);
	}

	@Test
	public void multiple() throws MalformedURLException {

		// Arrange
		ByteArrayInputStream stream = new ByteArrayInputStream(
				"<test><o>value1</o><t>key1</t></test><test><o>value2</o><t>key2</t></test><test><o>value3</o></test>".getBytes());

		WebPage webPage = new WebPage(new URL("http://localhost"), 200, "Ok", null, null, stream);
		WebPageExtractor extractor = new WebPageExtractor();
		extractor.setMatchingPatternsAsString(Arrays.asList(".*"));
		extractor.setDataStore(dataStore);

		Field dateField = new Field();
		dateField.setName("date");
		FieldExtractor dateFieldExtractor = new FieldExtractor();
		dateFieldExtractor.setType("xpath");
		dateFieldExtractor.setValue("//test/o/text()");
		dateField.setExtractor(dateFieldExtractor);

		Field keyField = new Field();
		keyField.setName("key");
		FieldExtractor keyFieldExtractor = new FieldExtractor();
		keyFieldExtractor.setType("xpath");
		keyFieldExtractor.setValue("//test/t/text()");
		keyField.setExtractor(keyFieldExtractor);

		extractor.setFields(Arrays.asList(dateField, keyField));

		// Actions
		extractorService.extractFormatThenStore(webPage, extractor);

		// Asserts
		Document data1 = new Document();
		data1.put("date", Arrays.asList("value1"));
		data1.put("key", Arrays.asList("key1"));
		Mockito.verify(dataStore).save(data1);

		Document data2 = new Document();
		data2.put("date", Arrays.asList("value2"));
		data2.put("key", Arrays.asList("key2"));
		Mockito.verify(dataStore).save(data2);

		Document data3 = new Document();
		data3.put("date", Arrays.asList("value3"));
		Mockito.verify(dataStore).save(data3);
	}

}