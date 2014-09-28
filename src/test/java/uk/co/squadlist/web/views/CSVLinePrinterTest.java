package uk.co.squadlist.web.views;

import static org.junit.Assert.assertTrue;

import java.util.Arrays;

import org.junit.Test;

public class CSVLinePrinterTest {

	@Test
	public void canRenderCSVLine() throws Exception {		
		final String[] fields = {"a", "b,c", "c"};
		
		String cvs = new CSVLinePrinter().printAsCSVLine(Arrays.asList(fields));

		assertTrue(cvs.contains("a,\"b,c\",c"));
	}

}
