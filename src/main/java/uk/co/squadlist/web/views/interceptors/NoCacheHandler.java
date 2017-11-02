package uk.co.squadlist.web.views.interceptors;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Component
public class NoCacheHandler extends HandlerInterceptorAdapter {

	private final static Logger log = Logger.getLogger(NoCacheHandler.class);

	public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
		String path = request.getRequestURI();
		boolean cachable = path.startsWith("/assets/");
		if (!cachable) {
			log.info("Path is not cachable; adding no cache headers: " + path);
			response.addHeader("Cache-Control", "no-cache");
		} else {
			log.info("Path is cachable: " + path);
		}
	}

}
