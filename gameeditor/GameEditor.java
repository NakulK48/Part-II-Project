package gameeditor;

import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Scanner;
import java.util.Set;

import gameconcepts.Action;
import gameconcepts.Direction;
import gameconcepts.Inventory;
import gameconcepts.Item;
import gameconcepts.ItemNotFoundException;
import gameconcepts.Link;
import gameconcepts.Location;
import knowledgerep.KnowledgeBase;
import knowledgerep.SameItemException;

public class GameEditor {
	
	public HashMap<String, Item> items;
	public HashMap<String, Location> locations;
	public Location startLocation;
	public Location victoryLocation;
	public Item victoryItem;
	public HashMap<String, Location> defeatLocations;
	public KnowledgeBase kb;
	public Scanner s = new Scanner(System.in);
	public HashMap<String, Action> actions;
	public HashMap<String, List<String>> propertiesWithRules;
	public Set<Link> links;

	public GameEditor(Game game) {
		super();
		this.items = game.allItems;
		this.locations = game.allLocations;
		this.startLocation = game.startLocation;
		this.victoryLocation = game.victoryLocation;
		this.defeatLocations = game.defeatLocations;
		this.victoryItem = game.victoryItem;
		this.kb = game.kb;
		this.actions = game.actions;
		this.propertiesWithRules = game.propertiesWithRules;
		this.links = game.links;
	}
	
	public String prompt(String promptText) {
		System.out.println(promptText);
		return s.nextLine();
	}
	
	public Direction getOppositeDirection(Direction direction) {
		switch (direction) {
			case NORTH: return Direction.SOUTH;
			case EAST: return Direction.WEST;
			case SOUTH: return Direction.NORTH;
			case WEST: return Direction.EAST;
			
			case NORTHEAST: return Direction.SOUTHWEST;
			case SOUTHEAST: return Direction.NORTHWEST;
			case SOUTHWEST: return Direction.NORTHEAST;
			case NORTHWEST: return Direction.SOUTHEAST;
			
			default: return null;
		}
			
	}
	
	public List<String> stripAndSplit(String raw) {
		return Arrays.asList(raw.replace(" ", "").split(","));
	}
	
	public void addAction() {
		String name = prompt("Action name: ");
		String property = prompt("Base property: ");
		String actionText = prompt("Action text: ");
		String destroyString = prompt("Does the action destroy its item? (y/n)");
		boolean destroyItem = destroyString.equals("y");

		String choice = prompt("Properties to add to item (comma separated):");
		List<String> propertiesToAdd = stripAndSplit(choice);

		choice = prompt("Properties to remove from item (comma separated):");
		List<String> propertiesToRemove = stripAndSplit(choice);
		
		LinkedList<String> itemsToAdd = new LinkedList<String>();
		
		choice = prompt("Items to add to inventory (q to stop):");
		
		while (true) {
			if (choice.equals("q")) break;
			if (!items.containsKey(choice)) {
				System.out.println("Not a valid item.");
				continue;
			}
			itemsToAdd.add(choice);
		}
		
		Action a = new Action(name, property, actionText, destroyItem, propertiesToAdd, propertiesToRemove, itemsToAdd);
		actions.put(name, a);
	}
	
	public void manageSingleAction(String actionName) {
		Action a = actions.get(actionName);
		System.out.println(actionName);
		System.out.println("Property: " + a.property);
		System.out.println("Properties to add:");
		System.out.println(a.propertiesToAdd);
		System.out.println("Properties to remove:");
		System.out.println(a.propertiesToRemove);
		System.out.println("Items to add:");
		System.out.println(a.itemsToAdd);
		
		String choice = prompt("Redefine action (y/n): ");
		if (choice.equals("y")) {
			actions.remove(actionName);
			addAction();
		}
		
	}
	
	public void manageActions() {

		while (true) {
			System.out.println();
			System.out.println("Current actions:");
			System.out.println(actions.keySet());
			System.out.println();
			String choice = prompt("Choose an action, or 'new' to add an action, or 'q' to go back");
			if (choice.equals("q")) return;
			if (choice.equals("new")) addAction();
			else {
				if (!actions.containsKey(choice)) {
					System.out.println("Not a valid choice.");
					continue;
				}
				
				manageSingleAction(choice);
			}
		}
	}

	public Location createLocation() {
		String name = prompt("Enter name: ");
		String flavourText = prompt("Enter flavour text:");
		Inventory inv = new Inventory();
		HashMap<Direction, String> exits = new HashMap<Direction, String>();
		// TODO: Have a way of adding items and exits.
		Location loc = new Location(name, flavourText, inv, exits);
		if (this.startLocation == null) this.startLocation = loc;
		return loc;
	}
	
