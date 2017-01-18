/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package RankAggregation;

import DataModel.Comparison;
import DataModel.SimilarityPairs;
import Utilities.Comparators.ComparisonWeightComparator;
import Utilities.Comparators.ReverseComparisonWeightComparator;
import java.util.TreeSet;

/**
 * Aggregates two given SimilarityPairs instances, into one PriorityQueue
 * The two similarityPairs should have the same number of comparisons. 
 * @author vefthym
 */
public abstract class AbstractRankAggregation {
    Comparison[] inQ1, inQ2;
    TreeSet<Comparison> outQ;
    SimilarityPairs out;
    
    /**
     * Store the input lists as two sorted array instances. 
     * Also, initialize the output as a sorted list, based on the aggregate score.
 Pre-condition: inQ1 and inQ2 must be sorted in descending order
 Post-condition: inQ2 stores the smallest list (for search purposes)
     * @param in1
     * @param in2 
     */
    public AbstractRankAggregation(Comparison[] in1, Comparison[] in2) {
        //keep the biggest list in inQ1 and the smallest in inQ2
        if (in1.length >= in2.length) {
            this.inQ1 = in1;
            this.inQ2 = in2;
        } else {
            this.inQ1 = in2;
            this.inQ2 = in1;
        }
        
        outQ = new TreeSet<>(new ReverseComparisonWeightComparator());
//                (c1, c2) -> -((Double)c1.getUtilityMeasure()).compareTo(c2.getUtilityMeasure())); //custom comparator
    }
    
    public abstract void runAggregation();
    
    public SimilarityPairs getAggregation() {
        if (out == null) {
            runAggregation();
            queueToSimilarityPairs();
        }
        return out;
    }
    
    private SimilarityPairs queueToSimilarityPairs() {
        out = new SimilarityPairs(true, outQ.size()); //TODO:check if clean-clean er is false
        outQ.stream().forEach(c -> out.addComparison(c));
        return out;
    }
    
}
