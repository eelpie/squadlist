package uk.co.squadlist.web.views;

import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.List;

import org.junit.Test;

import com.google.common.collect.Lists;

public class CSVLinePrinterTest {

	@Test
	public void canRenderCSVWithHeaders() throws Exception {
		final String[] fields = {"a", "b,c", "c"};
		final List<List<String>> rows = Lists.newArrayList();
		rows.add(Lists.newArrayList(Arrays.asList(fields)));

		List<String> headers = Lists.newArrayList("h1", "h2", "h3");
		final String csv = new CSVLinePrinter().printAsCSV(rows, headers);

		assertTrue(csv.startsWith("h1,h2,h3"));
		assertTrue(csv.contains("a,\"b,c\",c"));
	}

}
