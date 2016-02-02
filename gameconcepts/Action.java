package gameconcepts;

import java.io.Serializable;
import java.util.List;

public class Action implements Serializable {
	public String name;
	public String property;
	public String effectText;
	
	public boolean destroysItem;
	
	// If the above is true, these two should be empty.
	public List<String> propertiesToAdd;
	public List<String> propertiesToRemove;
	
	public List<String> itemsToAdd;

	public Action(String name, String property, String effectText, boolean destroysItem, List<String> propertiesToAdd,
			List<String> propertiesToRemove, List<String> itemsToAdd) {
		this.name = name;
		this.property = property;
		this.effectText = effectText;
		this.destroysItem = destroysItem;
		this.propertiesToAdd = propertiesToAdd;
		this.propertiesToRemove = propertiesToRemove;
		this.itemsToAdd = itemsToAdd;
	}
	
	
}
