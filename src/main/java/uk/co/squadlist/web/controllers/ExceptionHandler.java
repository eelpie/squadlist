package uk.co.squadlist.web.controllers;

import com.google.common.base.Strings;
import io.sentry.SentryClient;
import io.sentry.SentryClientFactory;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.Ordered;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerExceptionResolver;
import org.springframework.web.servlet.ModelAndView;
import uk.co.squadlist.client.swagger.ApiException;
import uk.co.squadlist.web.exceptions.PermissionDeniedException;
import uk.co.squadlist.web.exceptions.SignedInMemberRequiredException;
import uk.co.squadlist.web.exceptions.UnknownAvailabilityOptionException;
import uk.co.squadlist.web.exceptions.UnknownOutingException;
import uk.co.squadlist.web.urls.UrlBuilder;
import uk.co.squadlist.web.views.ViewFactory;

import javax.servlet.RequestDispatcher;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Component
public class ExceptionHandler implements HandlerExceptionResolver, Ordered {

    private final static Logger log = LogManager.getLogger(ExceptionHandler.class);

    private final UrlBuilder urlBuilder;
    private final ViewFactory viewFactory;

    @Value("${googleAnalyticsAccount}")
    private String googleAnalyticsAccount;

    private final SentryClient sentryClient;

    @Autowired
    public ExceptionHandler(UrlBuilder urlBuilder,
                            ViewFactory viewFactory,
                            @Value("${sentryDSN}") String sentryDSN) {
        this.urlBuilder = urlBuilder;
        this.viewFactory = viewFactory;
        log.info("Setting up Sentry client with DSN: " + sentryDSN);

        if (!Strings.isNullOrEmpty(sentryDSN)) {
            this.sentryClient = SentryClientFactory.sentryClient(sentryDSN);
        } else {
            this.sentryClient = null;
        }
    }

    @Override
    public ModelAndView resolveException(HttpServletRequest request,
                                         HttpServletResponse response,
                                         Object handler,
                                         Exception e) {
        log.debug("Handling exception of type: " + e.getClass());

        if (e instanceof UnknownAvailabilityOptionException) {
            response.setStatus(HttpStatus.NOT_FOUND.value());
            return new ModelAndView("404");
        }
        if (e instanceof UnknownOutingException) {
            response.setStatus(HttpStatus.NOT_FOUND.value());
            return new ModelAndView("404");
        }
        if (e instanceof PermissionDeniedException) {
            response.setStatus(HttpStatus.FORBIDDEN.value());
            return new ModelAndView("403");
        }

        if (e instanceof ApiException) {
            if (((ApiException) e).getCode() == HttpStatus.NOT_FOUND.value()) {
                response.setStatus(HttpStatus.NOT_FOUND.value());
                return new ModelAndView("404");
            }
        }

        if (e instanceof SignedInMemberRequiredException) {
            return viewFactory.redirectionTo(urlBuilder.loginUrl());
        }

        String path = (String) request.getAttribute(RequestDispatcher.ERROR_REQUEST_URI);

        if (sentryClient != null) {
            log.info("Sending sentry exception for path: " + path, e);
            sentryClient.sendException(e);
        }
        log.error("Returning 500 error for path: " + path, e);
        response.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
        return new ModelAndView("500").addObject("googleAnalyticsAccount", googleAnalyticsAccount);
    }

    @Override
    public int getOrder() {
        return Integer.MIN_VALUE;
    }

}
