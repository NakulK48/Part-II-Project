package gameconcepts;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.Serializable;
import java.util.HashMap;

import knowledgerep.KnowledgeBase;

public class SavePoint implements Serializable {
	public Inventory inv;
	public String currentLocation;
	public HashMap<String, Location> locations;
	public KnowledgeBase kb;
	
	public SavePoint(Inventory inv, String currentLocation, HashMap<String, Location> locations, KnowledgeBase kb) throws FileNotFoundException, IOException {
		super();
		this.inv = new Inventory(inv);
		this.currentLocation = currentLocation;
		// Must deep copy locations.
		this.locations = new HashMap<String, Location>();
		for (String s : locations.keySet()) {
			Location loc = new Location(locations.get(s));
			this.locations.put(s, loc);
		}
		this.kb = new KnowledgeBase(kb);
	}

}
