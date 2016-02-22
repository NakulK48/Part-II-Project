package gameconcepts;

import java.util.Set;

import knowledgerep.KnowledgeBase;
import knowledgerep.SameItemException;

public class Container extends Item {
	
	public int volume;
	public Set<String> contains;
	
	public Container(String name, String description, int volume, KnowledgeBase kb) {
		super(name, description);
		this.takable = false;
		this.volume = volume;
		kb.addProperty("container", name);
	}
	
	public Container(Container c) {
		super(c.name, c.description);
		this.takable = c.takable;
		this.volume = c.volume;
	}
	
	public void addItem(String itemName, KnowledgeBase kb) throws SameItemException {
		contains.add(itemName);
		kb.putInside(itemName, this.name);
	}
	
	public void removeItem(String itemName, KnowledgeBase kb) {
		contains.remove(itemName);
		kb.takeOut(itemName, this.name);
	}
	
	public void printDetails() {
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
	
}
