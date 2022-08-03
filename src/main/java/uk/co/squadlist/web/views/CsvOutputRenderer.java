package uk.co.squadlist.web.views;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class CsvOutputRenderer {

	private CSVLinePrinter csvLinePrinter;

	@Autowired
	public CsvOutputRenderer(CSVLinePrinter csvLinePrinter) {
		this.csvLinePrinter = csvLinePrinter;
	}

	public void renderCsvResponse(HttpServletResponse response, final List<String> headings, final List<List<String>> rows) throws IOException {
		final String output = csvLinePrinter.printAsCSV(rows, headings);
    	response.setContentType("text/csv");
    	PrintWriter writer = response.getWriter();
		writer.print(output);
		writer.flush();
	}

}
