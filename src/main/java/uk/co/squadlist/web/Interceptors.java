package uk.co.squadlist.web;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;
import uk.co.squadlist.web.views.interceptors.InstanceHandler;
import uk.co.squadlist.web.views.interceptors.NoCacheHandler;

@ComponentScan(basePackages="uk.co.squadlist.web")
@Configuration
public class Interceptors extends WebMvcConfigurerAdapter {

	@Autowired
	private InstanceHandler instanceHandler;

	@Autowired
	private NoCacheHandler noCacheHandler;

	@Override
	public void addInterceptors(InterceptorRegistry registry) {
		registry.addInterceptor(instanceHandler);
		registry.addInterceptor(noCacheHandler);
	}
	
}
