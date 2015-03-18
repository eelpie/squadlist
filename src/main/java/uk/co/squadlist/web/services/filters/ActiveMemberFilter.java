package uk.co.squadlist.web.services.filters;

import java.util.List;

import org.springframework.stereotype.Component;

import uk.co.squadlist.web.model.Member;

import com.google.common.collect.Lists;

@Component
public class ActiveMemberFilter {	// TODO suggests missing API functionality

	public List<Member> extractActive(List<Member> members) {
		List<Member> selected = Lists.newArrayList();
		for (Member member : members) {
			if (member.getInactive() == false) {
				selected.add(member);
			}
		}
		return selected;
	}

	public List<Member> extractInactive(List<Member> members) {
		List<Member> selected = Lists.newArrayList();
		for (Member member : members) {
			if (member.getInactive()) {
				selected.add(member);
			}
		}
		return selected;
	}

}
