package uk.co.squadlist.web.views;

import org.springframework.stereotype.Component;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.RedirectView;
import uk.co.squadlist.web.exceptions.SignedInMemberRequiredException;
import uk.co.squadlist.web.exceptions.UnknownInstanceException;
import uk.co.squadlist.web.model.Member;

@Component
public class ViewFactory {

    public ModelAndView getViewForLoggedInUser(String templateName, Member loggedInUser) throws SignedInMemberRequiredException, UnknownInstanceException {
        return new ModelAndView(templateName);
    }

    public ModelAndView redirectionTo(String url) {
        RedirectView redirectView = new RedirectView(url);
        redirectView.setExposeModelAttributes(false);
        return new ModelAndView(redirectView);
    }

}
