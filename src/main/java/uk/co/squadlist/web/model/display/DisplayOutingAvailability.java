package uk.co.squadlist.web.model.display;

import java.util.Date;

import uk.co.squadlist.web.model.Squad;

public class DisplayOutingAvailability {
	
	private final String id;
	private final Squad squad;
	private final String availability;
	private final Date date;
	private final String notes;
	
	public DisplayOutingAvailability(String id, Squad squad, Date date, String notes, String availability) {
		this.id = id;
		this.squad = squad;
		this.date = date;
		this.notes = notes;
		this.availability = availability;
	}

	public String getId() {
		return id;
	}

	public Squad getSquad() {
		return squad;
	}

	public Date getDate() {
		return date;
	}
	
	public String getNotes() {
		return notes;
	}

	public String getAvailability() {
		return availability;
	}

	@Override
	public String toString() {
		return "DisplayOutingAvailability [availability=" + availability
				+ ", date=" + date + ", id=" + id + ", notes=" + notes
				+ ", squad=" + squad + "]";
	}
	
}
