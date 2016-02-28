package gameconcepts;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.Scanner;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;

import alice.tuprolog.InvalidTheoryException;
import alice.tuprolog.NoSolutionException;
import alice.tuprolog.SolveInfo;
import gameeditor.Game;
import knowledgerep.KnowledgeBase;
import knowledgerep.SameItemException;
import nlp.AnalyzedInput;
import nlp.InputProcessing;
import nlp.InvalidInputException;

public class GameSession implements Serializable {

	private Game game;
	private String name;
	private Inventory currentInventory;
	private Location currentLocation;
	private KnowledgeBase kb;
	private HashMap<String, Action> actions;
	private HashMap<String, Location> locations;
	private HashMap<String, Item> items;
	private Set<Link> links;
	private InputProcessing ip;
	private SavePoint sp;
	
	private ArrayList<String> positiveResponses = new ArrayList<String>();
	private ArrayList<String> negativeResponses = new ArrayList<String>();
	
	public GameSession(Game game, String name, Inventory currentInventory, Location currentLocation, KnowledgeBase kb, 
			HashMap<String, Action> actions, HashMap<String, Location> locations, HashMap<String, Item> items,
			Set<Link> links) {
		this.game = game;
		this.name = name.toLowerCase();
		this.currentInventory = currentInventory;
		this.currentLocation = currentLocation;
		this.kb = kb;
		this.actions = actions;
		this.ip = new InputProcessing();
		this.locations = locations;
		this.items = items;
		this.links = links;
	}
	
	// For internal testing and debugging.
	private GameSession() {
		currentInventory = new Inventory();
		ip = new InputProcessing();
	}
	
	public static String prompt(String promptText) {
		Scanner in = new Scanner(System.in);
		System.out.println(promptText);
		return in.nextLine();
	}
	
	public static String randomChoice(ArrayList<String> list) {
		Random random = new Random();
		int index = random.nextInt(list.size());
		return list.get(index);
	}

	public void enterNewLocation(Location loc) {
		this.currentLocation = loc;
		System.out.println(loc.generateText());
	}
	
	public void moveLocation(Direction direction) {
		if (currentLocation.exits.containsKey(direction)) {
			String destName = currentLocation.exits.get(direction);
			enterNewLocation(locations.get(destName));
		}
		else {
			System.out.println("Invalid direction!");
		}
	}
	
	public void takeItem(String itemName) {
		if (!currentLocation.availableItems.hasItem(itemName)) {
			System.out.println("That item is not present!");
			return;
		}
		if (currentLocation.availableItems.hasItem(itemName)) {
			Item item = items.get(itemName);
			if (!item.takable) {
				System.out.println("You cannot take that!");
				return;
			}
			currentLocation.availableItems.removeItem(itemName);
			currentInventory.addItem(item);
			System.out.println("Took " + itemName);
		}
		else {
			Set<String> poss = getPossibleItems(currentLocation.availableItems.items, itemName);
			if (poss.size() == 0) {
				System.out.println("No such item is available!");
				return;
			}
			System.out.println("Did you mean:");
			System.out.println(poss);
			String choice = prompt("Enter one of the above items or type 'no'");
			if (choice.equals("no")) return;
			if (!poss.contains(choice)) {
				System.out.println("Sorry, that item is not available.");
			}
			takeItem(choice);
		}
	}
	
	public void inspectItem(String itemName) {
		if (!currentInventory.hasItem(itemName) && !currentLocation.availableItems.hasItem(itemName)) {
			System.out.println("You don't have that item!");
			return;
		}
		Item item = items.get(itemName);
		item.printInGameDetails();
	}
	
