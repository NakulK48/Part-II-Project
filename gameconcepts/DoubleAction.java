package gameconcepts;

import java.util.List;

public class DoubleAction extends Action {
	
	public String property2;

	public DoubleAction(String name, String property1, String property2, String effectText, boolean destroysItem,
			List<String> propertiesToAdd, List<String> propertiesToRemove, List<String> itemsToAdd) {
		super(name, property1, effectText, destroysItem, propertiesToAdd, propertiesToRemove, itemsToAdd);
		this.property2 = property2;
	}

}
