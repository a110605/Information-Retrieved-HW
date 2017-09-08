import java.util.HashMap;
import java.util.Map;

public class Document {

	/**
	 * List of token counts
	 */
	public Map<String, Integer> tokens;// token and its occurrences in class

	/**
	 * The class of the document
	 */
	public String category;
	
	/**
	 * Document constructor
	 */
	public Document() {
		tokens = new HashMap<>();
	}
}
