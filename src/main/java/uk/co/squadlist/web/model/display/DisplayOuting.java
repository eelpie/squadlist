package uk.co.squadlist.web.model.display;

import java.util.Date;

import uk.co.squadlist.web.model.Squad;

public class DisplayOuting {

	private final String id;
	private final Squad squad;
	private final Date date;

	public DisplayOuting(String id, Squad squad, Date date) {
		this.id = id;
		this.squad = squad;
		this.date = date;
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
	
}
