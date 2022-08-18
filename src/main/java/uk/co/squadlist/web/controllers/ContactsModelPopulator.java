package uk.co.squadlist.web.controllers;

import com.google.common.base.Function;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.collect.Ordering;
import com.google.common.collect.Sets;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.ModelAndView;
import uk.co.squadlist.client.swagger.ApiException;
import uk.co.squadlist.client.swagger.api.DefaultApi;
import uk.co.squadlist.model.swagger.Member;
import uk.co.squadlist.model.swagger.Squad;
import uk.co.squadlist.web.services.filters.ActiveMemberFilter;

import java.util.List;
import java.util.Set;

@Component
public class ContactsModelPopulator {

    private static Function<Member, String> roleName = new Function<Member, String>() {
        @Override
        public String apply(Member obj) {
            return obj.getRole();
        }
    };

    private static Function<Member, String> firstName = new Function<Member, String>() {
        @Override
        public String apply(Member obj) {
            return obj.getFirstName();
        }
    };

    private static Function<Member, String> lastName = new Function<Member, String>() {
        @Override
        public String apply(Member obj) {
            return obj.getLastName();
        }
    };

    private final static Ordering<Member> byLastName = Ordering.natural().nullsLast().onResultOf(lastName);
    private final static Ordering<Member> byFirstName = Ordering.natural().nullsLast().onResultOf(firstName);
    private final static Ordering<Member> byRole = Ordering.natural().nullsLast().onResultOf(roleName);
    private final static Ordering<Member> byRoleThenFirstName = byRole.compound(byFirstName);
    private final static Ordering<Member> byRoleThenLastName = byRole.compound(byLastName);

    private final ActiveMemberFilter activeMemberFilter;
    private final DisplayMemberFactory displayMemberFactory;

    @Autowired
    public ContactsModelPopulator(ActiveMemberFilter activeMemberFilter,
                                  DisplayMemberFactory displayMemberFactory) {
        this.activeMemberFilter = activeMemberFilter;
        this.displayMemberFactory = displayMemberFactory;
    }

    public void populateModel(final Squad squad, final ModelAndView mv, DefaultApi api, Member loggedInMember) throws ApiException {
        List<Member> squadMembers = api.getSquadMembers(squad.getId());
        Ordering<Member> byRoleThenName = byRoleThenFirstName; // TODO restore instance.getMemberOrdering() != null && instance.getMemberOrdering().equals("firstName") ? byRoleThenFirstName : byRoleThenLastName;

        final List<Member> activeMembers = byRoleThenName.sortedCopy(activeMemberFilter.extractActive(squadMembers));
        final Set<String> emails = Sets.newHashSet();
        for (Member member : activeMembers) {
            if (!Strings.isNullOrEmpty(member.getEmailAddress())) {
                emails.add(member.getEmailAddress());
            }
        }

        mv.addObject("title", squad.getName() + " contacts");
        mv.addObject("squad", squad);
        mv.addObject("members", displayMemberFactory.toDisplayMembers(activeMembers, loggedInMember));
        if (!emails.isEmpty()) {
            mv.addObject("emails", Lists.newArrayList(emails));
        }
    }

}