package uk.co.squadlist.web.views;

import org.springframework.stereotype.Component;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.RedirectView;

@Component
public class ViewFactory {

    public ModelAndView getViewFor(String templateName) {
        return new ModelAndView(templateName);
    }

    public ModelAndView redirectionTo(String url) {
        RedirectView redirectView = new RedirectView(url);
        redirectView.setExposeModelAttributes(false);
        return new ModelAndView(redirectView);
    }

}
