package gameconcepts;

import java.io.Serializable;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Item implements Serializable {
	private final int id;
	public final String name;
	public String description;
	public HashSet<String> properties;
	public boolean takable;
	public Set<String> tags;
	
	// Each item should have a unique identifier
	private static int nextID = 0;
	
	public Item(String name, String description) {
		this.name = name.toLowerCase();
		this.id = nextID;
		this.description = description;
		this.properties = new HashSet<String>();
		nextID++;
		takable = true;
		this.tags = new HashSet<String>(Arrays.asList(this.name.split(" ")));
	}
	
	public Item(String name, String description, boolean takable) {
		this.name = name.toLowerCase();
		this.id = nextID;
		this.description = description;
		this.properties = new HashSet<String>();
		nextID++;
		this.takable = takable;
		this.tags = new HashSet<String>(Arrays.asList(this.name.split(" ")));
	}
	
	@SuppressWarnings("unchecked")
	public Item(Item item) {
		if (item instanceof Key) {
			
		}
		this.id = item.id;
		this.name = item.name;
		this.description = item.description;
		this.properties = (HashSet<String>) item.properties.clone();
		this.takable = item.takable;
		this.tags = new HashSet<String>(item.tags);
	}
	
	public boolean hasTag(String tag) {
		return this.tags.contains(tag);
	}

	public int getID() {
		return id;
	}

	public String getName() {
		return name;
	}
	
	public void addProperty(String property) {
		this.properties.add(property);
	}
	
	public void addProperties(List<String> properties) {
		for (String property : properties) {
			addProperty(property);
		}
	}
	
	public void removeProperty(String property) {
		this.properties.remove(property);
	}
	
	public void removeProperties(List<String> properties) {
		for (String property : properties) {
			removeProperty(property);
		}
	}
	
	public void setProperties(List<String> properties) {
		this.properties = new HashSet<String>(properties);
	}
	
	public void printEditorDetails() {
		System.out.println();
		System.out.println(name.toUpperCase());
		System.out.println(description);
		System.out.println("Takable: " + takable);
		System.out.println();
		System.out.println("Properties:");
		System.out.println(properties);
		System.out.println();
	}
	
	public void printInGameDetails() {
		System.out.println(description);
		if (!properties.isEmpty()) System.out.println("It is " + String.join(", ", properties));
	}
	
}
