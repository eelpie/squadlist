package uk.co.squadlist.web.controllers;

import io.sentry.SentryClient;
import io.sentry.SentryClientFactory;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.Ordered;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerExceptionResolver;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.RedirectView;
import uk.co.squadlist.web.exceptions.*;
import uk.co.squadlist.web.urls.UrlBuilder;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Component
public class ExceptionHandler implements HandlerExceptionResolver, Ordered {

    private final static Logger log = Logger.getLogger(ExceptionHandler.class);

    private final UrlBuilder urlBuilder;

    @Value("${googleAnalyticsAccount}")
    private String googleAnalyticsAccount;

    @Value("${sentryDSN}")
    private String sentryDSN;

    private final SentryClient sentryClient;

    @Autowired
    public ExceptionHandler(UrlBuilder urlBuilder) {
        this.urlBuilder = urlBuilder;
        this.sentryClient = SentryClientFactory.sentryClient(sentryDSN);
    }

    @Override
    public ModelAndView resolveException(HttpServletRequest request,
                                         HttpServletResponse response,
                                         Object handler,
                                         Exception e) {
        log.debug("Handling exception of type: " + e.getClass());
        if (e instanceof UnknownInstanceException) {
            response.setStatus(HttpStatus.NOT_FOUND.value());
            return new ModelAndView("unknownInstance");
        }
        if (e instanceof UnknownAvailabilityOptionException) {
            response.setStatus(HttpStatus.NOT_FOUND.value());
            return new ModelAndView("404");
        }
        if (e instanceof UnknownBoatException) {
            response.setStatus(HttpStatus.NOT_FOUND.value());
            return new ModelAndView("404");
        }
        if (e instanceof UnknownOutingException) {
            response.setStatus(HttpStatus.NOT_FOUND.value());
            return new ModelAndView("404");
        }
        if (e instanceof UnknownSquadException) {
            response.setStatus(HttpStatus.NOT_FOUND.value());
            return new ModelAndView("404");
        }
        if (e instanceof UnknownMemberException) {
            response.setStatus(HttpStatus.NOT_FOUND.value());
            return new ModelAndView("404");
        }
        if (e instanceof PermissionDeniedException) {
            response.setStatus(HttpStatus.FORBIDDEN.value());
            return new ModelAndView("403");
        }

        if (e instanceof SignedInMemberRequiredException) {
            return new ModelAndView(new RedirectView(urlBuilder.loginUrl()));
        }

        log.info("Sending sentry exception for path: " + request.getPathInfo(), e);
        sentryClient.sendException(e);

        log.error("Returning 500 error for path: " + request.getPathInfo(), e);
        response.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
        return new ModelAndView("500").addObject("googleAnalyticsAccount", googleAnalyticsAccount);
    }

    @Override
    public int getOrder() {
        return Integer.MIN_VALUE;
    }

}
