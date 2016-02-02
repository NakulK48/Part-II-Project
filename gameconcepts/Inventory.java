package gameconcepts;
import java.io.Serializable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

public class Inventory implements Serializable {

	public HashSet<String> items;
	
	public Inventory() {
		this.items = new HashSet<String>();
	}
	
	public Inventory (Inventory inv) {
		this.items = (HashSet<String>) inv.items.clone();
	}
	
	public void addItem(Item item) {
		this.items.add(item.name);
	}
	
	public boolean hasItem(String itemName) {
		return items.contains(itemName);
	}
	
	public boolean hasItem(Item item) {
		return items.contains(item.name);
	}
	
	public Set<String> getItemNames() {
		return (Set<String>) items.clone();
	}
	
	public void removeItem(Item item) {
		this.items.remove(item.getName());
	}
	
	public void removeItem(String itemName) {
		items.remove(itemName);
	}
}
