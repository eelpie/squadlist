package uk.co.squadlist.web.views;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.List;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.springframework.stereotype.Component;

import com.google.common.collect.Iterables;

@Component
public class CSVLinePrinter {

	public String printAsCSV(List<List<String>> rows, List<String> headings) throws IOException {
		String[] header = Iterables.toArray(headings, String.class);
		final CSVFormat format = CSVFormat.RFC4180.withHeader().withDelimiter(',').withHeader(header);

		final ByteArrayOutputStream out = new ByteArrayOutputStream();
		final PrintStream ps = new PrintStream(out);

		final CSVPrinter printer = new CSVPrinter(ps, format);
		for (List<String> row : rows) {
			printer.printRecord(row);
		}
		printer.close();

		final String result = new String(out.toByteArray(), "UTF8");
		return result;
	}

}
