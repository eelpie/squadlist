package uk.co.squadlist.web.model;

public class AvailabilityOption {
	
	private String label;

	public String getLabel() {
		return label;
	}

	public void setLabel(String label) {
		this.label = label;
	}
	
	@Override
	public String toString() {
		return "AvailabilityOption [label=" + label + "]";
	}
	
}
