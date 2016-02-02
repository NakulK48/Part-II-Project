package gameconcepts;
import java.io.Serializable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;

public class Location implements Serializable {

	public String name;
	public String flavorText;
	public Inventory availableItems;
	public HashMap<Direction, String> exits;
	
	public Location(String name, String flavorText, Inventory availableItems, HashMap<Direction, String> exits) {
		this.name = name.toLowerCase();
		this.flavorText = flavorText;
		this.availableItems = availableItems;
		this.exits = exits;
	}
	
	public Location(Location loc) {
		this.name = loc.name;
		this.flavorText = loc.flavorText;
		this.availableItems = new Inventory(loc.availableItems);
		this.exits = loc.exits;
		
	}

	public String generateText() {
		LinkedList<String> lines = new LinkedList<String>();
		lines.add(name.toUpperCase());
		lines.add(flavorText);
		lines.add("\nAvailable Items: ");
		lines.addAll(availableItems.getItemNames());
		lines.add("\nExits: ");
		for (Direction s : exits.keySet()) {
			lines.add(s + ": " + exits.get(s));
		}
		lines.add("\n");
		return String.join("\n", lines);
	}
	
}
