package uk.co.squadlist.web.views;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import uk.co.squadlist.web.context.Context;
import uk.co.squadlist.web.localisation.text.PropertiesFileParser;

import com.google.common.collect.Maps;

public class TextHelperTest {

	@Mock
	private PropertiesFileParser propertiesFileParser;
	@Mock
	private Context context;
	private TextHelper textHelper;

	@Before
	public void setup() {
		MockitoAnnotations.initMocks(this);
		
		Map<String, String> englishKeys = Maps.newHashMap();
		englishKeys.put("login", "Login");
		englishKeys.put("require.access", "If you are a {} member who requires access to this copy of Squadlist please contact your coach.");
		when(propertiesFileParser.readTextPropertiesFromFile("en.properties")).thenReturn(englishKeys);
		
		Map<String, String> frenchKeys = Maps.newHashMap();
		frenchKeys.put("login", "Connexion");
		when(propertiesFileParser.readTextPropertiesFromFile("fr.properties")).thenReturn(frenchKeys);
		
		this.textHelper = new TextHelper(propertiesFileParser, context);
	}
	
	@Test
	public void canResolveLocalisedTextByKey() throws Exception {
		assertEquals("Login", textHelper.text("login"));
	}
		
	@Test
	public void canSubstitutePlaceHolderValues() throws Exception {		
		assertEquals("If you are a Twickenham member who requires access to this copy of Squadlist please contact your coach.", textHelper.text("require.access", "Twickenham"));
	}
	
	@Test
	public void shouldUseFrenchTextForFrenchContexts() throws Exception {	
		when(context.getTimeZone()).thenReturn("Europe/Paris");
		
		final String text = textHelper.text("login");
		
		assertEquals("Connexion", text);		
	}
	
}
