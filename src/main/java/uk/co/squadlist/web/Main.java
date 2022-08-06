package uk.co.squadlist.web;

import com.google.common.collect.Maps;
import org.apache.velocity.app.Velocity;
import org.apache.velocity.spring.VelocityEngineFactoryBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.boot.web.servlet.MultipartConfigFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.util.unit.DataSize;
import org.springframework.web.context.request.RequestContextListener;
import org.springframework.web.filter.CharacterEncodingFilter;
import org.springframework.web.multipart.commons.CommonsMultipartResolver;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import uk.co.eelpieconsulting.spring.views.velocity.VelocityViewResolver;
import uk.co.squadlist.web.auth.LoggedInUserService;
import uk.co.squadlist.web.urls.UrlBuilder;
import uk.co.squadlist.web.views.CssHelper;
import uk.co.squadlist.web.views.DateHelper;
import uk.co.squadlist.web.views.SquadNamesHelper;
import uk.co.squadlist.web.views.TextHelper;

import javax.servlet.MultipartConfigElement;
import java.util.Map;
import java.util.Properties;

@ComponentScan(basePackages="uk.co.squadlist.web,uk.co.eelpieconsulting.spring")
@Configuration
@EnableWebMvc
@EnableScheduling
@EnableAutoConfiguration
public class Main {

	private static ApplicationContext ctx;

    public static void main(String[] args) throws Exception {
    	ctx = SpringApplication.run(Main.class, args);     
    }

	@Bean
    public CommonsMultipartResolver multipartResolver(@Value("${maximum.upload}") Long maxUploadSize) {
    	final CommonsMultipartResolver multipartResolver = new CommonsMultipartResolver();
    	multipartResolver.setMaxUploadSize(maxUploadSize);
		return multipartResolver;    	
    }
    
    @Bean
    MultipartConfigElement multipartConfigElement() {
        MultipartConfigFactory factory = new MultipartConfigFactory();
		DataSize tenMegabytes = DataSize.ofMegabytes(10);
		factory.setMaxFileSize(tenMegabytes);
        factory.setMaxRequestSize(tenMegabytes);
        return factory.createMultipartConfig();
    }

	@Bean
	public FilterRegistrationBean filterRegistrationBean() {
		FilterRegistrationBean registrationBean = new FilterRegistrationBean();
		CharacterEncodingFilter characterEncodingFilter = new CharacterEncodingFilter();
		registrationBean.setFilter(characterEncodingFilter);
		characterEncodingFilter.setEncoding("UTF-8");
		characterEncodingFilter.setForceEncoding(true);
		registrationBean.setOrder(Integer.MIN_VALUE);
		registrationBean.addUrlPatterns("/*");
		return registrationBean;
	}

	@Bean
	public VelocityEngineFactoryBean velocityEngineFactoryBean() {
		VelocityEngineFactoryBean velocityEngineFactory = new VelocityEngineFactoryBean();

		Properties velocityProperties = new Properties();
		velocityProperties.setProperty(Velocity.INPUT_ENCODING, "UTF-8");
		velocityProperties.setProperty(Velocity.EVENTHANDLER_REFERENCEINSERTION, "org.apache.velocity.app.event.implement.EscapeHtmlReference");
		velocityProperties.setProperty("resource.loader", "class");
		velocityProperties.setProperty("class.resource.loader.class", "org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader");
		velocityProperties.setProperty("velocimacro.library", "spring.vm");

		velocityProperties.setProperty("resource.loader.class.cache", "true");
		// When resource.manager.cache.default_size is set to 0, then the default implementation uses the standard Java ConcurrentHashMap.
		velocityProperties.setProperty("resource.manager.cache.default_size", "0");

		velocityEngineFactory.setVelocityProperties(velocityProperties);
		return velocityEngineFactory;
	}

	@Bean
	public VelocityViewResolver velocityViewResolver(CssHelper cssHelper,
													 DateHelper dateHelper,
													 LoggedInUserService loggedInUserService,
													 SquadNamesHelper squadNamesHelper,
													 TextHelper textHelper,
													 UrlBuilder urlBuilder) {
		final VelocityViewResolver viewResolver = new VelocityViewResolver();
		viewResolver.setCache(true);
		viewResolver.setSuffix(".vm");
		viewResolver.setContentType("text/html;charset=UTF-8");

		final Map<String, Object> attributes = Maps.newHashMap();
		attributes.put("cssHelper", cssHelper);
		attributes.put("dateHelper", dateHelper);
		attributes.put("loggedInUserService", loggedInUserService);
		attributes.put("squadNamesHelper", squadNamesHelper);
		attributes.put("text", textHelper);
		attributes.put("urlBuilder", urlBuilder);
		viewResolver.setAttributesMap(attributes);
		return viewResolver;
	}

	@Bean
	@ConditionalOnMissingBean(RequestContextListener.class)
	public RequestContextListener requestContextListener() {
		return new RequestContextListener();
	}

}
