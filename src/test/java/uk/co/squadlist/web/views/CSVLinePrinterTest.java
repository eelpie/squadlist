package uk.co.squadlist.web.views;

import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.List;

import org.junit.Test;

import com.google.common.collect.Lists;

public class CSVLinePrinterTest {

	@Test
	public void canRenderCSVLine() throws Exception {		
		final String[] fields = {"a", "b,c", "c"};
		final List<List<String>> rows = Lists.newArrayList();
		rows.add(Lists.newArrayList(Arrays.asList(fields)));
		
		final String cvs = new CSVLinePrinter().printAsCSVLine(rows);
		
		assertTrue(cvs.contains("a,\"b,c\",c"));
	}

}
