package uk.co.squadlist.web.views;

import static com.google.common.truth.Truth.assertThat;
import static com.google.common.truth.Truth.assertWithMessage;

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

		assertThat(csv).startsWith("h1,h2,h3");
		assertThat(csv).containsMatch("a,\"b,c\",c");
	}

}
