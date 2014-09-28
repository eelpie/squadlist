package uk.co.squadlist.web.views;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.springframework.stereotype.Component;

@Component
public class CSVLinePrinter {

	public String printAsCSVLine(Iterable<String> fields) throws IOException {
		final CSVFormat format = CSVFormat.RFC4180.withHeader().withDelimiter(',');
		
		final ByteArrayOutputStream out = new ByteArrayOutputStream();
		final PrintStream ps = new PrintStream(out);
		
		final CSVPrinter printer = new CSVPrinter(ps, format);
		printer.printRecord(fields);
		printer.close();

		final String result = new String(out.toByteArray(), "UTF8");
		return result;
	}

}
