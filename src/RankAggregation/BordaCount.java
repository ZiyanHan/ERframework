/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package RankAggregation;

import DataModel.Comparison;

/**
 *
 * @author vefthym
 */
public class BordaCount extends AbstractRankAggregation {

    public BordaCount(Comparison[] in1, Comparison[] in2) {
        super(in1,in2);
    }

    @Override
    public void runAggregation() {
        int rank1 = inQ1.length;
        
        //add to outQ each comparison that exists in both queues, or only in inQ1
        for (Comparison q1Comparison : inQ1) {
            Comparison newComparison = new Comparison(q1Comparison.isCleanCleanER(),q1Comparison.getEntityId1(), q1Comparison.getEntityId2());            
            int rank2 = searchComparisonInArray(inQ2, q1Comparison, inQ1.length);
            //System.out.println("The new rank of comparison "+q1Comparison.getEntityId1()+","+q1Comparison.getEntityId2()+" is "+rank1+"+"+rank2+"="+(rank1+rank2));
            newComparison.setUtilityMeasure(rank1+rank2); //if it does not exist in inQ2, rank2 is the the lowest possible rank of inQ2
            outQ.add(newComparison); 
            rank1--;
        }
        
        //now add to outQ any comparisons that exist only in inQ2
        int rank2 = inQ1.length;
        for (Comparison q2Comparison : inQ2) {
            if (!outQ.contains(q2Comparison)) {
                Comparison newComparison = new Comparison(q2Comparison.isCleanCleanER(),q2Comparison.getEntityId1(), q2Comparison.getEntityId2());
                newComparison.setUtilityMeasure(rank2);
                outQ.add(newComparison);
                //System.out.println("Adding the comparison "+q2Comparison.getEntityId1()+","+q2Comparison.getEntityId2()+" only from q2 at rank:"+rank2);
            }
            rank2--;
        }
        
        /*
        System.out.println("\n\nThe output priority queue is:");
        for (Comparison comp : outQ) {
            System.out.println(comp.getEntityId1()+","+comp.getEntityId2()+": "+comp.getUtilityMeasure());
        }
        */
    }
    
    /**
     * Searches the cArray for the comparison, and returns its rank. 
     * The rank of an element at index i is defined as rank(i) = cArray.length - i.
     * If the element is not found, then the returned value is the lowest rank found in inQ2. 
     * @param cArray
     * @param comparison
     * @return 
     */
    private int searchComparisonInArray(Comparison[] cArray, Comparison comparison, int topRank) {        
        for (Comparison q2Comparison : cArray) {
            if (comparison.equals(q2Comparison)) {                    
                break;
            }
            topRank--;
        }
        return topRank;
    }
    
}
