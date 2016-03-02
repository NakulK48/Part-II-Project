package gameconcepts;

import java.io.Serializable;

import knowledgerep.KnowledgeBase;
import knowledgerep.SameItemException;

public class Link implements Serializable {

	Item item1;
	Item item2;
	Item resultItem;
	
	public Link(Item item1, Item item2, Item resultItem, KnowledgeBase kb) throws SameItemException {
		this.item1 = item1;
		this.item2 = item2;
		this.resultItem = resultItem;
		kb.addLink(item1.name, item2.name);
	}

	@Override
	public String toString() {
		return item1.name + "+" + item2.name + "->" + resultItem.name;
	}
	
}
