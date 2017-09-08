import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class main {
	static String[] stopwordlist = new String[1000];
	static String word;
	static Stemmer s = new Stemmer();
	static TfIdf tf;
	static String file;
	static String[] class1 = new String[5000];
	static Double[] dfIDF;
	static String[] classtext = new String[13];

	public static void main(String[] args) throws IOException {

		Map<String, URL> trainingFiles = new HashMap<>();// <class name,training
															// data resource>
		for (int i = 1; i <= 13; i++) {
			trainingFiles.put("" + i,
					new File("classtext/class" + i + ".txt").toURL());
		}
		Map<String, String[]> trainingExamples = new HashMap<>();// <class
																	// name,doc
																	// content>

		for (Map.Entry<String, URL> entry : trainingFiles.entrySet()) {
			trainingExamples.put(entry.getKey(), readLines(entry.getValue()));
		}

		// train classifier
		NaiveBayes nb = new NaiveBayes();
		nb.setChisquareCriticalValue(13.0);
		nb.train(trainingExamples);

		// get trained classifier knowledgeBase
		NaiveBayesKnowledgeBase knowledgeBase = nb.getKnowledgeBase();
		
		nb = null;
		trainingExamples = null;

		// Use classifier
		nb = new NaiveBayes(knowledgeBase);

		String outputfile = "doc_id\t\tclass_id\n";
		HashMap<Integer, Integer> hashmap = new HashMap<>();
		TreeMap<Integer, Integer> treeMap = new TreeMap<Integer, Integer>();

		File folder = new File("testing_set/");
		File[] listOfFiles = folder.listFiles();
		
		for (File f : listOfFiles) {
			String testdoc = readFile("testing_set/" + f.getName());
			String predictclass = nb.predict(testdoc);
			String docname = f.getName();

			hashmap.put(Integer.parseInt(docname.substring(0,
					docname.indexOf("."))), Integer.parseInt(predictclass));
		}
		
		treeMap.putAll(hashmap);
		
		for (Map.Entry<Integer, Integer> e : treeMap.entrySet()) {
			outputfile += String.format("%-15d\t%-15d\n", e.getKey(),
					e.getValue());
		}
		
		System.out.printf(outputfile);
		
		savetofile("output.txt", outputfile);
	}

	/**
	 * Reads the all lines from a file and places it a String array. In each
	 * record in the String array we store a training example text.
	 * 
	 * @param url
	 * @return string[]
	 * @throws IOException
	 */
	public static String[] readLines(URL url) throws IOException {
		Reader fileReader = new InputStreamReader(url.openStream(),
				Charset.forName("UTF-8"));
		List<String> lines;
		try (BufferedReader bufferedReader = new BufferedReader(fileReader)) {
			lines = new ArrayList<>();
			String line;
			while ((line = bufferedReader.readLine()) != null) {
				lines.add(line);
				// System.out.println(line);
			}
		}
		return lines.toArray(new String[lines.size()]);
	}

	/**
	 * Read the all file and combine their text to a bigger text.
	 * 
	 * @param String classpath
	 * @return String content
	 */
	public static String getclasstext(String classpath) throws IOException {
		String content = "";
		File folder = new File(classpath);
		File[] listOfFiles = folder.listFiles();
		for (File file : listOfFiles) {
			content += readFile(file.getPath());
		}
		return content;
	}

	/**
	 * get the number of file under dir f
	 * 
	 * @param File f
	 * @return size
	 * 
	 */
	public static long getsize(File f) {
		long size = 0;
		File flist[] = f.listFiles();
		size = flist.length;
		for (int i = 0; i < flist.length; i++) {
			if (flist[i].isDirectory()) {
				size = size + getsize(flist[i]);
				size--;
			}
		}
		return size;
	}
	
	/**
	 * move the source file to new destination directory 
	 * 
	 * @param String srcfile,String dstdir
	 *
	 */
	public static void movefile(String srcfile, String dstdir) {
		File srcFile = new File(srcfile);
		if (!srcFile.exists() || !srcFile.isFile())
			System.out.println("error exitst!");

		File destDir = new File(dstdir);
		if (!destDir.exists())
			destDir.mkdirs();
		srcFile.renameTo(new File(dstdir + File.separator + srcFile.getName()));
	}

	/**
	 * save contents to a file 
	 * 
	 * @param String savefn,String content
	 *
	 */
	public static void savetofile(String savefn, String content)
			throws IOException {
		BufferedWriter writer = new BufferedWriter(new FileWriter(savefn));
		writer.write(content);
		writer.close();
	}

	/**
	 * Read the file name and return it text
	 * 
	 * @param String filename
	 * @return String text
	 */
	public static String readFile(String fileName) throws IOException {
		BufferedReader br = new BufferedReader(new FileReader(fileName));
		try {
			StringBuilder sb = new StringBuilder();
			String line = br.readLine();
			while (line != null) {
				sb.append(line);
				line = br.readLine();
			}
			return sb.toString();
		} finally {
			br.close();
		}
	}

	public static void prepreare() throws IOException {
		String splitline[] = readFile("training.txt").split("\n");
		ArrayList<String> addword = new ArrayList<String>();

		for (int i = 0; i < 13; i++) {
			String account[] = splitline[i].split(" ");
			for (int j = 1; j <= 15; j++) {
				System.out.println(account[j] + ".txt" + " class" + (i + 1));
				movefile("IRTM/" + account[j] + ".txt", "training_set/class"
						+ (i + 1));
			}
		}
	}

}
