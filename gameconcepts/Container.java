package gameconcepts;

import java.util.HashSet;
import java.util.Set;

import knowledgerep.KnowledgeBase;
import knowledgerep.SameItemException;

public class Container extends Item {
	
	public int volume;
	public Set<String> contains;
	
	public Container(String name, String description, int volume, boolean takeable, KnowledgeBase kb) {
		super(name, description);
		this.takable = false;
		this.volume = volume;
		kb.addProperty("container", name);
		this.takable = takeable;
		this.contains = new HashSet<String>();
	}
	
	public Container(Container c) {
		super(c.name, c.description);
		this.takable = c.takable;
		this.volume = c.volume;
		this.takable = c.takable;
		this.contains = new HashSet<String>(c.contains);
	}
	
	public void addItem(String itemName, KnowledgeBase kb) throws SameItemException {
		contains.add(itemName);
		kb.putInside(itemName, this.name);
	}
	
	public void removeItem(String itemName, KnowledgeBase kb) {
		contains.remove(itemName);
		kb.takeOut(itemName, this.name);
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
		System.out.println("Contains:");
		System.out.println(contains);
	}
	
	public void printInGameDetails() {
		System.out.println(description);
		if (!properties.isEmpty()) System.out.println("It is " + String.join(", ", properties));
		System.out.println("Contains: ");
		if (contains.isEmpty()) System.out.println("nothing :(");
		else System.out.println(contains);
	}
	
}
