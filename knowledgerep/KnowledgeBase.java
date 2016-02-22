package knowledgerep;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.Serializable;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import alice.tuprolog.InvalidTheoryException;
import alice.tuprolog.MalformedGoalException;
import alice.tuprolog.NoSolutionException;
import alice.tuprolog.Prolog;
import alice.tuprolog.SolveInfo;
import alice.tuprolog.Term;
import alice.tuprolog.Theory;
import alice.tuprolog.UnknownVarException;
import gameconcepts.Item;

public class KnowledgeBase implements Cloneable, Serializable {
	
	public Theory base;
	public HashSet<String> facts;
	public HashSet<String> rules;
	
	private static String sanitiseString(String s) {
		return s.replace(" ", "_");
	}
	
	private static String desanitiseString(String s) {
		return s.replace("_", " ");
	}
	
	public KnowledgeBase() throws FileNotFoundException, IOException {
		base = new Theory(new FileInputStream("knowledgerep/expertSystem.pl"));
		this.facts = new HashSet<String>();
		this.rules = new HashSet<String>();
	}
	
	public KnowledgeBase(HashSet<String> facts, HashSet<String> rules) throws FileNotFoundException, IOException {
		base = new Theory(new FileInputStream("knowledgerep/expertSystem.pl"));
		this.facts = facts;
		this.rules = rules;
	}
	
	public KnowledgeBase(KnowledgeBase kb) throws FileNotFoundException, IOException {
		base = new Theory(new FileInputStream("knowledgerep/expertSystem.pl"));
		this.facts = kb.facts;
		this.rules = kb.rules;
	}
	
	public static String generateFact(String functor, String arg1, String arg2) {
		return sanitiseString(arg1) + " " + functor + " " + sanitiseString(arg2) + "."; 
	}
	
	public void addFact(String fact) {
		facts.add(fact);
	}
	
	public void addProperty(String property, String item) {
		// Because Prolog can be very stupid.
		if (property.replace(" ", "").equals("")) return;
		String fact = "fact : " + sanitiseString(item) + " hasproperty " + sanitiseString(property) + ".";
		addFact(fact);
	}
	
	public void addProperties(List<String> properties, String item) {
		for (String property : properties) {
			addProperty(property, item);
		}
	}
	
	public void addProperties(Set<String> properties, String item) {
		for (String property : properties) {
			addProperty(property, item);
		}
	}
	
	//Perhaps add the ability to change this?
	public void addPropertyWithValue(String property, String value, String item) {
		String fact = sanitiseString(property) + "(" + sanitiseString(item) + "," + sanitiseString(value) + ").";
		facts.add(fact);
	}
	
	public SolveInfo hasProperty(String property, String item) {
		String fact = sanitiseString(item) + " hasproperty " + sanitiseString(property);
		SolveInfo s = query(fact);
		return s;
	}
	
	public boolean hasLink(String item1, String item2) throws SameItemException, NoSolutionException {
		if (item1.equals(item2)) throw new SameItemException();
		String fact = sanitiseString(item1) + " linkedwith " + sanitiseString(item2);
		SolveInfo s = query(fact);
		return getQuerySuccess(s);
	}
	
	private void addRelation(String functor, String item1, String item2) throws SameItemException {
		if (item1.equals(item2)) throw new SameItemException();
		String fact = generateFact(functor, item1, item2);
		facts.add(fact);
	}
	
	public void addLink(String item1, String item2) throws SameItemException {
		addRelation("linkedwith", item1, item2);
	}
	
	public void addOpen(String key, String door) throws SameItemException {
		addRelation("opens", key, door);
	}
	
	public boolean hasOpen(String key, String door) throws SameItemException, NoSolutionException {
		if (key.equals(door)) throw new SameItemException();
		String fact = generateFact("opens", key, door);
		SolveInfo s = query(fact);
		return getQuerySuccess(s);
	}
	
	public boolean fitsInside(String item, String container) throws NoSolutionException {
		String fact = generateFact("fitsInside", item, container);
		SolveInfo s = query(fact);
		return getQuerySuccess(s);
	}
	
	public boolean isInside(String item, String container) throws NoSolutionException {
		String fact = generateFact("inside", item, container);
		SolveInfo s = query(fact);
		return getQuerySuccess(s);
	}
	
	public void putInside(String item, String container) throws SameItemException {
		addRelation("inside", item, container);
	}
	
	public void takeOut(String item, String container) {
		String fact = generateFact(item, "inside", container);
		facts.remove(fact);
	}
	
	public void removeProperty(String property, String item) {
		String fact = "fact : " + sanitiseString(item) + " hasproperty " + sanitiseString(property);
		facts.remove(fact);
	}
	
