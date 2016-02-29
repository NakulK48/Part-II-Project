package nlp;

public class InvalidInputException extends Exception {

	public String x = "";
	
	public InvalidInputException(String x) {
		this.x = x;
	}
}