	public void takeActionOnItem(String action, String itemName) throws ItemNotFoundException {
		if (!items.containsKey(itemName)) {
			System.out.println("That item does not exist!");
			return;
		}
		
		Item item = items.get(itemName);
		
		// (Takable) Item not in inventory
		if (!currentInventory.hasItem(itemName) && item.takable) {
			System.out.println("You don't have that item!");
			return;
		}
		
		// Static object (non-takable item) not present in room 
		if (!currentLocation.availableItems.hasItem(item) && !item.takable) {
			System.out.println("That item is not present!");
			return;
		}
		
		Action a = actions.get(action);
		
		String property = a.property;
		
		SolveInfo s = kb.hasProperty(property, itemName);
		
		try {
			boolean querySuccess = kb.getQuerySuccess(s);
			if (!querySuccess) {
				System.out.println(action + " failed because it does not have the property " + property);
				System.out.println("This is because " + kb.getQueryFailureReason(s));
				return;
			}
			
			System.out.println(a.effectText);
			
			if (a.destroysItem) currentInventory.removeItem(itemName);
			else {
				item.addProperties(a.propertiesToAdd);
				item.removeProperties(a.propertiesToRemove);
				kb.addProperties(a.propertiesToAdd, itemName);
				kb.removeProperties(a.propertiesToRemove, itemName);
			}
		} catch (NoSolutionException e) {
			System.out.println("You can't do that!");
			return;
		}
	}
	
	public void processUserInput(String input) {
		input = input.trim();
		List<String> words = Arrays.asList(input.split(" "));
		String action = words.get(0).toLowerCase();
		Scanner s = new Scanner(System.in);
		try {
			switch (action) {
				case "go":
				case "move":
					String direction = "";
					if (words.size() == 1) {
						System.out.println(action + " where?");
						direction = s.nextLine();
					}
					else direction = words.get(1);
					moveLocation(Direction.valueOf(direction.toUpperCase()));
					break;
				case "take":
					if (input.contains("from")) {
						takeItemFromContainer(input);
					} else {
						String availableItemName = String.join(" ", words.subList(1, words.size()));
						takeItem(availableItemName.toLowerCase());
					}
					break;
				case "put":
					putItemInContainer(input);
					break;
				case "inspect":
					String inventoryItemName = String.join(" ", words.subList(1, words.size()));
					inspectItem(inventoryItemName.toLowerCase());
					break;
				case "look":
					System.out.println(currentLocation.generateText());
					break;
				case "inventory":
					System.out.println(currentInventory.getItemNames());
					break;
				case "combine":
					combineItems(input);
					break;
				case "open":
					openLockedDoor(input);
					break;
				case "help":
					System.out.println("move <direction>/take <item in room>/inspect <item in inventory>/");
					System.out.println("look (get description of room)/inventory (list items)/");
					System.out.println("combine <item1> and <item2>/");
					System.out.println("open <door> with <key>/");
					System.out.println("save/restore");
					System.out.println("or take some action on an item in the room or your inventory.");
					break;
				case "save":
					saveGame();
					System.out.println("Game saved. Enter 'restore' to return to this point.");
					break;
				case "restore":
					if (sp == null) {
						System.out.println("No save point available!");
						break;
					}
					restoreFromSave();
				default:
					if (ip == null) ip = new InputProcessing();
					AnalyzedInput ai2 = ip.analyzeInput(input);
					processAnalyzedInput(ai2);
			}
		}
		catch (Exception e) {
			//TODO: REMOVE
			e.printStackTrace();
			System.out.println("Sorry, I don't understand. Try entering 'help.'");
		}
	}
	
	private void combineItems(String input) throws InvalidInputException, NoSolutionException, SameItemException {
		AnalyzedInput ai = ip.analyzeInput(input);
		if (ai.nouns.size() != 2) {
			System.out.println("You can only combine two items!");
			return;
		}
		
		String rawItemName1 = ai.nouns.get(0);
		String rawItemName2 = ai.nouns.get(1);
		
		Set<String> availableItems = currentInventory.getItemNames();
		availableItems.addAll(currentLocation.availableItems.items);
		
		try {
			String item1 = didYouMean(getPossibleItems(availableItems, rawItemName1));
			String item2 = didYouMean(getPossibleItems(availableItems, rawItemName2));
			if (!kb.hasLink(item1, item2)) {
				System.out.println("Those don't go together...");
				return;
			}
			
			Link link = getCorrectLink(item1, item2);
			if (link == null) {
				System.out.println("Those don't go together...");
				return;
			}
			
			currentInventory.removeItem(item1);
			currentInventory.removeItem(item2);
			currentLocation.availableItems.removeItem(item1);
			currentLocation.availableItems.removeItem(item2);
			
			if (link.resultItem.takable) currentInventory.addItem(link.resultItem);
			else currentLocation.availableItems.addItem(link.resultItem);
		}
		catch (InvalidInputException e) {
			System.out.println("Sorry, the item you selected was not recognised.");
			return;
		}
		

		
	}
	
