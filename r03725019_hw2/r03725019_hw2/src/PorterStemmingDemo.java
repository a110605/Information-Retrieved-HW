import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Scanner;

public class PorterStemmingDemo {

	static int t_index = 0;
	static String[] stopwordlist = new String[1000];
	static Stemmer s = new Stemmer();
	static long startTime;
	static long ouputfileTime;
	static long calDFTime;
	static long calTFIDFTime;
	static long calcosTime;
	static int tmpfilenum = 1;
	static TfIdf tf;
	static String word;
	static String file;
	static Double[] dfIDF;// Variable to hold document frequency and IDF of each
	static double cos;
	static String outputfilename = "output_";
	static String cosdoc1;
	static String cosdoc2;
	static File a = new File("output");

	public static void main(String[] args) {
		try {
			
			// get startime for stamp
			startTime = System.currentTimeMillis();
			
			if (getlist(a) == 0) {
				// stem the word in IRTM dir
				dostemming("IRTM");
			}
			
			// do stopwordremoval and output result to output dir
			tf = new TfIdf("output");
			
			// cal all doc DF in1collection
			calDF();
			
			// cal every term's TFIDF in each doc in collection
			tf.buildAllDocuments();
			calTFIDF();
			
			// cal cos sim for doc1 and doc2
			System.out
					.printf("Enter two docs to calculate their cos similiarity. e.g.output_1.txt & output_2.txt\n");
			System.out.printf("Doc1:");
			Scanner s = new Scanner(System.in);
			cosdoc1 = s.nextLine();
			System.out.printf("Doc2:");
			cosdoc2 = s.nextLine();
			System.out.println("cos similarity = "
					+ cosSimilarity(tf.documents.get(cosdoc1),
							tf.documents.get(cosdoc2)));

			// show time statistic
			System.out.println(timest());
		} catch (FileNotFoundException e) {
			System.out.println("file " + " not found");
		} catch (IOException e1) {
			e1.printStackTrace();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	// get the file number under dir f
	public static long getlist(File f) {
		long size = 0;
		File flist[] = f.listFiles();
		size = flist.length;
		for (int i = 0; i < flist.length; i++) {
			if (flist[i].isDirectory()) {
				size = size + getlist(flist[i]);
				size--;
			}
		}
		return size;
	}

	public static String timest() {
		System.out
				.println("--------------------Time Statistic-------------------------");
		String time = "";
		long calcosTime = System.currentTimeMillis();
		if (getlist(a) == 0) {
			time += "Using Time for output file : "
					+ (ouputfileTime - startTime) / 1000 + " secs\n";
		}
		time += "Using Time for cal DF : " + (calDFTime - startTime) / 1000
				+ " secs\n";
		time += "Using Time for cal TFIDF : " + (calTFIDFTime - calDFTime)
				/ 1000 + " secs\n";
		time += "Total using Time : " + (calcosTime - startTime) / 1000
				+ " secs\n";
		return time;
	}

	public static void calDF() throws IOException {
		String outputDF = "";
		outputDF += "t_index\tTerm\t\tDocument Frequency\n";

		for (Iterator<String> it = tf.allwords.keySet().iterator(); it
				.hasNext();) {
			word = it.next();
			dfIDF = tf.allwords.get(word);
			t_index++;
			// dfIDF[0] is the DF of the word and dfIDF[1] is the IDF of the
			// word
			outputDF += String.format("%-6s\t%-15s\t%-1.0f\n", t_index, word,
					dfIDF[0]);
		}

		System.out.println("Output dictionary: "+ System.getProperty("user.dir") + "/dictionary/dictionary.txt");
		savetofile("dictionary/dictionary.txt", outputDF);
		calDFTime = System.currentTimeMillis();
	}

	public static double cosSimilarity(Document doc1, Document doc2) {
		String word;
		String word1;
		String word2;
		double similarity = 0;
		double unitlength1 = 0;
		double unitlength2 = 0;
		System.out
				.println("-------------------cos similarity--------------------------");
		System.out.println("doc1:" + cosdoc1 + ", doc2:" + cosdoc2);
		for (Iterator<String> it = doc1.words.keySet().iterator(); it.hasNext();) {
			word = it.next();
			if (doc2.words.containsKey(word)) 
				similarity += (doc1.words.get(word)[2] * doc2.words.get(word)[2]);
		}

		for (Iterator<String> it = doc1.words.keySet().iterator(); it.hasNext();) {
			word1 = it.next();
			unitlength1 += Math.pow(doc1.words.get(word1)[2], 2);
		}
		unitlength1 = Math.sqrt(unitlength1);

		for (Iterator<String> it = doc2.words.keySet().iterator(); it.hasNext();) {
			word2 = it.next();
			unitlength2 += Math.pow(doc2.words.get(word2)[2], 2);
		}
		unitlength2 = Math.sqrt(unitlength2);

		System.out.println("similarity = " + similarity);
		System.out
				.println("unitl1 = " + unitlength1 + ", unitl2 = "
						+ unitlength2 + "\nuni1*unit2 = "
						+ (unitlength1 * unitlength2));
		
		similarity = similarity / (unitlength1 * unitlength2);
		calcosTime = System.currentTimeMillis();
		return similarity;
	}

	public static void calTFIDF() throws IOException {
		Map<String, Double[]> myMap = new HashMap<String, Double[]>();
		Double[] values;
		String output = "";
		
		for (Iterator<String> it = tf.documents.keySet().iterator(); it
				.hasNext();) {
			file = it.next();
			myMap = tf.documents.get(file).getF_TF_TFIDF();
			output += "Term Number = " + myMap.size() + "\nTerm\t\t\tTFIDF\n";
			for (String key : myMap.keySet()) {
				values = myMap.get(key);
				output += String.format("%-15s\t%-1.6f\n", key, values[2]);
			}
			savetofile("TF_IDF/tf_idf_" + file, output);
			output = "";
		}
  
		System.out.println("Output TFIDF directory:" + System.getProperty("user.dir") + "/TFIDF/");
		System.out.println("------------------------------------------------------------");
		
		calTFIDFTime = System.currentTimeMillis();
	}

	public static void dostemming(String indir) throws FileNotFoundException,
			InterruptedException {
		File folder = new File(indir);
		File[] listOfFiles = folder.listFiles();
		for (File file : listOfFiles) {
			System.out.println("Stem file for output : " + outputfilename
					+ file.getName());
			stemming(file.getPath(), file.getName());
		}
		ouputfileTime = System.currentTimeMillis();
	}

	public static void stemming(String tmpfilename, String savename)
			throws FileNotFoundException {
		char[] w = new char[1000];
		String outputstring = "";
		FileInputStream in = new FileInputStream(tmpfilename);
		try {
			while (in.available() > 0) {
				int ch = in.read();
				if (Character.isLetter((char) ch)) {// if char ch is
					// letter,ex:abc...
					int j = 0;
					while (in.available() > 0) {// file content still have
						// letter
						// Lowercasing all character
						// ch = Character.toLowerCase((char) ch);
						w[j] = (char) ch;
						if (j < 1000)
							j++;
						ch = in.read();
						// ex: whitespace
						if (!Character.isLetter((char) ch)) {
							// add ch to w[] represent a word
							for (int c = 0; c < j; c++)
								s.add(w[c]);

							s.stem();// stem the word
							{
								String u = s.toString();
								outputstring += u + " ";
								// System.out.println(u);
							}
							break;
						}// end !Character.isLetter
					}// END while true
				}// end Character.isLetter
			}// end while
			savetofile("output/" + outputfilename + savename, outputstring);// output_1.txt...
		} catch (IOException e) {
			System.out.println("error reading " + tmpfilename);
		}

	}

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

	public static void savetofile(String savefn, String content)
			throws IOException {
		BufferedWriter writer = new BufferedWriter(new FileWriter(savefn));
		writer.write(content);
		writer.close();
	}
}