package uk.co.squadlist.web.model.forms;

import org.hibernate.validator.constraints.NotBlank;

public class ChangePassword {

	@NotBlank
	private String currentPassword, newPassword;

	public String getCurrentPassword() {
		return currentPassword;
	}

	public void setCurrentPassword(String currentPassword) {
		this.currentPassword = currentPassword;
	}

	public String getNewPassword() {
		return newPassword;
	}

	public void setNewPassword(String newPassword) {
		this.newPassword = newPassword;
	}

	@Override
	public String toString() {
		return "ChangePassword [currentPassword=" + currentPassword + ", newPassword=" + newPassword + "]";
	}
	
}