	public void manageSingleLocation(String locName) {
		Location loc = locations.get(locName);
		while (true) {
			System.out.println(loc.generateText());
			System.out.println("(1) Edit flavour text");
			System.out.println("(2) Add item");
			System.out.println("(3) Remove item");
			System.out.println("(4) Add exit");
			System.out.println("(5) Remove exit");
			System.out.println("(q) Return to previous menu");
			String choice = s.nextLine();
			try {
				switch (choice) {
					case "1": //flavour
						String flavor = prompt("New flavour text:");
						loc.flavorText = flavor;
						break;
					case "2": //add item
						System.out.println("Available items:");
						System.out.println(this.items.keySet());
						String itemName = prompt("Enter an item name:");
						if (items.containsKey(itemName)) {
							Item item = items.get(itemName);
							loc.availableItems.addItem(item);
						}
						else {
							System.out.println("No such item. Create it first.\n");
						}
						break;
					case "3": //remove item
						System.out.println(loc.availableItems.getItemNames());
						String removedItemName = prompt("Enter an item name:");
						loc.availableItems.removeItem(removedItemName);
						break;
					case "4": //add exit
						String directionString = prompt("Enter a direction:");
						Direction direction = Direction.valueOf(directionString.toUpperCase());
						System.out.println("Available rooms:");
						System.out.println(this.locations.keySet());
						String locationName = prompt("Enter a location name:");
						if (!locations.containsKey(locationName)) {
							System.out.println("No such room\n");
							break;
						}
						Location exit = locations.get(locationName);
						loc.exits.put(direction, locationName);
						exit.exits.put(getOppositeDirection(direction), locName);
						break;
					case "5": //remove exit
						System.out.println(loc.exits.keySet());
						directionString = prompt("Enter a direction:");
						direction = Direction.valueOf(directionString.toUpperCase());
						loc.exits.remove(direction);
						break;
					case "q":
						return;
				}
			} catch (IllegalArgumentException e) {
				System.out.println("That is not one of the eight valid compass directions.");
				continue;
			}
		}
	}
	
	public void manageLocations() {
		while (true) {
			System.out.println("Current locations:");
			System.out.println(this.locations.keySet());
			System.out.println("Select a location, or 'new' to create a location, or (q) to return to main menu");
			String choice = s.nextLine();
			if (choice.equals("q")) return;
			if (choice.equals("new")) {
				Location loc = createLocation();
				locations.put(loc.name, loc);
				manageSingleLocation(loc.name);
			}
			if (locations.containsKey(choice)) {
				manageSingleLocation(choice);
			}
			else {
				System.out.println("No such location.");
			}
		}
	}
	
	public Item createItem() {
		String name = prompt("Enter name:");
		String description = prompt("Enter description:");
		String takableString = prompt("Takable? (y/n)");
		boolean takable = true;
		if (takableString.equals("n")) takable = false;
		Item item = new Item(name, description, takable);
		//TextInput.launchPropertyWindow(item);
		//kb.addProperties(item.properties, item.name);
		return item;
	}
	
	public void manageSingleItem(String itemName) {
		while (true) {
			System.out.println();
			Item item = items.get(itemName);
			item.printDetails();
			
			System.out.println("(1) Edit description");
			System.out.println("(2) Set properties");
			System.out.println("(3) Set takable");
			System.out.println("(q) Go back");
			
			String choice = s.nextLine();
			switch (choice) {
				case "1":
					item.description = prompt("Enter description:");
					break;
				case "2": //set properties
					TextInput.launchPropertyWindow(item, kb);
					break;
				case "3":
					boolean takable = true;
					String takableString = prompt("Takable? (y/n)");
					if (takableString.equals("n")) takable = false;
					item.takable = takable;
				case "q":
					return;
				default:
					System.out.println("Not a valid option.");
			}
		}
	}
	
	public void manageLinks() {
		while (true) {
			System.out.println("Current links:");
			System.out.println(this.links);
			String choice = prompt("'new' to create a link, or (q) to return to main menu");
			if (choice.equals("q")) return;
			if (choice.equals("new")) {
				createLink();
			}
		}
	}
	
	public void createLink() {
		System.out.println("REQUIRED ITEM 1:");
		Item item1 = chooseItem(items.keySet());
		System.out.println("REQUIRED ITEM 2:");
		Item item2 = chooseItem(items.keySet());
		System.out.println("RESULT ITEM:");
		Item result = chooseItem(items.keySet());
		try {
			Link link = new Link(item1, item2, result, kb);
		} catch (SameItemException e) {
			System.out.println("Those cannot be the same item!");
			createLink();
		}
	}
	
	public Item chooseItem(Set<String> itemNames) {
		System.out.println("Available items:");
		System.out.println(itemNames);
		while (true) {
			String choice = prompt("Type an item name:");
			if (itemNames.contains(choice)) return items.get(choice);
			System.out.println("Invalid item.");
		}
	}
	
