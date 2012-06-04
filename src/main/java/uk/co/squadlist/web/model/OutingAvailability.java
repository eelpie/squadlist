package uk.co.squadlist.web.model;

public class OutingAvailability {

	private Outing outing;
	private String availability;
	
	public Outing getOuting() {
		return outing;
	}
	public void setOuting(Outing outing) {
		this.outing = outing;
	}
	public String getAvailability() {
		return availability;
	}
	public void setAvailability(String availability) {
		this.availability = availability;
	}
	
	@Override
	public String toString() {
		return "OutingAvailability [outing=" + outing + ", availability="
				+ availability + "]";
	}

}
