package uk.co.squadlist.web.model.forms;

import java.util.List;

import org.hibernate.validator.constraints.NotBlank;

public class MemberDetails {

	@NotBlank
	private String firstName, lastName;
	private String knownAs, emailAddress, gender, role, contactNumber, rowingPoints, scullingPoints, sweepOarSide, registrationNumber, emergencyContactName, emergencyContactNumber, sculling;	
	private Integer dateOfBirthDay, dateOfBirthMonth, dateOfBirthYear;
	private String weight;	// TODO validate
	private List<MemberSquad> squads;
	private String profileImage;
	
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
	
	public String getRowingPoints() {
		return rowingPoints;
	}

	public void setRowingPoints(String rowingPoints) {
		this.rowingPoints = rowingPoints;
	}

	public String getScullingPoints() {
		return scullingPoints;
	}

	public void setScullingPoints(String scullingPoints) {
		this.scullingPoints = scullingPoints;
	}

	public String getRegistrationNumber() {
		return registrationNumber;
	}

	public void setRegistrationNumber(String registrationNumber) {
		this.registrationNumber = registrationNumber;
	}

	public List<MemberSquad> getSquads() {
		return squads;
	}

	public void setSquads(List<MemberSquad> squads) {
		this.squads = squads;
	}

	public String getSweepOarSide() {
		return sweepOarSide;
	}

	public void setSweepOarSide(String sweepOarSide) {
		this.sweepOarSide = sweepOarSide;
	}

	public String getEmergencyContactName() {
		return emergencyContactName;
	}

	public void setEmergencyContactName(String emergencyContactName) {
		this.emergencyContactName = emergencyContactName;
	}

	public String getEmergencyContactNumber() {
		return emergencyContactNumber;
	}

	public void setEmergencyContactNumber(String emergencyContactNumber) {
		this.emergencyContactNumber = emergencyContactNumber;
	}

	public String getGender() {
		return gender;
	}
	public void setGender(String gender) {
		this.gender = gender;
	}
	
	public String getWeight() {
		return weight;
	}
	public void setWeight(String weight) {
		this.weight = weight;
	}
	public String getKnownAs() {
		return knownAs;
	}
	public void setKnownAs(String knownAs) {
		this.knownAs = knownAs;
	}

	public String getSculling() {
		return sculling;
	}
	public void setSculling(String sculling) {
		this.sculling = sculling;
	}

	public Integer getDateOfBirthDay() {
		return dateOfBirthDay;
	}

	public void setDateOfBirthDay(Integer dateOfBirthDay) {
		this.dateOfBirthDay = dateOfBirthDay;
	}

	public Integer getDateOfBirthMonth() {
		return dateOfBirthMonth;
	}

	public void setDateOfBirthMonth(Integer dateOfBirthMonth) {
		this.dateOfBirthMonth = dateOfBirthMonth;
	}

	public Integer getDateOfBirthYear() {
		return dateOfBirthYear;
	}

	public void setDateOfBirthYear(Integer dateOfBirthYear) {
		this.dateOfBirthYear = dateOfBirthYear;
	}
	
	public String getRole() {
		return role;
	}
	public void setRole(String role) {
		this.role = role;
	}
	
	public String getProfileImage() {
		return profileImage;
	}
	public void setProfileImage(String profileImage) {
		this.profileImage = profileImage;
	}

	@Override
	public String toString() {
		return "MemberDetails [firstName=" + firstName + ", lastName="
				+ lastName + ", knownAs=" + knownAs + ", emailAddress="
				+ emailAddress + ", gender=" + gender + ", role=" + role
				+ ", contactNumber=" + contactNumber + ", rowingPoints="
				+ rowingPoints + ", scullingPoints=" + scullingPoints
				+ ", sweepOarSide=" + sweepOarSide + ", registrationNumber="
				+ registrationNumber + ", emergencyContactName="
				+ emergencyContactName + ", emergencyContactNumber="
				+ emergencyContactNumber + ", sculling=" + sculling
				+ ", dateOfBirthDay=" + dateOfBirthDay + ", dateOfBirthMonth="
				+ dateOfBirthMonth + ", dateOfBirthYear=" + dateOfBirthYear
				+ ", weight=" + weight + ", squads=" + squads
				+ ", profileImage=" + profileImage + "]";
	}
	
}