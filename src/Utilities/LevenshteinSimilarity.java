/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Utilities;

/**
 *
 * @author vefthym
 */
public class LevenshteinSimilarity {
    
    String s1, s2;

    public LevenshteinSimilarity(String s1, String s2) {
        this.s1 = s1;
        this.s2 = s2;
    }    
    
    /**
     * Returns a normalized Levenshtein similarity (1-levenshtein distance)
     * @return 
     */
    public double getLevenshteinSimilarity() {
        if (s1 == null || s2 == null) {
            return 0;
        }
        return 1 - levenshteinDistance(s1, s2) / (double)Math.max(s1.length(), s2.length());
    }
    
    /**
     * Copied from https://en.wikibooks.org/wiki/Algorithm_Implementation/Strings/Levenshtein_distance#Java
     * @param lhs
     * @param rhs
     * @return 
     */
    private int levenshteinDistance (CharSequence lhs, CharSequence rhs) {                          
        int len0 = lhs.length() + 1;                                                     
        int len1 = rhs.length() + 1;                                                     

        // the array of distances                                                       
        int[] cost = new int[len0];                                                     
        int[] newcost = new int[len0];                                                  

        // initial cost of skipping prefix in String s0                                 
        for (int i = 0; i < len0; i++) cost[i] = i;                                     

        // dynamically computing the array of distances                                  

        // transformation cost for each letter in s1                                    
        for (int j = 1; j < len1; j++) {                                                
            // initial cost of skipping prefix in String s1                             
            newcost[0] = j;                                                             

            // transformation cost for each letter in s0                                
            for(int i = 1; i < len0; i++) {                                             
                // matching current letters in both strings                             
                int match = (lhs.charAt(i - 1) == rhs.charAt(j - 1)) ? 0 : 1;             

                // computing cost for each transformation                               
                int cost_replace = cost[i - 1] + match;                                 
                int cost_insert  = cost[i] + 1;                                         
                int cost_delete  = newcost[i - 1] + 1;                                  

                // keep minimum cost                                                    
                newcost[i] = Math.min(Math.min(cost_insert, cost_delete), cost_replace);
            }                                                                           

            // swap cost/newcost arrays                                                 
            int[] swap = cost; cost = newcost; newcost = swap;                          
        }                                                                               

        // the distance is the cost for transforming all letters in both strings        
        return cost[len0 - 1];                                                          
    }
    
    public static void main (String[] args) {
        String s1 = "Adobe  5";        
        String s2 = "Adobe  4";
        
        LevenshteinSimilarity test = new LevenshteinSimilarity(s1,s2);
        System.out.println(test.getLevenshteinSimilarity());
    }
    
}
