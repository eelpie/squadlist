package uk.co.squadlist.web.model.forms;

import org.hibernate.validator.constraints.NotBlank;

public class AvailabilityOptionDetails {
	
    @NotBlank
	private String name, colour;
    
	public String getName() {
		return name;
	}
	
	public void setName(String name) {
		this.name = name;
	}

	public String getColour() {
		return colour;
	}

	public void setColour(String colour) {
		this.colour = colour;
	}

	@Override
	public String toString() {
		return "AvailabilityOptionDetails [colour=" + colour + ", name=" + name
				+ "]";
	}
	
}
