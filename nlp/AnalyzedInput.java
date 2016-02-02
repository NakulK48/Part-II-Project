package nlp;

import java.util.List;
import java.util.Set;

public class AnalyzedInput {

	public List<String> nouns;
	public String verb;
	public Set<String> verbSynonyms;
	public String sentiment;
	
	public AnalyzedInput(List<String> nouns, String verb, Set<String> verbSynonyms, String sentiment) {
		super();
		this.nouns = nouns;
		this.verb = verb;
		this.verbSynonyms = verbSynonyms;
		this.sentiment = sentiment;
	}
	
	
	
}
