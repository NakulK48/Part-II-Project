package gameconcepts;

public class Key extends Item {
	
	public String lockedDoorName;
	
	public Key(String name, String description, String lockedDoorName) {
		super(name, description);
		this.lockedDoorName = lockedDoorName;
	}

}
