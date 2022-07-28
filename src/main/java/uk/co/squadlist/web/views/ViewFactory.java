package uk.co.squadlist.web.views;

import org.springframework.stereotype.Component;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.RedirectView;
import uk.co.squadlist.web.model.Instance;

@Component
public class ViewFactory {

    public ModelAndView getViewFor(String templateName, Instance instance) {
        return new ModelAndView(templateName).
                addObject("instance", instance);
    }

    public ModelAndView redirectionTo(String url) {
        RedirectView redirectView = new RedirectView(url);
        redirectView.setExposeModelAttributes(false);
        return new ModelAndView(redirectView);
    }

}
