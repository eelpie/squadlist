package uk.co.squadlist.web;

import com.google.common.collect.Maps;
import org.apache.log4j.Logger;
import org.apache.velocity.app.Velocity;
import org.apache.velocity.runtime.RuntimeConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.context.embedded.FilterRegistrationBean;
import org.springframework.boot.context.embedded.MultipartConfigFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.web.filter.CharacterEncodingFilter;
import org.springframework.web.multipart.commons.CommonsMultipartResolver;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;
import org.springframework.web.servlet.view.velocity.VelocityConfigurer;
import org.springframework.web.servlet.view.velocity.VelocityViewResolver;
import uk.co.eelpieconsulting.common.email.EmailService;
import uk.co.eelpieconsulting.common.views.EtagGenerator;
import uk.co.eelpieconsulting.common.views.ViewFactory;
import uk.co.squadlist.web.api.SquadlistApi;
import uk.co.squadlist.web.api.SquadlistApiFactory;
import uk.co.squadlist.web.auth.ApiBackedAuthenticationProvider;
import uk.co.squadlist.web.auth.ApiBackedUserDetailsService;
import uk.co.squadlist.web.auth.LoggedInUserService;
import uk.co.squadlist.web.urls.UrlBuilder;
import uk.co.squadlist.web.views.*;

import javax.servlet.MultipartConfigElement;
import java.io.IOException;
import java.util.Map;

@EnableAutoConfiguration
@ComponentScan(basePackages="uk.co.squadlist.web")
@Configuration
@EnableWebMvc
@EnableScheduling
@EnableWebSecurity
public class Main extends WebSecurityConfigurerAdapter {
	
	private final static Logger log = Logger.getLogger(Main.class);
			  
	private static ApplicationContext ctx;

    public static void main(String[] args) throws Exception {
    	ctx = SpringApplication.run(Main.class, args);     
    }

	@Override
	protected void configure(HttpSecurity http) throws Exception {
		http.csrf().disable();

		http.authorizeRequests()
			.antMatchers("/favicon.ico", "/static/**", "login**", "/reset-password/**", "/reset-password", "/ical", "/rss",
					"/social/facebook/signin", "/social/facebook/signin/callback").permitAll()
			.anyRequest().authenticated()
			.and()
			.formLogin().loginPage("/login").failureUrl("/login?errors=true").permitAll()
			.and()
			.logout().permitAll();
	}

	@Bean
    public CommonsMultipartResolver multipartResolver(@Value("${maximum.upload}") Long maxUploadSize) throws IOException {
    	final CommonsMultipartResolver multipartResolver = new CommonsMultipartResolver();
    	multipartResolver.setMaxUploadSize(maxUploadSize);
		return multipartResolver;    	
    }
    
    @Bean
    MultipartConfigElement multipartConfigElement() {
        MultipartConfigFactory factory = new MultipartConfigFactory();
        factory.setMaxFileSize("10MB");
        factory.setMaxRequestSize("10MB");
        return factory.createMultipartConfig();
    }
    
    @Bean
    public ViewFactory viewFactory() throws IOException {
    	return new ViewFactory(new EtagGenerator());
    }

	@Bean
	public SquadlistApi squadlistApi(SquadlistApiFactory squadlistApiFactory) {
		return squadlistApiFactory.create();
	}

	@Bean
	public EmailService emailService(@Value("${smtp.host}") String smtpHost, @Value("${smtp.port}") Integer smtpPort) {
		return new EmailService(smtpHost, smtpPort);
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
	public VelocityConfigurer velocityConfigurer() {
		final VelocityConfigurer vc = new VelocityConfigurer();
		final Map<String, Object> velocityPropertiesMap = Maps.newHashMap();
		velocityPropertiesMap.put(Velocity.OUTPUT_ENCODING, "UTF-8");
		velocityPropertiesMap.put(Velocity.INPUT_ENCODING, "UTF-8");
		velocityPropertiesMap.put(RuntimeConstants.RESOURCE_LOADER, "classpath");
		velocityPropertiesMap.put("eventhandler.referenceinsertion.class", "org.apache.velocity.app.event.implement.EscapeHtmlReference");
		vc.setVelocityPropertiesMap(velocityPropertiesMap);
		return vc;
	}

	@Bean
	public VelocityViewResolver velocityViewResolver(CssHelper cssHelper, DateFormatter dateFormatter,
													 DateHelper dateHelper, LoggedInUserService loggedInUserService,
													 PermissionsHelper permissionsHelper, SquadNamesHelper squadNamesHelper,
													 TextHelper textHelper,
													 UrlBuilder urlBuilder) {
		final VelocityViewResolver viewResolver = new VelocityViewResolver();
		viewResolver.setCache(true);
		viewResolver.setPrefix("");
		viewResolver.setSuffix(".vm");
		viewResolver.setContentType("text/html;charset=UTF-8");
		
		final Map<String, Object> attributes = Maps.newHashMap();
		attributes.put("cssHelper", cssHelper);
		attributes.put("dateFormatter", dateFormatter);
		attributes.put("dateHelper", dateHelper);
		attributes.put("loggedInUserService", loggedInUserService);
		attributes.put("permissionsHelper", permissionsHelper);
		attributes.put("squadNamesHelper", squadNamesHelper);
		attributes.put("text", textHelper);
		attributes.put("urlBuilder", urlBuilder);
		viewResolver.setAttributesMap(attributes);
		return viewResolver;
	}
	
}
