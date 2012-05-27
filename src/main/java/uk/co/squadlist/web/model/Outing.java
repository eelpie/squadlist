package uk.co.squadlist.web.model;

public class Outing {

	private int id;
	private String squad;
	private String date;
	private String notes;
	
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public String getSquad() {
		return squad;
	}
	public void setSquad(String squad) {
		this.squad = squad;
	}
	public String getDate() {
		return date;
	}
	public void setDate(String date) {
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
