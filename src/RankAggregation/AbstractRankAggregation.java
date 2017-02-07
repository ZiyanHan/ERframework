/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package RankAggregation;

import DataModel.AbstractBlock;
import DataModel.Comparison;
import DataModel.SimilarityPairs;
import Utilities.Comparators.ReverseComparisonWeightComparator;
import java.util.Arrays;
import java.util.List;
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
        initializeQueues(in1, in2);
    }
    
    /**
     * Alternative constructor, to get the input queues from two input blocking collections.
     * @param blocking1
     * @param blocking2 
     */
    public AbstractRankAggregation(List<AbstractBlock> blocking1, List<AbstractBlock> blocking2) {
        System.out.println("Running rank aggregation...");        
        Comparison[] comparisonsFromValues = getSortedComparisonsFromBlocks(blocking1);
        Comparison[] comparisonsFromNeighbors = getSortedComparisonsFromBlocks(blocking2);        
        
        initializeQueues(comparisonsFromValues, comparisonsFromNeighbors);
    }
    
    private Comparison[] getSortedComparisonsFromBlocks(List<AbstractBlock> blocks) {
        TreeSet<Comparison> valueQ = new TreeSet<>(new ReverseComparisonWeightComparator());           
        blocks.stream().forEach((block) -> valueQ.addAll(block.getComparisons()));
        
        //streaming verion (less memory required, as the comparisons are not kept in memory)
        /* 
        blocks.stream()
                .map((block) -> block.getComparisonIterator()) //for (block: blocks) iterator = block.getComparisonIterator();
                .forEach((iterator) -> {           //for every block's iterator
                    while (iterator.hasNext()) {                        
                        valueQ.add(iterator.next()); //add the next Comparison to valueQ
                    }
                });
        */
        
        Comparison[] valuesArray = new Comparison[valueQ.size()];        
        valuesArray = valueQ.toArray(valuesArray);        
        return valuesArray;
    }
    
    private void initializeQueues(Comparison[] in1, Comparison[] in2) {
        outQ = new TreeSet<>(new ReverseComparisonWeightComparator());
        
        if (in1 == null && in2 == null) {
            outQ = null; 
            out = new SimilarityPairs(true, 0); //return an emtpy list of pairs
        } else if (in1 == null) {
            addComparisonsToQueue(in2); //just copy the non-null input comparisons to the output
            queueToSimilarityPairs(); //the process ends here
        } else if (in2 == null) {
            addComparisonsToQueue(in1); //just copy the non-null input comparisons to the output
            queueToSimilarityPairs(); //the process ends here
        } else {
            //keep the biggest list in inQ1 and the smallest in inQ2
            if (in1.length >= in2.length) {
                this.inQ1 = in1;
                this.inQ2 = in2;
            } else {
                this.inQ1 = in2;
                this.inQ2 = in1;
            }
        }
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
    
    private void addComparisonsToQueue(Comparison[] comparisons) {
        outQ.addAll(Arrays.asList(comparisons));
    }
    
    public Comparison[] getInputQueue1() {
        return inQ1;
    }
    
    public Comparison[] getInputQueue2() {
        return inQ2;
    }
    
}
