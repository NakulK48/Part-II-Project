package gameconcepts;

public class Key extends Item {
	
	public String lockedDoorName;
	
	public Key(String name, String description, String lockedDoorName) {
		super(name, description, true);
		this.lockedDoorName = lockedDoorName;
	}
	
	public Key(Key k) {
		super(k);
		this.lockedDoorName = k.lockedDoorName;
	}

}
