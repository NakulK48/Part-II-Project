package gameconcepts;

import java.util.HashMap;

public class LockedDoor extends Item {

	public String originLocation;
	public String targetLocation;
	public Direction direction;
	
	public LockedDoor(String name, String description, String originLocation, String targetLocation) {
		super(name, description, false);
		this.originLocation = originLocation;
		this.targetLocation = targetLocation;
	}
	
	public LockedDoor(LockedDoor ld) {
		super(ld);
		this.originLocation = ld.originLocation;
		this.targetLocation = ld.targetLocation;
	}
	
	public void open(HashMap<String, Location> locs) {
		Location originLoc = locs.get(originLocation);
		originLoc.addExit(direction, targetLocation, locs);
		originLoc.availableItems.removeItem(this.name);
	}
}
