package uk.co.squadlist.web.model;

import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import uk.co.squadlist.web.model.forms.MemberDetails;

import com.google.common.collect.Lists;

public class Member {
	
	private String id, firstName, lastName, gender, dateOfBirth, emailAddress, contactNumber, rowingPoints, scullingPoints, registrationNumber;
	private int weight;
	private List<Squad> squads;
	
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
	public int getWeight() {
		return weight;
	}
	public void setWeight(int weight) {
		this.weight = weight;
	}
	public List<Squad> getSquads() {
		return squads;
	}
	public void setSquads(List<Squad> squads) {
		this.squads = squads;
	}
	
	public List<NameValuePair> toNameValuePairs() {
		final List<NameValuePair> nameValuePairs = Lists.newArrayList();
		nameValuePairs.add(new BasicNameValuePair("firstName", this.getFirstName()));
		nameValuePairs.add(new BasicNameValuePair("lastName", this.getLastName()));
		nameValuePairs.add(new BasicNameValuePair("emailAddress", this.getEmailAddress()));
		nameValuePairs.add(new BasicNameValuePair("contactNumber", this.getContactNumber()));
		nameValuePairs.add(new BasicNameValuePair("weight", Integer.toString(this.getWeight())));
		nameValuePairs.add(new BasicNameValuePair("registrationNumber", this.getRegistrationNumber()));
		nameValuePairs.add(new BasicNameValuePair("rowingPoints", this.getRowingPoints()));
		nameValuePairs.add(new BasicNameValuePair("scullingPoints", this.getScullingPoints()));
		return nameValuePairs;
	}
	
	public void updateFrom(MemberDetails memberDetails) {
		this.setFirstName(memberDetails.getFirstName());
		this.setLastName(memberDetails.getLastName());
		this.setEmailAddress(memberDetails.getEmailAddress());
		this.setContactNumber(memberDetails.getContactNumber());
		this.setRowingPoints(memberDetails.getRowingPoints());
		this.setScullingPoints(memberDetails.getScullingPoints());
		this.setRegistrationNumber(memberDetails.getRegistrationNumber());
	}
	
	@Override
	public String toString() {
		return "Member [contactNumber=" + contactNumber + ", dateOfBirth="
				+ dateOfBirth + ", emailAddress=" + emailAddress
				+ ", firstName=" + firstName + ", gender=" + gender + ", id="
				+ id + ", lastName=" + lastName + ", registrationNumber="
				+ registrationNumber + ", rowingPoints=" + rowingPoints
				+ ", scullingPoints=" + scullingPoints + ", squads=" + squads
				+ ", weight=" + weight + "]";
	}
	
}
