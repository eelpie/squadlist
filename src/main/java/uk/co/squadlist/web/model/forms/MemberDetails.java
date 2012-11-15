package uk.co.squadlist.web.model.forms;

public class MemberDetails {
	
	private String firstName;
	private String lastName;
	private String squad;
	
	public String getFirstName() {
		return firstName;
	}
	
	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}
	
	public String getLastName() {
		return lastName;
	}
	
	public void setLastName(String lastName) {
		this.lastName = lastName;
	}
	
	public String getSquad() {
		return squad;
	}

	public void setSquad(String squad) {
		this.squad = squad;
	}

	@Override
	public String toString() {
		return "MemberDetails [firstName=" + firstName + ", lastName="
				+ lastName + ", squad=" + squad + "]";
	}
	
}
