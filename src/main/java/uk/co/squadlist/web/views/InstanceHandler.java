package uk.co.squadlist.web.views;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import uk.co.squadlist.web.api.InstanceSpecificApiClient;

public class InstanceHandler extends HandlerInterceptorAdapter {
	
	private final InstanceSpecificApiClient api;
	
	@Autowired
	public InstanceHandler(InstanceSpecificApiClient api) {
		this.api = api;
	}
	
	public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView mv) throws Exception {		
    	mv.addObject("instance", api.getInstance());	// TODO limit to HTML requests
	}

}
