package uk.co.squadlist.web.model;

import java.util.Map;

import org.codehaus.jackson.annotate.JsonProperty;

public class OutingWithSquadAvailability {

	private Outing outing;
	
	@JsonProperty("availability")
	private Map<String, String> availability;

	public OutingWithSquadAvailability() {
	}

	public OutingWithSquadAvailability(Outing outing, Map<String, String> availability) {
		this.outing = outing;
		this.availability = availability;
	}

	public Outing getOuting() {
		return outing;
	}

	public void setOuting(Outing outing) {
		this.outing = outing;
	}

	public Map<String, String> getAvailability() {
		return availability;
	}

	public void setAvailability(Map<String, String> availability) {
		this.availability = availability;
	}

}
