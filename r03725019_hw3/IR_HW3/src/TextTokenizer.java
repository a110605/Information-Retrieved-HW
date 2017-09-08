import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class TextTokenizer {
	/**
	 * Preprocess the text by removing punctuation, duplicate spaces and
	 * lowercasing it.
	 * 
	 * @param text
	 * @return processed text
	 */
	public static String preprocess(String text) {
		return text.replaceAll("\\p{P}", " ").replaceAll("\\s+", " ")
				.toLowerCase(Locale.getDefault());
	}

	/**
	 * A simple method to extract the keywords from the text. For real world
	 * applications it is necessary to extract also keyword combinations.
	 * 
	 * @param text
	 * @return string[]
	 */
	public static String[] extractKeywords(String text) {
		return text.split(" ");
	}

	/**
	 * Counts the number of occurrences of the keywords inside the text.
	 * 
	 * @param keywordArray
	 * @return Map<word,occurrences> counts
	 */
	public static Map<String, Integer> getKeywordCounts(String[] keywordArray) {
		Map<String, Integer> counts = new HashMap<>();
		Integer counter;

		for (int i = 0; i < keywordArray.length; ++i) {
			counter = counts.get(keywordArray[i]);
			if (counter == null) {
				counter = 0;
			}
			counts.put(keywordArray[i], ++counter); // increase counter for the
													// keyword
		}// end for
		return counts;
	}

	/**
	 * read file content
	 * 
	 * @param filename
	 * @return file content
	 */
	public static String readFile(String fileName) throws IOException {
		BufferedReader br = new BufferedReader(new FileReader(fileName));
		try {
			StringBuilder sb = new StringBuilder();
			String line = br.readLine();
			while (line != null) {
				sb.append(line);
				sb.append("\n");
				line = br.readLine();
			}
			return sb.toString();
		} finally {
			br.close();
		}
	}

	/**
	 * read file content
	 * 
	 * @param filename
	 * @return file content
	 */
	public static String[] getstopwordlist(String stopwordfile)
			throws IOException {
		return readFile(stopwordfile).split("\n");
	}
	public static void savetofile(String savefn, String content)
			throws IOException {
		BufferedWriter writer = new BufferedWriter(new FileWriter(savefn));
		writer.write(content);
		writer.close();
	}
	
	/**
	 * Tokenizes the document and returns a Document Object.
	 * 
	 * @param text
	 * @return Document doc
	 * @throws IOException
	 */
	public static Document tokenize(String text) {
		// all term in the training doc dataset
		String[] keywordArray = extractKeywords(preprocess(text));
		// String[] keywordArray = preprocess(text);
		Document doc = new Document();
		doc.tokens = getKeywordCounts(keywordArray);
//		for (Map.Entry<String, Integer> entry : doc.tokens.entrySet()) {
//			String key = entry.getKey();
//			int occ = entry.getValue();
//			// System.out.println (key + "\tdoc occ=" + occ);
//		}
		//System.out.println("keywordArray =" + keywordArray.length);
		
		
		//System.out.println("number of tokens size in class =" + doc.tokens.size());
		return doc;
	}
}
