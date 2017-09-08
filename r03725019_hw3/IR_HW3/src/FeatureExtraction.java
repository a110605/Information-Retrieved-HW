import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FeatureExtraction {

	/**
	 * Generates a FeatureStats Object with metrics about the occurrences of the
	 * keywords in categories, the number of category counts and the total
	 * number of observations. These stats are used by the feature selection
	 * algorithm.
	 * 
	 * @param dataset
	 * @return
	 */
	public FeatureStats extractFeatureStats(List<Document> dataset) {
		FeatureStats stats = new FeatureStats();
		Integer categoryCount;
		String category;
		Integer featureCategoryCount;
		String feature;
		Map<String, Integer> featureCategoryCounts;// 該feature在此class出現次數

		// do something for every doc in dataset
		for (Document doc : dataset) {
			++stats.n; // increase the number of observed doc
			category = doc.category;// get this doc class

			// 計算屬於此class的docs數
			categoryCount = stats.categoryCounts.get(category);
			if (categoryCount == null) {
				stats.categoryCounts.put(category, 1);
			} else {
				stats.categoryCounts.put(category, categoryCount + 1);
			}

			// do something for every token in this doc
			for (Map.Entry<String, Integer> entry : doc.tokens.entrySet()) {
				// get token from doc
				feature = entry.getKey();
				// get the counts of the feature in the categories
				featureCategoryCounts = stats.featureCategoryJointCount
						.get(feature);

				if (featureCategoryCounts == null) {
					// initialize it if it does not exist
					stats.featureCategoryJointCount.put(feature,
							new HashMap<String, Integer>());
				}
				// feature在此class中出現次數
				featureCategoryCount = stats.featureCategoryJointCount.get(
						feature).get(category);
				if (featureCategoryCount == null) {
					featureCategoryCount = 0;
				}
				// increase the number of occurrences of the feature in the
				// category
				stats.featureCategoryJointCount.get(feature).put(category,
						++featureCategoryCount);
			}// end doc.tokens.entrySet()
		}// end Document doc : dataset

		return stats;
	}

	/**
	 * Perform feature selection by using the chisquare non-parametrical
	 * statistical test.
	 * 
	 * @param stats
	 * @param criticalLevel
	 * @return Map selectedFeatures
	 */
	public Map<String, Double> chisquare(FeatureStats stats,
			double criticalLevel) {
		// 用來存放選到<feature,此feature的chisquareScore值>
		Map<String, Double> selectedFeatures = new HashMap<>();

		String feature;
		String category;
		Map<String, Integer> categoryList;

		int N1dot, N0dot, N00, N01, N10, N11;
		double chisquareScore;// x^2
		Double previousScore;

		for (Map.Entry<String, Map<String, Integer>> entry1 : stats.featureCategoryJointCount
				.entrySet()) {
			feature = entry1.getKey();// get feature
			categoryList = entry1.getValue();// Map<class,屬於此class且此feature出現次數>>

			// calculate the N1. (number of documents that have the feature)
			N1dot = 0;
			for (Integer count : categoryList.values()) {
				N1dot += count;
			}

			// calculate the N0. (number of documents that DONT have the
			// feature)
			N0dot = stats.n - N1dot;

			for (Map.Entry<String, Integer> entry2 : categoryList.entrySet()) {
				category = entry2.getKey();// get class
				N11 = entry2.getValue(); // N11 is the number of documents that
											// have the feature and belong on
											// the specific category
				N01 = stats.categoryCounts.get(category) - N11; // N01 is the
																// total number
																// of documents
																// that do not
																// have the
																// particular
																// feature BUT
																// they belong
																// to the
																// specific
																// category

				N00 = N0dot - N01; // N00 counts the number of documents that
									// don't have the feature and don't belong
									// to the specific category
				N10 = N1dot - N11; // N10 counts the number of documents that
									// have the feature and don't belong to the
									// specific category

				// calculate the chisquare score based on the above statistics
				chisquareScore = stats.n
						* Math.pow(N11 * N00 - N10 * N01, 2)
						/ ((N11 + N01) * (N11 + N10) * (N10 + N00) * (N01 + N00));

				// if the score is larger than the critical value then add it in
				// the list

				if (chisquareScore >= criticalLevel) {
					previousScore = selectedFeatures.get(feature);
					if (previousScore == null || chisquareScore > previousScore) {
						//if (selectedFeatures.size() < 500)
						selectedFeatures.put(feature, chisquareScore);
					}

				}// end if

			}
		}
		return selectedFeatures;
	}
}