	public void removeProperties(List<String> properties, String item) {
		for (String property : properties) {
			removeProperty(property, item);
		}
	}
	
	public void removeProperties(Set<String> properties, String item) {
		for (String property : properties) {
			removeProperty(property, item);
		}
	}
	
	public void addRule(String rule) {
		rules.add(rule);
	}
	
	public List<String> sanitiseList(List<String> rawList) {
		LinkedList<String> sanitised = new LinkedList<String>();
		for (String raw : rawList) {
			sanitised.add(sanitiseString(raw));
		}
		
		return sanitised;
	}
	
	public void addRule(String conclusion, List<String> premises) {
		String rule = String.join("&", sanitiseList(premises)) + " implies " + sanitiseString(conclusion) + ".";
		addRule(rule);
	}
	
	public void removeRule(String conclusion) {
		for (String rule : rules) {
			if (rule.endsWith(conclusion + ".")) {
				rules.remove(rule);
			}
		}
	}
	
	public SolveInfo query(String query) {
		Prolog prolog = new Prolog();
		try {
			prolog.addTheory(base);
			prolog.addTheory(getTheory());
			SolveInfo s = prolog.solve(generateQuery(query));
			return s;
		} catch (InvalidTheoryException e) {
			System.out.println("Your theory is invalid!");
			System.out.println("Facts: " + theoryFromClauses(facts) + "\n" + "Rules: " + theoryFromClauses(rules));
			e.printStackTrace();
			return null;
		} catch (MalformedGoalException e) {
			System.out.println("Your query is invalid!");
			return null;
		}
	}
	
	public void printTheory() {
		System.out.println(theoryFromClauses(facts));
		System.out.println(theoryFromClauses(rules));
	}
	
	public boolean getQuerySuccess(SolveInfo s) throws NoSolutionException {
		if (s.getVarValue("T").toString().equals("true")) return true;
		if (s.getVarValue("T").toString().equals("false")) return false;
		throw new NoSolutionException();
	}
	
	public String getQueryFailureReason(SolveInfo s) throws NoSolutionException {
		Term t = s.getVarValue("R");
		String prologList = s.getVarValue("R").toString();
		return fullyParsePrologFailedPremiseList(prologList);
	}
	
	public static String generateQuery(String baseQuery) {
		return "answer(" + baseQuery + ", T, R).";
	}

	private Theory getTheory() throws InvalidTheoryException {
		String theoryString = theoryFromClauses(facts) + "\n" + theoryFromClauses(rules);
			Theory theory = new Theory(theoryString);
			return theory;
	}

	private static String theoryFromClauses(HashSet<String> clauses) {
		return String.join("\n", clauses);
	}
	
	private static List<String> javaListFromPrologList(String prologList) {
		int bracketDepth = 0;
		List<String> javaList = new LinkedList<String>();
		String currentElement = "";
		for (char c : prologList.toCharArray()) {
			if (c == '[') continue;
			if (c == ']') javaList.add(currentElement);
			if (c == ')') bracketDepth--;
			if (c == '(') bracketDepth++;
			if (bracketDepth == 0 && c == ',') {
				javaList.add(currentElement);
				currentElement = "";
				continue;
			}
			currentElement += c;
		}
		
		return javaList;
	}
	
	private static String parseFailedPremisePrologFunctor(String prologTerm) {
		Pattern p = Pattern.compile("(.*)\\((.*),(.*)\\)");
		Matcher m = p.matcher(prologTerm);
		m.matches();
		String functor = (m.group(1));
		String arg1 = (m.group(2));
		String arg2 = (m.group(3));
		if (functor.equals("hasproperty")) {
			return "the " + desanitiseString(arg1) + " is not " + desanitiseString(arg2);
		}
		return "it is not the case that " + desanitiseString(arg1) + " " + functor + " " + desanitiseString(arg2);
	}
	
	public static String fullyParsePrologFailedPremiseList(String prologList) {
		List<String> javaList = javaListFromPrologList(prologList);
		if (javaList.size() == 0) return "failed because that property is atomic and does not hold.";
		List<String> reasons = new LinkedList<String>();
		String fullReason = "failed because ";
		for (String s : javaList) {
			String reason = parseFailedPremisePrologFunctor(s);
			reasons.add(reason);
		}
		fullReason += String.join(" and ", reasons) + ".";
		return fullReason;
	}
	
	public static void main(String[] args) throws FileNotFoundException, IOException, NoSolutionException, UnknownVarException {
		KnowledgeBase kb = new KnowledgeBase();
		SolveInfo s = kb.query("plate hasproperty glass");
		System.out.println(kb.getQuerySuccess(s));
		String failureReason = kb.getQueryFailureReason(s);
		System.out.println(failureReason);
	}
}