	private void takeItemFromContainer(String input) throws InvalidInputException, NoSolutionException {
		AnalyzedInput ai = ip.analyzeInput(input);
		if (ai.nouns.size() != 2) {
			System.out.println("Please specify both an item and a container.");
			return;
		}
		
		String rawItemName = ai.nouns.get(0);
		String rawContainerName = ai.nouns.get(1);
		
		Set<String> availableItems = items.keySet();
		Set<String> availableContainers = currentInventory.getItemNames();
		availableContainers.addAll(currentLocation.availableItems.items);
		
		try {
			String item = didYouMean(getPossibleItems(availableItems, rawItemName));
			String container = didYouMean(getPossibleItems(availableContainers, rawContainerName));
			if (!kb.isInside(item, container)) {
				System.out.println("That item isn't inside the container...");
				return;
			}
			
			kb.takeOut(item, container);
			Item i = items.get(item);
			currentInventory.addItem(i);
			System.out.println("Took " + item + " from " + container);
		}
		catch (InvalidInputException e) {
			System.out.println("Sorry, the item you selected was not recognised.");
			return;
		}
	}
	
	private void putItemInContainer(String input) throws InvalidInputException, NoSolutionException, SameItemException {
		AnalyzedInput ai = ip.analyzeInput(input);
		if (ai.nouns.size() != 2) {
			System.out.println("Please specify both an item and a container.");
			return;
		}
		
		String rawItemName1 = ai.nouns.get(0);
		String rawItemName2 = ai.nouns.get(1);
		
		Set<String> availableItems = currentInventory.getItemNames();
		Set<String> availableContainers = currentInventory.getItemNames();
		availableContainers.addAll(currentLocation.availableItems.items);
		
		try {
			String item = didYouMean(getPossibleItems(availableItems, rawItemName1));
			String container = didYouMean(getPossibleItems(availableContainers, rawItemName2));
			if (!kb.fitsInside(item, container)) {
				System.out.println("The " + item + " does not fit inside the " + container);
				return;
			}
			
			kb.putInside(item, container);
			Item i = items.get(item);
			currentInventory.removeItem(i);
			System.out.println("Put " + item + " in " + container);
		}
		catch (InvalidInputException e) {
			System.out.println("Sorry, the item you selected was not recognised.");
			return;
		}
	}
	
	private void openLockedDoor(String input) throws InvalidInputException, NoSolutionException, SameItemException, InvalidTheoryException {
		AnalyzedInput ai = ip.analyzeInput(input);
		
		if (ai.nouns.size() != 2) {
			System.out.println("Please select a door and a key");
			return;
		}
		
		
		String rawDoorName = ai.nouns.get(0);
		String rawKeyName = ai.nouns.get(1);
		
		Set<String> availableItems = currentInventory.getItemNames();
		availableItems.addAll(currentLocation.availableItems.items);
		
		try {
			String doorName = didYouMean(getPossibleItems(availableItems, rawDoorName));
			String keyName = didYouMean(getPossibleItems(availableItems, rawKeyName));
			Item ik = items.get(keyName);
			Item id = items.get(doorName);
			if (!(ik instanceof Key)) {
				System.out.println("That isn't a key!");
				return;
			}
			if (!(id instanceof LockedDoor)) {
				System.out.println("That isn't a door!");
			}
			if (!kb.hasOpen(keyName, doorName)) {
				System.out.println("Those don't go together...");
				return;
			}
			
			LockedDoor ld = (LockedDoor) items.get(doorName);
			Key k = (Key) items.get(keyName);
			
			ld.open(locations);
			
		}
		catch (InvalidInputException e) {
			System.out.println("Sorry, the item you selected was not recognised.");
			return;
		}
	}
	
	private Link getCorrectLink(String item1, String item2) {
		for (Link link : links) {
			if (link.item1.name.equals(item1) && link.item2.name.equals(item2)) return link;
			if (link.item1.name.equals(item2) && link.item2.name.equals(item1)) return link;
		}
		
		return null;
	}
	
	private String didYouMean(Set<String> poss) throws InvalidInputException {
		//Only one possibility: return it.
		if (poss.size() == 1) return new LinkedList<String>(poss).get(0);
		System.out.println("Did you mean:");
		System.out.println(poss);
		String choice = prompt("Type an item name or 'no' to return");
		if (poss.contains(choice)) {
			return choice;
		}
		
		else {
			throw new InvalidInputException();
		}
	}
	
