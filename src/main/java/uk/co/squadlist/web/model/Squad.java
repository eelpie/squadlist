package uk.co.squadlist.web.model;

public class Squad {

	private String id, name;
	
	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	@Override
	public String toString() {
		return "Squad [id=" + id + ", name=" + name + "]";
	}
	
}
