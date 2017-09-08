import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class NaiveBayes {
	private double chisquareCriticalValue = 10.83; // equivalent to pvalue
													// 0.001. It is used by
													// feature selection
													// algorithm

	private NaiveBayesKnowledgeBase knowledgeBase;

	/**
	 * This constructor is used when we load an already train classifier
	 * 
	 * @param knowledgeBase
	 */
	public NaiveBayes(NaiveBayesKnowledgeBase knowledgeBase) {
		this.knowledgeBase = knowledgeBase;
	}

	/**
	 * This constructor is used when we plan to train a new classifier.
	 */
	public NaiveBayes() {
		this(null);
	}

	/**
	 * Gets the knowledgebase parameter
	 * 
	 * @return
	 */
	public NaiveBayesKnowledgeBase getKnowledgeBase() {
		return knowledgeBase;
	}

	/**
	 * Gets the chisquareCriticalValue paramter.
	 * 
	 * @return
	 */
	public double getChisquareCriticalValue() {
		return chisquareCriticalValue;
	}

	/**
	 * Sets the chisquareCriticalValue parameter.
	 * 
	 * @param chisquareCriticalValue
	 */
	public void setChisquareCriticalValue(double chisquareCriticalValue) {
		this.chisquareCriticalValue = chisquareCriticalValue;
	}

	/**
	 * Preprocesses the original dataset and converts it to a List of Documents.
	 * 
	 * @param trainingDataset
	 * @return ArrayList of Document : dataset
	 */
	private List<Document> preprocessDataset(
			Map<String, String[]> trainingDataset) {
		List<Document> dataset = new ArrayList<>();// this is for storing our
													// Document object

		String category;
		String[] content;
		Document doc;
		Iterator<Map.Entry<String, String[]>> it = trainingDataset.entrySet()
				.iterator();
		// loop through all the categories and training examples
		while (it.hasNext()) {
			Map.Entry<String, String[]> entry = it.next();
			category = entry.getKey();// get class name
			content = entry.getValue();// get training data content

			// do something for all content in this class
			for (int i = 0; i < content.length; ++i) {
				// for each content in the category tokenize its text and
				// convert it into a Document object.
				doc = TextTokenizer.tokenize(content[i]);
				doc.category = category;// give to Document its class
				dataset.add(doc);
				// content[i] = null; //try freeing some memory
			}
			// it.remove(); //try freeing some memory
		}

		// System.out.println("dataset=" + dataset.size());
		return dataset;
	}

	/**
	 * Gathers the required counts for the features and performs feature
	 * selection on the above counts. It returns a FeatureStats object that is
	 * later used for calculating the probabilities of the model.
	 * 
	 * @param dataset
	 * @return
	 * @throws
	 */
	private FeatureStats selectFeatures(List<Document> dataset) {
		FeatureExtraction featureExtractor = new FeatureExtraction();

		// the FeatureStats object contains statistics about all the features
		// found in the documents
		FeatureStats stats = featureExtractor.extractFeatureStats(dataset); // extract
																			// the
																			// stats
																			// of
																			// the
																			// dataset

		// we pass this information to the feature selection algorithm and we
		// get a list with the selected features
		Map<String, Double> selectedFeatures = featureExtractor.chisquare(
				stats, chisquareCriticalValue);

		String o = "";
		for (Map.Entry<String, Double> e : selectedFeatures.entrySet()) {
			String feature = e.getKey();
			Double chi = e.getValue();
			o += feature + "\n";
			// System.out.println(feature + "\tchiscore=" + chi);
		}
		try {
			savetofile("selected_feature.txt", o);
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

		// clip from the stats all the features that are not selected
		Iterator<Map.Entry<String, Map<String, Integer>>> it = stats.featureCategoryJointCount
				.entrySet().iterator();
		while (it.hasNext()) {
			String feature = it.next().getKey();

			if (selectedFeatures.containsKey(feature) == false) {
				// if the feature is not in the selectedFeatures list remove it
				it.remove();
			}
		}

		return stats;
	}

	public static void savetofile(String savefn, String content)
			throws IOException {
		BufferedWriter writer = new BufferedWriter(new FileWriter(savefn));
		writer.write(content);
		writer.close();
	}

	/**
	 * Wrapper method of train() which enables the estimation of the prior
	 * probabilities based on the sample.
	 * 
	 * @param trainingDataset
	 */
	public void train(Map<String, String[]> trainingDataset) {
		train(trainingDataset, null);
	}

	/**
	 * Trains a Naive Bayes classifier by using the Multinomial Model by passing
	 * the trainingDataset and the prior probabilities.
	 * 
	 * @param trainingDataset
	 * @param categoryPriors
	 * @throws IllegalArgumentException
	 */
	public void train(Map<String, String[]> trainingDataset,
			Map<String, Double> categoryPriors) throws IllegalArgumentException {
		Document doc = new Document();

		// preprocess the given dataset
		List<Document> dataset = preprocessDataset(trainingDataset);

		// produce the feature stats and select the best features
		FeatureStats featureStats = selectFeatures(dataset);

		// intiliaze the knowledgeBase of the classifier
		knowledgeBase = new NaiveBayesKnowledgeBase();
		knowledgeBase.n = featureStats.n; // number of observations doc
		knowledgeBase.d = featureStats.featureCategoryJointCount.size(); // number
																			// of
																			// features

		// check is prior probabilities are given
		if (categoryPriors == null) {
			// System.out.println("categoryPriors == null");
			// if not estimate the priors from the sample
			knowledgeBase.c = featureStats.categoryCounts.size(); // get number
																	// of
																	// cateogries

			knowledgeBase.logPriors = new HashMap<>();
			String category;
			int count;

			// for (Document docc : dataset) {
			// //
			// System.out.println("class="+docc.category+" term size="+docc.tokens.size());
			// knowledgeBase.logPriors.put(docc.category,
			// (double) docc.tokens.size());
			// }

			for (Map.Entry<String, Integer> entry : featureStats.categoryCounts
					.entrySet()) {
				category = entry.getKey();// get class
				count = entry.getValue();// get 屬於此Class的doc數量
				knowledgeBase.logPriors.put(category,
						Math.log((double) count / knowledgeBase.n));// log
																	// P(c)=log(Nc/N)

			}
			// for (Map.Entry<String, Double> e : knowledgeBase.logPriors
			// .entrySet()) {
			// System.out.println("class:"+e.getKey()+" logPrior:"+e.getValue());
			// }

		} else {
			System.out.println("categoryPriors != null");
			// if they are provided then use the given priors
			knowledgeBase.c = categoryPriors.size();// number of category

			// make sure that the given priors are valid
			if (knowledgeBase.c != featureStats.categoryCounts.size()) {
				throw new IllegalArgumentException(
						"Invalid priors Array: Make sure you pass a prior probability for every supported category.");
			}

			String category;
			Double priorProbability;
			for (Map.Entry<String, Double> entry : categoryPriors.entrySet()) {
				category = entry.getKey();
				priorProbability = entry.getValue();
				if (priorProbability == null) {
					throw new IllegalArgumentException(
							"Invalid priors Array: Make sure you pass a prior probability for every supported category.");
				} else if (priorProbability < 0 || priorProbability > 1) {
					throw new IllegalArgumentException(
							"Invalid priors Array: Prior probabilities should be between 0 and 1.");
				}

				knowledgeBase.logPriors.put(category,
						Math.log(priorProbability));
			}

		}// end if categoryPriors == null

		// We are performing laplace smoothing (also known as add-1). This
		// requires to estimate the total feature occurrences in each category
		Map<String, Double> featureOccurrencesInCategory = new HashMap<>();

		Integer occurrences;
		Double featureOccSum;
		for (String category : knowledgeBase.logPriors.keySet()) {
			featureOccSum = 0.0;
			for (Map<String, Integer> categoryListOccurrences : featureStats.featureCategoryJointCount
					.values()) {
				occurrences = categoryListOccurrences.get(category);// get
																	// category
																	// occurrences
																	// numbers
				if (occurrences != null) {
					featureOccSum += occurrences;
				}
			}
			featureOccurrencesInCategory.put(category, featureOccSum);
		}

		// estimate log likelihoods
		String feature;
		Integer count;
		Map<String, Integer> featureCategoryCounts;
		double logLikelihood;
		// do every class in logPriors
		for (String category : knowledgeBase.logPriors.keySet()) {
			for (Map.Entry<String, Map<String, Integer>> entry : featureStats.featureCategoryJointCount
					.entrySet()) {
				feature = entry.getKey();// get feature term
				featureCategoryCounts = entry.getValue();// get Map<String,
															// Integer>

				count = featureCategoryCounts.get(category);// get number of
															// occurrences of
															// term in doc from
															// class c
				if (count == null) {
					count = 0;
				}
				// log P(X=t|c)=log(T/
				logLikelihood = Math
						.log((count + 1.0)
								/ (featureOccurrencesInCategory.get(category) + knowledgeBase.d));
				//System.out.println(knowledgeBase.d);
				if (knowledgeBase.logLikelihoods.containsKey(feature) == false) {
					knowledgeBase.logLikelihoods.put(feature,
							new HashMap<String, Double>());
				}
				knowledgeBase.logLikelihoods.get(feature).put(category,
						logLikelihood);// put logLikelihood

			}// end for

		}// end for

		featureOccurrencesInCategory = null;
	}

	/**
	 * Predicts the category of a text by using an already trained classifier
	 * and returns its category.
	 * 
	 * @param text
	 * @return predicted class
	 * @throws IllegalArgumentException
	 */
	public String predict(String text) throws IllegalArgumentException {
		if (knowledgeBase == null) {
			throw new IllegalArgumentException(
					"Knowledge Bases missing: Make sure you train first a classifier before you use it.");
		}

		// Tokenizes the text and creates a new document
		Document doc = TextTokenizer.tokenize(text);

		String category;
		String feature;
		Integer occurrences;
		Double logprob;

		String maxScoreCategory = null;
		Double maxScore = Double.NEGATIVE_INFINITY;// first set to infinity
													// negative

		for (Map.Entry<String, Double> entry1 : knowledgeBase.logPriors
				.entrySet()) {
			category = entry1.getKey();// get category
			logprob = entry1.getValue(); // get the initial scores with the
											// priors

			// foreach feature of the testing document
			for (Map.Entry<String, Integer> entry2 : doc.tokens.entrySet()) {
				feature = entry2.getKey();// get feature

				if (!knowledgeBase.logLikelihoods.containsKey(feature)) {
					continue; // if the feature does not exist in the knowledge
								// base skip it
				}
				occurrences = entry2.getValue(); // get testing feature's
													// occurrences in text
				logprob += occurrences
						* knowledgeBase.logLikelihoods.get(feature).get(
								category); // multiply loglikelihood score with
											// occurrences
			}
			// predictionScores.put(category, logprob);
			if (logprob > maxScore) {
				maxScore = logprob;
				maxScoreCategory = category;
			}
		}
		return maxScoreCategory; // return the category with highest score
	}
}
