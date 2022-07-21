package uk.co.squadlist.web.controllers;

import com.google.common.base.Function;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.collect.Ordering;
import com.google.common.collect.Sets;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.ModelAndView;
import uk.co.squadlist.web.annotations.RequiresSquadPermission;
import uk.co.squadlist.web.api.InstanceSpecificApiClient;
import uk.co.squadlist.web.exceptions.SignedInMemberRequiredException;
import uk.co.squadlist.web.model.Instance;
import uk.co.squadlist.web.model.Member;
import uk.co.squadlist.web.model.Squad;
import uk.co.squadlist.web.services.Permission;
import uk.co.squadlist.web.services.PermissionsService;
import uk.co.squadlist.web.services.filters.ActiveMemberFilter;
import uk.co.squadlist.web.views.model.DisplayMember;

import java.util.ArrayList;
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

    private final PermissionsService permissionsService;
    private final ActiveMemberFilter activeMemberFilter;

    @Autowired
    public ContactsModelPopulator(PermissionsService permissionsService,
                                  ActiveMemberFilter activeMemberFilter) {
        this.permissionsService = permissionsService;
        this.activeMemberFilter = activeMemberFilter;
    }

    @RequiresSquadPermission(permission = Permission.VIEW_SQUAD_CONTACT_DETAILS)
    public void populateModel(final Squad squad, final ModelAndView mv, Instance instance, InstanceSpecificApiClient instanceSpecificApiClient, Member loggedInMember) throws SignedInMemberRequiredException {
        List<Member> squadMembers = instanceSpecificApiClient.getSquadMembers(squad.getId());
        Ordering<Member> byRoleThenName = instance.getMemberOrdering() != null && instance.getMemberOrdering().equals("firstName") ? byRoleThenFirstName : byRoleThenLastName;

        final List<Member> activeMembers = byRoleThenName.sortedCopy(activeMemberFilter.extractActive(squadMembers));
        final Set<String> emails = Sets.newHashSet();
        for (Member member : activeMembers) {
            if (!Strings.isNullOrEmpty(member.getEmailAddress())) {
                emails.add(member.getEmailAddress());
            }
        }

        mv.addObject("title", squad.getName() + " contacts");
        mv.addObject("squad", squad);
        mv.addObject("members", toDisplayMembers(activeMembers, loggedInMember));
        if (!emails.isEmpty()) {
            mv.addObject("emails", Lists.newArrayList(emails));
        }
    }

    private List<DisplayMember> toDisplayMembers(List<Member> members, Member loggedInUser) {
        List<DisplayMember> displayMembers = new ArrayList<>();
        for (Member member : members) {
            boolean isEditable = permissionsService.hasMemberPermission(loggedInUser, Permission.EDIT_MEMBER_DETAILS, member);
            displayMembers.add(new DisplayMember(member, isEditable));
        }
        return displayMembers;
    }

}