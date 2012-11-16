package uk.co.squadlist.web.model.forms;

public class SquadDetails {
	
	private String name;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	@Override
	public String toString() {
		return "SquadDetails [name=" + name + "]";
	}
	
}
