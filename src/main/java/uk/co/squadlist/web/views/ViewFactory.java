package uk.co.squadlist.web.views;

import com.google.common.base.Strings;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.RedirectView;
import uk.co.squadlist.web.model.Instance;

@Component
public class ViewFactory {

    @Value("${googleAnalyticsAccount}")
    private String googleAnalyticsAccount;

    public ModelAndView getViewFor(String templateName, Instance instance) {
        ModelAndView mv = new ModelAndView(templateName).
                addObject("instance", instance);
        if (!Strings.isNullOrEmpty(googleAnalyticsAccount)) {
            mv.addObject("googleAnalyticsAccount", googleAnalyticsAccount);
        }
        return mv;
    }

    public ModelAndView redirectionTo(String url) {
        RedirectView redirectView = new RedirectView(url);
        redirectView.setExposeModelAttributes(false);
        return new ModelAndView(redirectView);
    }

}
