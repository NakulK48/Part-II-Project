package nlp;

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

public class InputProcessing {
	
	// Requires '-Dwordnet.database.dir=C:\WordNet\2.1\dict\' as a VM arg.
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
	
	/*
	public AnalyzedInput analyzeInput(String text) throws InvalidInputException {
		Annotation document = new Annotation(text);
		pipeline.annotate(document);
		List<CoreMap> sentences = document.get(SentencesAnnotation.class);
		if (sentences.size() != 1) {
			throw new InvalidInputException();
		}
		List<String> nouns = new LinkedList<String>();
		String verb = null;
		Set<String> verbSynonyms = null;
		String sentiment = null;
		
		// There should only be one sentence.
		for (CoreMap sentence : sentences) {
			for (CoreLabel token : sentence.get(TokensAnnotation.class)) {
				String word = token.getString(TextAnnotation.class);
				String pos = token.getString(PartOfSpeechAnnotation.class);
				if (pos.startsWith("N")) {
					nouns.add(word);
				}
				if (pos.startsWith("V")) {
					verb = word;
					verbSynonyms = getVerbSynonymsAndHypernyms(word);
				}
			}
			
			sentiment = sentence.get(SentimentClass.class);
		}
		
		if (nouns.size() == 0 || verb == null) {
			// Fall back on verb noun if the more sophisticated parse fails.
			return naivelyAnalyzeInput(text);
		}
		
		return new AnalyzedInput(nouns, verb, verbSynonyms, sentiment);
	}
	
	public AnalyzedInput naivelyAnalyzeInput(String text) {
		String[] split = text.split(" ");
		List<String> nouns = new LinkedList<String>();
		String noun = split[1];
		nouns.add(noun);
		String verb = split[0];
		Set<String> verbSynonyms = new HashSet<String>();
		String sentiment = "neutral";
		return new AnalyzedInput(nouns, verb, verbSynonyms, sentiment);
	}
	*/
	
	public AnalyzedInput analyzeInput(String input) throws InvalidInputException {
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
	
	private AnalyzedInput posParse(String input) throws InvalidInputException {
		List<String> nouns = new LinkedList<String>();
		String verb = null;
		Set<String> verbSynonyms = null;
		
		Document doc = new Document(input);
		
		if (doc.sentences().size() != 1) {
			throw new InvalidInputException();
		}
		
		// There should only be one sentence.
		for (Sentence sentence : doc.sentences()) {
			String currentNoun = "";
			for (int i = 0; i < sentence.length(); i++) {
				String word = sentence.word(i);
				String tag = sentence.posTag(i);
				// Include preceding adjective in the noun.
				if (tag.startsWith("J")) currentNoun += word + " ";
				// Bring in all previous adjectives and the noun itself
				if (tag.startsWith("N")) {
					nouns.add(currentNoun + word);
					currentNoun = "";
				}
				// Only one verb
				if (tag.startsWith("V")) verb = word;
			}
		}
		verbSynonyms = getVerbSynonymsAndHypernyms(verb);
		return new AnalyzedInput(nouns, verb, verbSynonyms);
	}
	
	private AnalyzedInput ieParse(String input) {
		
		List<String> nouns = new LinkedList<String>();
		
		String verb = getFirstWord(input);
		Set<String> verbSyn = getVerbSynonymsAndHypernyms(verb);
		
		String text = removeFirstWord(input);
		
		Document doc = new Document(text);
		for (Sentence s : doc.sentences()) {
			for (RelationTriple triple : s.openieTriples()) {
				nouns.add(triple.subjectGloss());
				nouns.add(triple.objectGloss());
			}
			
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