	public void manageItems() {
		while (true) {
			System.out.println("Current items:");
			System.out.println(this.items.keySet());
			String choice = prompt("Select an item, or 'new' to create an item, or (q) to return to main menu");
			if (choice.equals("q")) return;
			if (choice.equals("new")) {
				Item item = createItem();
				items.put(item.name, item);
				manageSingleItem(item.name);
			}
			if (items.containsKey(choice)) {
				manageSingleItem(choice);
			}
			else {
				System.out.println("No such item.");
			}
		}
	}
	
	public void manageDefeatLocations() {
		System.out.println("Current defeat locations:");
		System.out.println(defeatLocations.keySet());

		System.out.println("(a)dd or (r)emove?");
		String choice = s.nextLine();
		switch (choice) {
			case "a":
				System.out.println("Available locations:");
				System.out.println(this.locations.keySet());
				String addChoice = s.nextLine();
				defeatLocations.put(addChoice,  locations.get(addChoice));
				break;
			case "r":
				String removeChoice = s.nextLine();
				defeatLocations.remove(removeChoice);
				break;
			default:
				System.out.println("Choice not recognised.");
				manageDefeatLocations();
				break;
		}
		String startName = s.nextLine();
		if (locations.containsKey(startName)) {
			this.startLocation = locations.get(startName);
		}
		else System.out.println("Invalid location.");
	}
	
	public void managePropertyRules() {
		System.out.println("Properties With Rules:");
		System.out.println(propertiesWithRules);
		String choice = prompt("Type a property name to view and edit, or type 'new', or 'q' to go back");
		if (choice.equals("q")) return;
		if (choice.equals("new")) {
			String name = prompt("Enter the property name:");
			String premises = prompt("Enter the desired premise properties, separated by commas:");
			List<String> premiseList = Arrays.asList(premises.replace(" ", "").split(","));
			propertiesWithRules.put(name, premiseList);
			kb.addRule(name, premiseList);
			managePropertyRules();
		}
		else if (!propertiesWithRules.containsKey(choice)) {
			System.out.println("That property does not exist.");
			managePropertyRules();
		}
		else {
			System.out.println(choice);
			System.out.println(propertiesWithRules.get(choice));
			String choice2 = prompt("Delete? (y/n)");
			if (choice2.equals("y")) {
				propertiesWithRules.remove(choice);
				kb.removeRule(choice);
			}
			managePropertyRules();
		}
	}
	
	public void mainCreator() {
		System.out.println("Enter a number to continue");
		while (true) {
			System.out.println("(1) Manage locations");
			System.out.println("(2) Manage items");
			System.out.println("(3) Set start location");
			System.out.println("(4) Set victory location");
			System.out.println("(5) Set defeat locations");
			System.out.println("(6) Set victory item");
			System.out.println("(7) Manage actions");
			System.out.println("(8) Manage property rules");
			System.out.println("(9) Print knowledgebase");
			System.out.println("(10) Manage links");
			System.out.println("(q) Exit");
			String choice = s.nextLine();
			switch (choice){
				case "1":
					manageLocations();
					break;
				case "2":
					manageItems();
					break;
				case "3":
					System.out.println("Current start location:");
					if (startLocation != null) System.out.println(startLocation.name);
					System.out.println("Available locations:");
					System.out.println(this.locations.keySet());
					String startName = prompt("Start location:");
					if (locations.containsKey(startName)) {
						this.startLocation = locations.get(startName);
					}
					else System.out.println("Invalid location.");
					break;
				case "4":
					System.out.println("Current victory location:");
					if (victoryLocation != null) System.out.println(victoryLocation.name);
					System.out.println("Available locations:");
					System.out.println(this.locations.keySet());
					String victoryName = prompt("Victory location:");
					if (locations.containsKey(victoryName)) {
						this.victoryLocation = locations.get(victoryName);
					}
					else System.out.println("Invalid location.");
					break;
				case "5":
					manageDefeatLocations();
				case "6":
					System.out.println("Current victory item:");
					if (victoryItem != null) System.out.println(victoryItem.name);
					System.out.println("Available items:");
					System.out.println(this.items.keySet());
					String itemName = prompt("Victory item:");
					if (locations.containsKey(itemName)) {
						this.victoryItem = items.get(victoryItem);
					}
					else System.out.println("Invalid item.");
					break;
				case "7":
					manageActions();
					break;
				case "8":
					managePropertyRules();
					break;
				case "9":
					kb.printTheory();
					break;
				case "10":
					manageLinks();
				case "q":
					return;
				default:
					System.out.println("Option not recognised");
			}
		}
	}
	
	public static void main(String[] args) {
	}
	
}
