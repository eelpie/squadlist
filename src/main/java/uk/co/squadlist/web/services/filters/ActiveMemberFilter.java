package uk.co.squadlist.web.services.filters;

import com.google.common.collect.Lists;
import org.springframework.stereotype.Component;
import uk.co.squadlist.model.swagger.Member;

import java.util.List;

@Component
public class ActiveMemberFilter {	// TODO suggests missing API functionality

	public List<Member> extractActive(List<Member> members) {
		List<Member> selected = Lists.newArrayList();
		for (Member member : members) {
			if (!member.isInactive()) {
				selected.add(member);
			}
		}
		return selected;
	}
	public List<Member> extractInactive(List<Member> members) {
		List<Member> selected = Lists.newArrayList();
		for (Member member : members) {
			if (member.isInactive()) {
				selected.add(member);
			}
		}
		return selected;
	}

}
