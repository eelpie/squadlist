package uk.co.squadlist.web.exceptions.propertyeditors;

import java.beans.PropertyEditorSupport;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import uk.co.squadlist.web.api.InstanceSpecificApiClient;
import uk.co.squadlist.web.exceptions.UnknownSquadException;

@Component
public class SquadPropertyEditor extends PropertyEditorSupport {

	private final InstanceSpecificApiClient api;

	@Autowired
	public SquadPropertyEditor(InstanceSpecificApiClient api) {
		this.api = api;
	}

	@Override
	public void setAsText(String squadId) throws IllegalArgumentException {
		try {
			setValue(api.getSquad(squadId));
		} catch (UnknownSquadException e) {
			throw new IllegalArgumentException("Invalid squad");
		}
	}
	
}
