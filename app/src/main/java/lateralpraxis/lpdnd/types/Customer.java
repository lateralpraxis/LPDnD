package lateralpraxis.lpdnd.types;

public class Customer {
	private String Id;
	private String Name;

	public Customer(String id, String name) {
		super();
		Id = id;
		Name = name;
	}

	public String getId() {
		return Id;
	}
	public void setId(String id) {
		Id = id;
	}
	public String getName() {
		return Name;
	}
	public void setName(String name) {
		Name = name;
	}

	@Override
	public String toString () {
		return Name;
	}

}
