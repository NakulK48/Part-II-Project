package nlp;

import java.util.List;
import java.util.Set;

public class AnalyzedInput {

	public List<String> nouns;
	public String verb;
	public Set<String> verbSynonyms;
	
	public AnalyzedInput(List<String> nouns, String verb, Set<String> verbSynonyms) {
		super();
		this.nouns = nouns;
		this.verb = verb;
		this.verbSynonyms = verbSynonyms;
	}

	@Override
	public String toString() {
		return "AnalyzedInput [nouns=" + nouns + ", verb=" + verb + ", verbSynonyms=" + verbSynonyms + "]";
	}

	
}
