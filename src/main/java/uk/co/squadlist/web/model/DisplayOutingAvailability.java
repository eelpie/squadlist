package uk.co.squadlist.web.model;

public class DisplayOutingAvailability {
	
	private final int id, squad;
	private final String squadName, date, availability;
	
	public DisplayOutingAvailability(int id, int squad, String squadName, String date, String availability) {
		this.id = id;
		this.squad = squad;
		this.squadName = squadName;
		this.date = date;
		this.availability = availability;
	}

	public int getId() {
		return id;
	}

	public int getSquad() {
		return squad;
	}

	public String getSquadName() {
		return squadName;
	}

	public String getDate() {
		return date;
	}

	public String getAvailability() {
		return availability;
	}
	
}
