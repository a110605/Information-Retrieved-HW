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
     * <feature,Map<class,�ݩ�class�B��feature�X�{����>>
     */
    public Map<String, Map<String, Integer>> featureCategoryJointCount;
    
    /**
     * Measures how many times each category was found in the training dataset.
     * <class,�ݩ�Class��doc�ƶq> 
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
