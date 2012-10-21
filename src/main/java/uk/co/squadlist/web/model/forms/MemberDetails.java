package uk.co.squadlist.web.model.forms;

public class MemberDetails {
	
	private String firstName;
	private String lastName;
	
	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}
	
	public void setLastName(String lastName) {
		this.lastName = lastName;
	}
	
	@Override
	public String toString() {
		return "MemberDetails [firstName=" + firstName + ", lastName="
				+ lastName + "]";
	}
	
}
