package uk.co.squadlist.web.model;

import java.util.Date;

public class Outing {
	
	private String id;
	private Squad squad;
	private Date date;
	private String notes;
	
	public Outing() {
	}

	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public Squad getSquad() {
		return squad;
	}
	public void setSquad(Squad squad) {
		this.squad = squad;
	}
	public Date getDate() {
		return date;
	}
	public void setDate(Date date) {
		this.date = date;
	}
	public String getNotes() {
		return notes;
	}
	public void setNotes(String notes) {
		this.notes = notes;
	}
	
	@Override
	public String toString() {
		return "Outing [id=" + id + ", squad=" + squad + ", date=" + date + ", notes=" + notes + "]";
	}
	
}
