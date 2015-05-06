package uk.co.squadlist.web.views;

import static org.junit.Assert.assertEquals;

import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import uk.co.squadlist.web.localisation.text.PropertiesFileParser;

import com.google.common.collect.Maps;

public class TextHelperTest {

	@Mock
	private PropertiesFileParser propertiesFileParser;

	@Before
	public void setup() {
		MockitoAnnotations.initMocks(this);
	}
		
	@Test
	public void canSubstitutePlaceHolderValues() throws Exception {		
		Map<String, String> keys = Maps.newHashMap();
		keys.put("require.access", "If you are a {} member who requires access to this copy of Squadlist please contact your coach.");
		Mockito.when(propertiesFileParser.readTextPropertiesFromFile("EN.properties")).thenReturn(keys);		
		TextHelper textHelper = new TextHelper(propertiesFileParser);

		final String text = textHelper.text("require.access", "Twickenham");
		
		assertEquals("If you are a Twickenham member who requires access to this copy of Squadlist please contact your coach.", text);
	}
	
}
