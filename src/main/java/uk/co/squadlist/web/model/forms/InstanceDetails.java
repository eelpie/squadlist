package uk.co.squadlist.web.model.forms;

public class InstanceDetails {

	private String memberOrdering, governingBody;

	public String getMemberOrdering() {
		return memberOrdering;
	}

	public void setMemberOrdering(String memberOrdering) {
		this.memberOrdering = memberOrdering;
	}

	public String getGoverningBody() {
		return governingBody;
	}

	public void setGoverningBody(String governingBody) {
		this.governingBody = governingBody;
	}

	@Override
	public String toString() {
		return "InstanceDetails{" +
				"memberOrdering='" + memberOrdering + '\'' +
				", governingBody='" + governingBody + '\'' +
				'}';
	}

}
