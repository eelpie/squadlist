package uk.co.squadlist.web.model.forms;

@Deprecated	// TODO why won't Squad bind?
public class MemberSquad {
	
	private String id;
	
	public MemberSquad() {
	}
	
	public MemberSquad(String id) {
		this.id = id;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	@Override
	public String toString() {
		return "MemberSquad [id=" + id + "]";
	}
	
}
