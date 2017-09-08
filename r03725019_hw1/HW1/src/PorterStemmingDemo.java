import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import javax.swing.*;
import javax.swing.event.*;

public class PorterStemmingDemo {
	static String[] stopwordlist = new String[500];
	static Stemmer s = new Stemmer();
	static String inputfilename = "input.txt";
	static String tmpfilename = "tmp_filted.txt";
	static String outputfilename = "output.txt";

	public static void main(String[] args) {
		String outputstring = "";
		char[] w = new char[501];
		
		for (int i = 0; i < 1; i++)
			try {
				getstopwordlist();
				stopwordremoval(readFile(inputfilename));
				FileInputStream in = new FileInputStream(tmpfilename);

				try {
					while (in.available() > 0) {
						int ch = in.read();
						if (Character.isLetter((char) ch)) {
							int j = 0;
							while (in.available() > 0) {
								// Lowercasing all character
								w[j] = (char) ch;
								if (j < 500)
									j++;
								ch = in.read();
								// if char ch is not letter, ex:whitespace
								if (!Character.isLetter((char) ch)) {
									
									// add ch to w[] represent a word
									for (int c = 0; c < j; c++)
										s.add(w[c]);
									/* or, to test add(char[] w, int j) */
									/* s.add(w, j); */
									
									s.stem();// stem the word
									{
										String u = s.toString();
										outputstring += u + "\n";
										// System.out.println(u);
									}
									break;
								}// end !Character.isLetter
							}// END while true
						}// end Character.isLetter
					}// end while
					
					savetofile(outputfilename, outputstring);
					JOptionPane.showMessageDialog(null, "Output file at "+ System.getProperty("user.dir")+"/"+outputfilename);
				} catch (IOException e) {
					System.out.println("error reading " + tmpfilename);
				}
			} catch (FileNotFoundException e) {
				System.out.println("file " + tmpfilename + " not found");
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
	}

	// read .txt file to a string
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

	// get String array : stopwordarray
	public static void getstopwordlist() throws IOException {
		stopwordlist = readFile("stopwordlist.txt").split("\n");
	}

	// save content to a file
	public static void savetofile(String savefn, String content)
			throws IOException {
		BufferedWriter writer = new BufferedWriter(new FileWriter(savefn));
		writer.write(content);
		writer.close();
	}

	// remove stop word and save to a tmpfile
	public static void stopwordremoval(String u) throws IOException {
		String s = "";
		String splitline[] = u.split("\n");
		ArrayList<String> addword = new ArrayList<String>();
		for (String abc : splitline) {
			String account[] = abc.split(" ");
			for (String acc : account) {
				addword.add(acc);
			}
		}

		for (int i = 0; i < addword.size(); i++) {
			// lowercase all string in addword arraylist
			String lowercase = addword.get(i).toLowerCase();
			
			// filter all string with "'"
			if (lowercase.contains("'")) {
				int lastindex = lowercase.indexOf("'");
				String removalstring = lowercase.substring(0, lastindex);
				lowercase = removalstring;
				
			}
			
			// filter all string with stopwordlist
			if (!Arrays.asList(stopwordlist).contains(lowercase)) 
				s += lowercase + " ";
		}
		savetofile(tmpfilename, s);
	}
}