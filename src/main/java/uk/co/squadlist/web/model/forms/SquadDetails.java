package uk.co.squadlist.web.model.forms;

import org.hibernate.validator.constraints.NotBlank;

public class SquadDetails {
	
    @NotBlank
	private String name;
    
    private String members;
    
	public String getName() {
		return name;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	public String getMembers() {
		return members;
	}

	public void setMembers(String members) {
		this.members = members;
	}

	@Override
	public String toString() {
		return "SquadDetails [members=" + members + ", name=" + name + "]";
	}
	
}
