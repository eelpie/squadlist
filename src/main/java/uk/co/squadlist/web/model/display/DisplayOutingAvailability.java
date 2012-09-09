package uk.co.squadlist.web.model.display;

import java.util.Date;

import uk.co.squadlist.web.model.Squad;

public class DisplayOutingAvailability {
	
	private final int id;
	private final Squad squad;
	private final String availability;
	private final Date date;
	
	public DisplayOutingAvailability(int id, Squad squad, Date date, String availability) {
		this.id = id;
		this.squad = squad;
		this.date = date;
		this.availability = availability;
	}

	public int getId() {
		return id;
	}

	public Squad getSquad() {
		return squad;
	}

	public Date getDate() {
		return date;
	}

	public String getAvailability() {
		return availability;
	}
	
}
