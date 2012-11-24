package uk.co.squadlist.web.model.forms;

import org.hibernate.validator.constraints.NotBlank;

public class MemberDetails {

	@NotBlank
	private String firstName, lastName, emailAddress;
	
	private String contactNumber, squad;
	
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
	
	public String getEmailAddress() {
		return emailAddress;
	}

	public void setEmailAddress(String emailAddress) {
		this.emailAddress = emailAddress;
	}
	
	public String getContactNumber() {
		return contactNumber;
	}

	public void setContactNumber(String contactNumber) {
		this.contactNumber = contactNumber;
	}

	public String getSquad() {
		return squad;
	}

	public void setSquad(String squad) {
		this.squad = squad;
	}

	@Override
	public String toString() {
		return "MemberDetails [contactNumber=" + contactNumber
				+ ", emailAddress=" + emailAddress + ", firstName=" + firstName
				+ ", lastName=" + lastName + ", squad=" + squad + "]";
	}
	
}