	private int getStringSimilarity(String s1, String s2) {
		int longerLength = Math.max(s1.length(), s2.length());
		int shorterLength = Math.min(s1.length(), s2.length());
		
		//This is the most that Levenshtein distance can be.
		int max = longerLength;
		
		//This is the least that it can be.
		int min = longerLength - shorterLength;
		
		int levenshtein = StringUtils.getLevenshteinDistance(s1, s2);
		
		//This thus provides a 0-1 scale from least to most similar.
		return (1 - levenshtein / (max - min));
	}
	
	private Set<String> getPossibleItems(Set<String> allItems, String input) {
		Set<String> possibleItems = new HashSet<String>();
		
		//Simplest case: the input is the exact name of an item.
		if (allItems.contains(input)) {
			possibleItems.add(input);
			return possibleItems;
		}
		
		//The entered word is one of the item's tags
		for (String s : allItems) {
			Item i = items.get(s);
			if (i.hasTag(input)) {
				possibleItems.add(s);
			}
		}
		
		//The entered word is similar to one of the item's tags.
		//Don't check this unless there were no perfect matches. 
		if (possibleItems.size() == 0) {
			for (String s : allItems) {
				Item i = items.get(s);
				for (String tag : i.tags) {
					int similarity = getStringSimilarity(tag, input);
					if (similarity > 0.5) {
						possibleItems.add(s);
					}
				}
			}
		}
		
		return possibleItems;
	}
	
	public void processAnalyzedInput(AnalyzedInput ai) throws ItemNotFoundException {
		String item = null;
		
		if (ai.nouns.size() > 1) {
			System.out.println("That action only takes one item.");
			return;
		}
		
		String noun = ai.nouns.get(0);
		
		if (currentInventory.hasItem(noun)) {
			item = noun;
		}
		else {
			Set<String> inventoryAndRoomItems = currentInventory.getItemNames();
			inventoryAndRoomItems.addAll(currentLocation.availableItems.getItemNames());
			Set<String> possibleItems = getPossibleItems(inventoryAndRoomItems, noun);
			if (possibleItems.size() == 0) {
				possibleItems = getPossibleItems(items.keySet(), noun);
				if (possibleItems.size() == 0) {
					System.out.println("Sorry, this world knows of no such item.");
				}
				else {
					System.out.println("I know of an item like that, but it's not available here.");
				}
			}
			
			System.out.println("Did you mean:");
			System.out.println(possibleItems);
			String choice = prompt("Select an item name, or type 'cancel'");
			if (possibleItems.contains(choice)) {
				item = choice;
			}
			else {
				System.out.println("Sorry, that doesn't seem to be an item.");
			}
		}
		
		String action = ai.verb;
		HashSet<String> possibleActions = new HashSet<String>();
		
		if (!actions.containsKey(action)) {
			String latestSynonym = null;
			for (String synonym : ai.verbSynonyms) {
				if (actions.containsKey(synonym))  {
					latestSynonym = synonym;
					possibleActions.add(synonym);
				}
			}
			
			if (possibleActions.size() == 1) action = latestSynonym;
			else if (possibleActions.size() > 0){
				System.out.println("Perhaps you meant one of: ");
				System.out.println(possibleActions);
				action = prompt("Select an action: ");
			}
		}
		
		takeActionOnItem(action, item);
	}
	
	public void play() {
		this.ip = new InputProcessing();
		System.out.println(currentLocation.generateText());
		while (true) {
			if (game.isVictory(currentLocation, currentInventory)) game.printVictoryText();
			else if (game.isDefeat(currentLocation)) game.printDefeatText();
			System.out.print("> ");
			Scanner s = new Scanner(System.in);
			String input = s.nextLine();
			if (input.equals("q")) break;
			processUserInput(input);
		}
		this.ip = null;
	}
	
	public void saveGame() throws FileNotFoundException, IOException {
		SavePoint sp = new SavePoint(currentInventory, currentLocation.name, locations, kb);
		this.sp = sp;
	}
	
	public void restoreFromSave() {
		this.currentInventory = sp.inv;
		this.currentLocation = locations.get(sp.currentLocation);
		this.locations = sp.locations;
		this.kb = sp.kb;
		play();
	}
	
}
