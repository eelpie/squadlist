package uk.co.squadlist.web;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.authentication.configurers.GlobalAuthenticationConfigurerAdapter;
import uk.co.squadlist.web.auth.ApiBackedAuthenticationProvider;
import uk.co.squadlist.web.auth.ApiBackedUserDetailsService;

@Configuration
public class Security extends GlobalAuthenticationConfigurerAdapter {

    @Autowired
    private ApiBackedAuthenticationProvider apiBackedAuthenticationProvider;

    @Autowired
    private ApiBackedUserDetailsService apiBackedUserDetailsService;

    @Override
    public void init(AuthenticationManagerBuilder auth) throws Exception {
        auth.authenticationProvider(apiBackedAuthenticationProvider);
        auth.userDetailsService(apiBackedUserDetailsService);
    }
}
