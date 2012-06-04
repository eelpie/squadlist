package uk.co.squadlist.web.model.display;

public class DisplayOuting {

	private final int id, squad;
	private final String squadName, date;

	public DisplayOuting(int id, int squad, String squadName, String date) {
		this.id = id;
		this.squad = squad;
		this.squadName = squadName;
		this.date = date;
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
	
}
