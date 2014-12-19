package uk.co.squadlist.web.model.forms;

import org.hibernate.validator.constraints.NotBlank;

public class AvailabilityOptionDetails {
	
    @NotBlank
	private String name;
    
	public String getName() {
		return name;
	}
	
	public void setName(String name) {
		this.name = name;
	}

	@Override
	public String toString() {
		return "AvailabilityOptionDetails [name=" + name + "]";
	}
	
}
