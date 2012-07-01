package uk.co.squadlist.web.views;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.web.servlet.View;

public class JsonView implements View {
	
	private final JsonSerializer jsonSerializer;

	public JsonView(JsonSerializer jsonSerializer) {
		this.jsonSerializer = jsonSerializer;
	}

	@Override
	public String getContentType() {
		return "application/json";
	}

	@Override
	public void render(Map model, HttpServletRequest request, HttpServletResponse response) throws Exception {
		response.setCharacterEncoding("UTF-8");
		response.setContentType(getContentType());
    	response.setHeader("Cache-Control", "max-age=0");
    	
		final String json = jsonSerializer.serialize(model.get("data"));
		response.getWriter().write(json);		
		response.getWriter().flush();
	}
	
}
