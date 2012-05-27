package uk.co.squadlist.web.model;

import java.util.List;

public class Member {
	
	private String id, firstName, lastName, gender, dateOfBirth;
	private List<String> squads;
	
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
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
	public String getGender() {
		return gender;
	}
	public void setGender(String gender) {
		this.gender = gender;
	}
	public String getDateOfBirth() {
		return dateOfBirth;
	}
	public void setDateOfBirth(String dateOfBirth) {
		this.dateOfBirth = dateOfBirth;
	}
	public List<String> getSquads() {
		return squads;
	}
	public void setSquads(List<String> squads) {
		this.squads = squads;
	}
	
	@Override
	public String toString() {
		return "Member [id=" + id + ", firstName=" + firstName + ", lastName="
				+ lastName + ", gender=" + gender + ", dateOfBirth="
				+ dateOfBirth + ", squads=" + squads + "]";
	}
		
}
