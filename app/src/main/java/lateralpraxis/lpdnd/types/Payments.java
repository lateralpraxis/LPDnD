package lateralpraxis.lpdnd.types;

public class Payments {
	private String Id;
	private String Name;
	private String Date;
	//Constructor for payments
	public Payments ( String Id , String Name, String Date ) {
		this.Id = Id;
		this.Name = Name;
		this.Date = Date;
	}

	//Method to set Id
	public void setId(String id){
		this.Id = id;
	}

	//Method to set Name
	public void setName(String name){
		this.Name = name;
	}

	//Method to set Date
	public void setDate(String date){
		this.Name = date;
	}

	//Method to Get Id
	public String getId () {
		return Id;
	}

	//Method to get Name
	public String getName () {
		return Name;
	}

	//Method to get Date
	public String getDate () {
		return Date;
	}

}
