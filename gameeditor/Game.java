package gameeditor;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Scanner;
import java.util.Set;

import gameconcepts.Action;
import gameconcepts.GameSession;
import gameconcepts.Inventory;
import gameconcepts.Item;
import gameconcepts.Link;
import gameconcepts.Location;
import knowledgerep.KnowledgeBase;

public class Game implements Serializable {

	public String name;
	public HashMap<String, GameSession> sessions;
	public HashMap<String, Location> allLocations;
	public HashMap<String, Location> defeatLocations;
	public Location victoryLocation;
	public HashMap<String, Item> allItems;
	public Item victoryItem;
	public Location startLocation;
	public String victoryText;
	public String defeatText;
	public KnowledgeBase kb;
	public HashMap<String, Action> actions;
	public HashMap<String, List<String>> propertiesWithRules;
	public Set<Link> links;
	
	public Game(String name) throws FileNotFoundException, IOException {
		super();
		this.name = name;
		this.sessions = new HashMap<String, GameSession>();
		this.allLocations = new HashMap<String, Location>();
		this.allItems = new HashMap<String, Item>();
		this.startLocation = null;
		this.victoryLocation = null;
		this.defeatLocations = new HashMap<String, Location>();
		this.victoryItem = null;
		this.victoryText = "You win!";
		this.defeatText = "You lose!";
		this.kb = new KnowledgeBase();
		this.actions = new HashMap<String, Action>();
		this.propertiesWithRules = new HashMap<String, List<String>>();
		this.links = new HashSet<Link>();
	}

	public void editGame() throws IOException {
		GameEditor ge = new GameEditor(this);
		ge.mainCreator();
		this.allLocations = ge.locations;
		this.allItems = ge.items;
		this.startLocation = ge.startLocation;
		this.victoryLocation = ge.victoryLocation;
		this.defeatLocations = ge.defeatLocations;
		this.victoryItem = ge.victoryItem;
		this.kb = ge.kb;
		this.actions = ge.actions;
		this.propertiesWithRules = ge.propertiesWithRules;
		this.links = ge.links;
		saveToFile();
	}
	
	public void saveToFile() throws IOException {
		File f = new File("game.ser");
		if (!f.exists()) {
			f.createNewFile();
		}
		FileOutputStream fos = new FileOutputStream(f);
		ObjectOutputStream oos = new ObjectOutputStream(fos);
		oos.writeObject(this);
		oos.close();
		fos.close();
	}
	
	
	
	@SuppressWarnings("unchecked")
	public void playGame() throws IOException {
		Scanner s = new Scanner(System.in);
		//TODO: These both really need to be deep copies.
		while (true) {
			System.out.println("Select a session or type 'new' to create one or 'q' to quit.");
			System.out.println("Available sessions:");
			System.out.println(sessions.keySet());
			String choice = s.nextLine();
			if (choice.equals("q")) break;
			if (choice.equals("new")) {
				System.out.println("Name (must be unique):");
				String sessionName = s.nextLine();
				newSession(sessionName);
			}
			else if (!sessions.containsKey(choice)) {
				System.out.println("Not a valid session.");
				continue;
			}
			else {
				GameSession session = sessions.get(choice);
				session.play();
			}
		}
	}
	
	public HashMap<String, Location> deepCopyAllLocations(HashMap<String, Location> original) {
		HashMap<String, Location> copy = new HashMap<String, Location>();
		for (String s : original.keySet()) {
			copy.put(s, new Location(original.get(s)));
		}
		
		return copy;
	}
	
	public HashMap<String, Item> deepCopyAllItems(HashMap<String, Item> original) {
		HashMap<String, Item> copy = new HashMap<String, Item>();
		for (String s : original.keySet()) {
			copy.put(s, new Item(original.get(s)));
		}
		
		return copy;
	}
	
	@SuppressWarnings("unchecked")
	public void newSession(String name) throws FileNotFoundException, IOException {
		// Want to copy the knowledge base instead of assigning it because
		// the session may add some facts to the knowledge base
		// For the same reason, we want to copy the locations and items.
		HashSet<String> facts = (HashSet<String>)kb.facts.clone();
		HashSet<String> rules = (HashSet<String>) kb.rules.clone();
		KnowledgeBase sessionKB = new KnowledgeBase(facts, rules);
		HashMap<String, Location> newAllLocations = deepCopyAllLocations(allLocations);
		HashMap<String, Item> newAllItems = deepCopyAllItems(allItems);
		Location newStartLocation = newAllLocations.get(startLocation.name);
		GameSession session = new GameSession(this, name, new Inventory(), newStartLocation, sessionKB, 
				actions, newAllLocations, newAllItems, links);
		sessions.put(name, session);
	}
	
	public boolean isVictory(Location loc, Inventory inv) {
		return false;
		// return loc.equals(victoryLocation) || inv.hasItem(victoryItem);
	}
	
	public boolean isDefeat(Location loc) {
		return false;
		// return defeatLocations.containsKey(loc.name);
	}
	
	public void printVictoryText() {
		System.out.println(victoryText);
	}
	
	public void printDefeatText() {
		System.out.println(defeatText);
	}
	
	public static void main(String[] args) throws IOException, ClassNotFoundException {
		Scanner s = new Scanner(System.in);
		File f = new File("game.ser");
		Game g = null;
		if (!f.exists()) {
			g = new Game("roomGame");
		}
		else {
			FileInputStream fis = new FileInputStream(f);
			ObjectInputStream ois = new ObjectInputStream(fis);
			g = (Game) ois.readObject();
		}
		while (true) {
			System.out.println("Select an action: (play), (edit), (q)uit");
			String choice = s.nextLine();
			switch (choice) {
				case "play":
					g.playGame();
					break;
				case "edit":
					g.editGame();
					break;
				case "q":
					return;
				default:
					System.out.println("Invalid option.");
			}
		}
	}
}
