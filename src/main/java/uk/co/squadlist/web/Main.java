package uk.co.squadlist.web;

import org.apache.log4j.Logger;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

@ComponentScan(basePackages = "uk.co.squadlist.web")
@Configuration
@EnableWebMvc
@EnableScheduling
@EnableAutoConfiguration
public class Main {

  private final static Logger log = Logger.getLogger(Main.class);

  private static ApplicationContext ctx;

  public static void main(String[] args) throws Exception {
    ctx = SpringApplication.run(Main.class, args);
  }

  /*
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
*/

  /*
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
  */

  /*
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
  */

  /*
  @Bean
  @ConditionalOnMissingBean(RequestContextListener.class)
  public RequestContextListener requestContextListener() {
    return new RequestContextListener();
  }
  */

}
