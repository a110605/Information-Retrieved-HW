import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;

public class main {
	static String[] stopwordlist = new String[1000];
	static String word;
	static TfIdf tf;
	static String file;
	static Stemmer s = new Stemmer();
	static String[] class1 = new String[5000];
	static Double[] dfIDF;
	static double[][] c;
	static int[] I;
	static int N;
	static File folder = new File("IRTM1");
	File[] listOfFiles = folder.listFiles();
	static HashMap<Integer, ArrayList<String>> A = new HashMap<Integer, ArrayList<String>>();

	public static void main(String[] args) throws IOException {
		tf = new TfIdf("IRTM1");
		tf.buildAllDocuments();

		N = getlist(folder);// 20
		I = new int[N];
		c = new double[N][N];
		int cluster;

		String o = "";
		for (int i = 0; i < N; i++) {// i=0~19
			for (int j = 0; j < N; j++) {// j=1~19
				c[i][j] = cosSimilarity(tf.documents.get((i + 1) + ".txt"),
						tf.documents.get((j + 1) + ".txt"));
			}
			ArrayList<String> a = new ArrayList<String>();
			a.add("");// 一開始先給""
			A.put(i, a);
			I[i] = 1;// all for 1
		}// end for
		cluster = A.size();

		System.out.println("Matrix C Done!" + o);

		for (int k = 0; k < N - 1; k++) {// k=0~N-2
			int i = MAXpair().get(0).intValue();
			int m = MAXpair().get(1).intValue();
			Double max = MAXpair().get(2);

			if (i != m && I[i] == 1 && I[m] == 1) {
				System.out.println("\n第" + (k + 1) + "輪 " + i + " " + m + " "
						+ max);
				if (A.containsKey(m)) {// 如果有此cluster
					if (!A.containsKey(i)) {
						A.put(i, new ArrayList<String>());
						String valueall = getAll(A.get(m));
						A.get(i).add(m + " " + valueall);

						A.remove(m);
					} else {
						String valueall = getAll(A.get(m));
						A.get(i).add(m + " " + valueall);

						A.remove(m);
					}

				} else {// 如果沒有此cluster
					if (A.containsKey(i)) {// 如果有此key，加到後面去
						A.get(i).add(m + " ");
					} else {// 如果沒有new一個新的
						A.put(i, new ArrayList<String>());
						A.get(i).add(m + " ");
					}
				}

				for (int j = 0; j < N; j++) {
					c[i][j] = MAX(c[j][i], c[j][m]);
					c[j][i] = MAX(c[j][i], c[j][m]);
				}

				cluster = A.size();
				System.out.println("clusternum = " + cluster);
				if ((cluster == 8) || (cluster == 13) || (cluster == 20))
					savetoresult(cluster);

				displayA();
				I[m] = 0;
				i = 0;
				m = 0;
			}// end if
		}// end for
		System.out.println("HAC Done!");
	}

	public static void savetoresult(int k) throws IOException {
		String finaloutput = "";
		for (Entry<Integer, ArrayList<String>> e : A.entrySet()) {
			String temp = "";
			ArrayList<String> v = e.getValue();

			for (int i = 0; i < v.size(); i++) {
				temp += v.get(i) + "";
			}
			String[] o = temp.trim().split(" ");
			int[] number = new int[o.length];// 19
			if (!temp.equals("")) {// 處理value!=""
				// String[]轉呈int[]
				for (int i = 0; i < o.length; i++) {
					// if (!o[i].equals(""))
					number[i] = Integer.valueOf(o[i]);
				}
				Arrays.sort(number);// sort 後面的值
				for (int i : number) {
					finaloutput += i + "\n";
				}
			}
			finaloutput += e.getKey() + "\n";
			finaloutput += "\n";
		}// end for
		savetofile(k + ".txt", finaloutput);
	}

	public static String getAll(ArrayList<String> a) {
		String r = "";
		for (int i = 0; i < a.size(); i++) {
			r += a.get(i) + "";
		}
		r.trim();
		return r;
	}

	public static int clustersize() {
		int size = 0;
		size = A.size();
		return size;
	}

	public static void displayA() {
		for (Entry<Integer, ArrayList<String>> entry : A.entrySet()) {
			System.out.print(entry.getKey() + ":");
			for (String fruitNo : entry.getValue()) {
				System.out.print(fruitNo);
			}
			System.out.println();
		}
	}// end displayA

	public static double MAX(double sim1, double sim2) {// 取小的那個
		if (sim1 < sim2) {
			return sim1;
		} else {
			return sim2;
		}
	}

	public static List<Double> MAXpair() {
		int i = 0, j = 0;
		List<Double> result = new ArrayList<Double>();
		double max = 0;
		for (i = 0; i < N; i++) {// i=0~19
			for (j = 0; j < N; j++) {// i=1~19
				if ((I[j] == 1) && (I[i] == 1) && (i != j) && (max <= c[i][j])) {
					max = c[i][j];
					result.add(0, (double) i);
					result.add(1, (double) j);
					result.add(2, max);
				}
			}
		}
		return result;
	}

	// get the file num under dir f
	public static int getlist(File f) {
		int size = 0;
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

	public static double cosSimilarity(Document doc1, Document doc2) {
		String word;
		String word1;
		String word2;
		double similarity = 0;
		double unitlength1 = 0;
		double unitlength2 = 0;

		for (Iterator<String> it = doc1.words.keySet().iterator(); it.hasNext();) {
			word = it.next();
			if (doc2.words.containsKey(word)) {
				similarity += (doc1.words.get(word)[2] * doc2.words.get(word)[2]);
			}
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

		similarity = similarity / (unitlength1 * unitlength2);

		return similarity;
	}

	/**
	 * Read the all file and combine their text to a bigger text.
	 * 
	 * @param String
	 *            classpath
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
	 * @param File
	 *            f
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
	 * save contents to a file
	 * 
	 * @param String
	 *            savefn,String content
	 */
	public static void savetofile(String savefn, String content)
			throws IOException {
		BufferedWriter writer = new BufferedWriter(new FileWriter(savefn));
		writer.write(content);
		writer.close();
	}

	public static void dostemming(String indir) throws FileNotFoundException,
			InterruptedException {
		File folder = new File(indir);
		File[] listOfFiles = folder.listFiles();
		for (File file : listOfFiles) {
			System.out.println("Stem file for : " + file.getName());
			stemming(file.getPath(), file.getName());
		}

	}

	/**
	 * Read the file name and return it text
	 * 
	 * @param String
	 *            filename
	 * @return String text
	 */
	public static String readFile(String fileName) throws IOException {
		BufferedReader br = new BufferedReader(new FileReader(fileName));
		try {
			StringBuilder sb = new StringBuilder();
			String line = br.readLine();
			while (line != null) {
				sb.append(line);
				// sb.append("\n");
				line = br.readLine();
			}
			return sb.toString();
		} finally {
			br.close();
		}
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
			savetofile("output/" + savename, outputstring);// output_1.txt...
		} catch (IOException e) {
			System.out.println("error reading " + tmpfilename);
		}
	}

}