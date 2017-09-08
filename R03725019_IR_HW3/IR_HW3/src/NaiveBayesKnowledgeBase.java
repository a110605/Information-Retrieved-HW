import java.util.HashMap;
import java.util.Map;

public class NaiveBayesKnowledgeBase {
    /**
     * number of training observed doc
     */
    public int n=0;
    
    /**
     * number of categories
     */
    public int c=0;
    
    /**
     * number of all features
     */
    public int d=0;
    
    /**
     * log priors for log( P(c) )
     */
    public Map<String, Double> logPriors = new HashMap<>();//<class,class's logPrior>
    
    /**
     * log likelihood for log( P(x|c) ) 
     */
    public Map<String, Map<String, Double>> logLikelihoods = new HashMap<>();//<feature,Map<class,logLikelihood>>
}
