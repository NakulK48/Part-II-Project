package nlp;

import java.io.OutputStream;
import java.io.PrintStream;
import java.io.Serializable;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import edu.smu.tspell.wordnet.Synset;
import edu.smu.tspell.wordnet.SynsetType;
import edu.smu.tspell.wordnet.VerbSynset;
import edu.smu.tspell.wordnet.WordNetDatabase;
import edu.stanford.nlp.ie.util.RelationTriple;
import edu.stanford.nlp.simple.Document;
import edu.stanford.nlp.simple.Sentence;

public class InputProcessing implements Serializable {
	
	public InputProcessing() {
		System.setProperty("wordnet.database.dir", "C:/WordNet/2.1/dict/");
		
		System.out.println("\nLoading; please wait...\n");
		// Store original print stream.
		PrintStream err = System.err;

		// Silence all writers to print stream to avoid annoying CoreNLP setup text.
		System.setErr(new PrintStream(new OutputStream() {
		    public void write(int b) {
		    }
		}));
		
		// So the loading time is now and not later.
		analyzeInput("open red door with red key");
		
		// Re-enable original print stream.
		System.setErr(err);   
	}
	
	public static Set<String> getVerbSynonymsAndHypernyms(String word) {
		WordNetDatabase wndb = WordNetDatabase.getFileInstance();
		Synset[] sss = wndb.getSynsets(word, SynsetType.VERB);
		HashSet<String> allSynonyms = new HashSet<String>();
		for (int i = 0; i < sss.length; i++) {
			VerbSynset ss = (VerbSynset) sss[i];
			List<String> synonyms = Arrays.asList(ss.getWordForms());
			allSynonyms.addAll(synonyms);
			VerbSynset[] hss = ss.getHypernyms();
			for (int j = 0; j < hss.length; j++) {
				VerbSynset hs = hss[j];
				List<String> hypernyms = Arrays.asList(hs.getWordForms());
				allSynonyms.addAll(hypernyms);
			}
		}
		return allSynonyms;
	}
	
	public AnalyzedInput analyzeInput(String input) {
		AnalyzedInput ai = null;
		
		ai = ieParse(input);
		if (ai.nouns.size() > 0) return ai;
		
		ai = posParse(input);
		
		return ai;
	}
	
	public String getFirstWord(String input) {
		LinkedList<String> newInput = new LinkedList<String>(Arrays.asList(input.split(" ")));
		return newInput.get(0);
	}
	
	public String removeFirstWord(String input) {
		LinkedList<String> newInput = new LinkedList<String>(Arrays.asList(input.split(" ")));
		newInput.remove(0);
		return String.join(" ", newInput);
	}
	
	private AnalyzedInput posParse(String input) {
		List<String> nouns = new LinkedList<String>();
		String verb = getFirstWord(input);
		Set<String> verbSynonyms = null;
		
		Document doc = new Document(input);
		
		// There should only be one sentence. Ignore others.
		
		Sentence sentence = doc.sentence(0);
		String currentNoun = "";
		boolean prevWasNoun = false;
		for (int i = 0; i < sentence.length(); i++) {
			String word = sentence.word(i);
			String tag = sentence.posTag(i);
			// Include preceding adjective in the noun.
			if (tag.startsWith("J")) currentNoun += word + " ";
			// Bring in all previous adjectives and the noun itself
			if (tag.startsWith("N")) {
				if (prevWasNoun) {
					//handle compound nouns
					currentNoun = nouns.remove(nouns.size() - 1);
					nouns.add(currentNoun + " " + word);
					currentNoun = "";
				} else {
					nouns.add(currentNoun + word);
					currentNoun = "";
					prevWasNoun = true;
				}
			}
			else prevWasNoun = false;
			// Only one verb
			if (tag.startsWith("V")) verb = word;
		}
		if (verb != null) verbSynonyms = getVerbSynonymsAndHypernyms(verb);
		return new AnalyzedInput(nouns, verb, verbSynonyms);
	}
	
	private AnalyzedInput ieParse(String input) {
		
		List<String> nouns = new LinkedList<String>();
		
		String verb = getFirstWord(input);
		Set<String> verbSyn = getVerbSynonymsAndHypernyms(verb);
		
		String text = removeFirstWord(input);
		
		Document doc = new Document(text);
		
		// There should only be one sentence. Ignore others.
		Sentence sentence = doc.sentence(0);
		
		// Only one of these as well, but no easy way of getting it by itself.
		for (RelationTriple triple : sentence.openieTriples()) {
			nouns.add(triple.subjectGloss());
			nouns.add(triple.objectGloss());
		}
		
		AnalyzedInput ai = new AnalyzedInput(nouns, verb, verbSyn);
		return ai;
	}
	
	public static void main(String[] args) throws InvalidInputException {
		String text1 = "burn plank";
		String text2 = "open red door with red key";
		
		InputProcessing ip = new InputProcessing();
		System.out.println(ip.analyzeInput(text1));
		System.out.println(ip.analyzeInput(text2));
		//System.out.println(ip.analyzeInput(text));
	}
}
