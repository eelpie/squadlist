package uk.co.squadlist.web.model.forms;

public class InstanceDetails {

	private String memberOrdering;

	public String getMemberOrdering() {
		return memberOrdering;
	}

	public void setMemberOrdering(String memberOrdering) {
		this.memberOrdering = memberOrdering;
	}

	@Override
	public String toString() {
		return "InstanceDetails [memberOrdering=" + memberOrdering + "]";
	}

}
