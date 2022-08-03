package uk.co.squadlist.web.views;

import java.util.List;

import org.springframework.stereotype.Component;

import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import uk.co.squadlist.model.swagger.Squad;

@Component
public class SquadNamesHelper {

	private final static Joiner commaJoiner = Joiner.on(", ");

	public String list(List<Squad> squads) {
		final List<String> squadNames = Lists.newArrayList();
		for (Squad squad : squads) {
			squadNames.add(squad.getName());
		} 
		return commaJoiner.join(squadNames);
	}

	public String listSquads(List<Squad> squads) {
		final List<String> squadNames = Lists.newArrayList();
		for (Squad squad : squads) {
			squadNames.add(squad.getName());
		}
		return commaJoiner.join(squadNames);
	}

}
