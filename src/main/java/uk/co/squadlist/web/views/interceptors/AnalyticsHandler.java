package uk.co.squadlist.web.views.interceptors;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Component
public class AnalyticsHandler extends HandlerInterceptorAdapter {

	@Value("${googleAnalyticsAccount}")
	private String googleAnalyticsAccount;

	public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView mv) throws Exception {
    if (mv != null) {
				mv.addObject("googleAnalyticsAccount", googleAnalyticsAccount);
    }
	}

}
