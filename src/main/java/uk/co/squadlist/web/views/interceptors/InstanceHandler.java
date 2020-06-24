package uk.co.squadlist.web.views.interceptors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;
import uk.co.squadlist.web.api.SquadlistApi;
import uk.co.squadlist.web.api.SquadlistApiFactory;
import uk.co.squadlist.web.context.InstanceConfig;
import uk.co.squadlist.web.model.Instance;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Component
public class InstanceHandler extends HandlerInterceptorAdapter {

    private final SquadlistApi squadlistApi;
    private final InstanceConfig instanceConfig;

    @Autowired
    public InstanceHandler(SquadlistApiFactory squadlistApiFactory, InstanceConfig instanceConfig) throws IOException {
        this.squadlistApi = squadlistApiFactory.createClient();
        this.instanceConfig = instanceConfig;
    }

    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView mv) throws Exception {
        if (mv != null) {
            final Instance instance = squadlistApi.getInstance(instanceConfig.getInstance());
            if (instance != null) {
                mv.addObject("instance", instance);    // TODO limit to HTML requests
            }
        }
    }

}
