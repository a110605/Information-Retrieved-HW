import java.util.HashMap;
import java.util.Map;

public class FeatureStats {
    /**
     * total number of Observed doc
     */
    public int n;
    
    /**
     * 
     * It stores the co-occurrences of Feature and Category values
     * <feature,Map<class,屬於此class且此feature出現次數>>
     */
    public Map<String, Map<String, Integer>> featureCategoryJointCount;
    
    /**
     * Measures how many times each category was found in the training dataset.
     * <class,屬於此Class的doc數量> 
     */
    public Map<String, Integer> categoryCounts; 

    /**
     * Constructor
     */
    public FeatureStats() {
        n = 0;
        featureCategoryJointCount = new HashMap<>();
        categoryCounts = new HashMap<>();
    }
}
