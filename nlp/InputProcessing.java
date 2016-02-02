package nlp;

import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import edu.smu.tspell.wordnet.Synset;
import edu.smu.tspell.wordnet.SynsetType;
import edu.smu.tspell.wordnet.VerbSynset;
import edu.smu.tspell.wordnet.WordNetDatabase;
import edu.stanford.nlp.ling.CoreAnnotations.PartOfSpeechAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.SentencesAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.TextAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.TokensAnnotation;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.sentiment.SentimentCoreAnnotations.SentimentClass;
import edu.stanford.nlp.util.CoreMap;

public class InputProcessing {
	
	Properties props = new Properties();
	StanfordCoreNLP pipeline;
	
	public InputProcessing() {
		props.setProperty("annotators", "tokenize, ssplit, pos, parse, sentiment");
		pipeline = new StanfordCoreNLP(props);
	}
	
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
}
