/*
* Copyright [2016] [George Papadakis (gpapadis@yahoo.gr)]
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
 */
package Utilities.TextModels;

import Utilities.Enumerations.RepresentationModel;
import Utilities.Enumerations.SimilarityMetric;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author G.A.P. II
 */
public abstract class BagModel extends AbstractModel {
    
    private static final Logger LOGGER = Logger.getLogger(BagModel.class.getName());

    protected double noOfTotalTerms;
    protected final Map<String, Integer> itemsFrequency;

    public BagModel(int n, RepresentationModel md, SimilarityMetric sMetric, String iName) {
        super(n, md, sMetric, iName);

        itemsFrequency = new HashMap<String, Integer>();
    }
    
    public double getCosineSimilarity(BagModel oModel) {
        Map<String, Integer> itemVector1 = itemsFrequency;
        Map<String, Integer> itemVector2 = oModel.getItemsFrequency();
        if (itemVector2.size() < itemVector1.size()) {
            itemVector1 = oModel.getItemsFrequency();
            itemVector2 = itemsFrequency;
        }

        double numerator = 0.0;
        for (Entry<String, Integer> entry : itemVector1.entrySet()) {
            Integer frequency2 = itemVector2.get(entry.getKey());
            if (frequency2 != null) {
                numerator += entry.getValue() * frequency2 / noOfTotalTerms / oModel.getNoOfTotalTerms();
            }
        }
        
        double denominator = getVectorMagnitude(this)*getVectorMagnitude(oModel); 
        return numerator / denominator;
    }

    private double getEnhancedJaccardSimilarity(BagModel oModel) {
        Map<String, Integer> itemVector1 = itemsFrequency;
        Map<String, Integer> itemVector2 = oModel.getItemsFrequency();
        if (itemVector2.size() < itemVector1.size()) {
            itemVector1 = oModel.getItemsFrequency();
            itemVector2 = itemsFrequency;
        }

        double numerator = 0.0;
        for (Entry<String, Integer> entry : itemVector1.entrySet()) {
            Integer frequency2 = itemVector2.get(entry.getKey());
            if (frequency2 != null) {
                numerator += Math.min(entry.getValue(), frequency2);
            }
        }

        double denominator = noOfTotalTerms + oModel.getNoOfTotalTerms() - numerator;
        return numerator / denominator;
    }
    
    private double getGeneralizedJaccardSimilarity(BagModel oModel) {
        double totalTerms1 = noOfTotalTerms;
        double totalTerms2 = oModel.getNoOfTotalTerms();
        Map<String, Integer> itemVector1 = itemsFrequency;
        Map<String, Integer> itemVector2 = oModel.getItemsFrequency();
        if (itemVector2.size() < itemVector1.size()) {
            itemVector1 = oModel.getItemsFrequency();
            itemVector2 = itemsFrequency;

            totalTerms1 = oModel.getNoOfTotalTerms();
            totalTerms2 = noOfTotalTerms;
        }

        double numerator = 0.0;
        for (Entry<String, Integer> entry : itemVector1.entrySet()) {
            Integer frequency2 = itemVector2.get(entry.getKey());
            if (frequency2 != null) {
                numerator += Math.min(entry.getValue() / totalTerms1, frequency2 / totalTerms2);
            }
        }

        final Set<String> allKeys = new HashSet<String>(itemVector1.keySet());
        allKeys.addAll(itemVector2.keySet());
        double denominator = 0.0;
        for (String key : allKeys) {
            Integer frequency1 = itemVector1.get(key);
            Integer frequency2 = itemVector2.get(key);
            double freq1 = frequency1 == null ? 0 : frequency1 / totalTerms1;
            double freq2 = frequency2 == null ? 0 : frequency2 / totalTerms2;
            denominator += Math.max(freq1, freq2);
        }

        return numerator / denominator;
    }
    
    
    /**
     * vefthym
     * Returns the weighted Jaccard similarity of two entities:
     * = (sum_{t common} w1(t) + w2(t)) / (sum_{t in E1} w1(t) + sum_{t in E2} w2(t) ), where  
     * weight of token t in entity collection E w(t) = log_10 (|E|/|E_t|), 
     * where |E_t| is the number of entities in E that contain the token t
     * @param oModel represents a whole entity collection (not a single entity)
     * @return 
     */
    public double getWeightedJaccardSimilarity(Set<String> tokens1, Set<String> tokens2, BagModel oModel) {
        Map<String, Integer> itemVector1 = itemsFrequency;
        Map<String, Integer> itemVector2 = oModel.getItemsFrequency();
        double noOfDocuments1 = noOfDocuments;        
        double noOfDocuments2 = oModel.getNoOfDocuments(); //this should be the number of entities (check when updateModel is called)
        
        if (itemVector2.size() < itemVector1.size()) {
            itemVector1 = oModel.getItemsFrequency();
            itemVector2 = itemsFrequency;
            noOfDocuments1 = noOfDocuments2;
            noOfDocuments2 = noOfDocuments;
            Set<String> tmp = new HashSet<>(tokens1);            
            tokens1 = tokens2;
            tokens2 = tmp;
        }
                
        double numerator = 0.0;
        double denominator = Double.MIN_NORMAL; //the smallest positive constant, to avoid 0/0
        for (String key1 : tokens1) {
//            System.out.print("frequency of "+key1);
            Integer frequency1 = itemVector1.get(key1);
//            System.out.println(": "+frequency1);
            Integer frequency2 = itemVector2.get(key1);            
            double weight1 = Math.log10(noOfDocuments1/frequency1);            
//            System.out.println("weight1("+key1+")="+weight1);
            if (frequency2 != null) {                
                double weight2 = Math.log10(noOfDocuments2/frequency2);
//                System.out.println("weight2("+key1+")="+weight2);
                numerator += weight1 + weight2;
            } else {
                denominator += weight1; //this is the case that a word belongs to E1 only
            }
        }        
        
        denominator += numerator; //weight of common words + weight of words belonging only to E1
        //now, we need to cover the words that belong only to E2 for the denominator
        for (String key2 : tokens2) {
            if (!itemVector1.containsKey(key2)) {
                Integer frequency2 = itemVector2.get(key2);
                double weight2 = Math.log10(noOfDocuments2/frequency2);
//                System.out.println("weight2("+key2+")="+weight2);
                denominator += weight2;
            }
        }        
        
        return Math.min(numerator / denominator, 1);
    }    
    
    
    /**
     * vefthym
     * Returns the weighted ARCS similarity of two entities.     
     * @param tokens1
     * @param tokens2
     * @param oModel represents a whole entity collection (not a single entity)
     * @return 
     */
    public double getARCSSimilarity(Set<String> tokens1, Set<String> tokens2, BagModel oModel) {
        Map<String, Integer> itemVector1 = itemsFrequency;
        Map<String, Integer> itemVector2 = oModel.getItemsFrequency();        
        
        if (itemVector2.size() < itemVector1.size()) {
            itemVector1 = oModel.getItemsFrequency();
            itemVector2 = itemsFrequency;            
            Set<String> tmp = new HashSet<>(tokens1);            
            tokens1 = tokens2;
        }
                
        double similarity = 0;        
        for (String key1 : tokens1) {
            long frequency1 = itemVector1.getOrDefault(key1, 0);
            long frequency2 = itemVector2.getOrDefault(key1, 0);
            if (frequency2 != 0) {                                       
                similarity += 1.0 / (Math.log1p(frequency1*frequency2) / Math.log(2));                
            } 
        }            
        
        return similarity;
    }   
    
    
    /**
     * vefthym
     * Returns the weighted ARCS similarity of two entities.     
     * @param tokens1
     * @param tokens2
     * @param oModel represents a whole entity collection (not a single entity)
     * @return 
     */
    public double getWeightedARCSSimilarity(Set<String> tokens1, Set<String> tokens2, BagModel oModel) {
        Map<String, Integer> itemVector1 = itemsFrequency;
        Map<String, Integer> itemVector2 = oModel.getItemsFrequency();        
        
        if (itemVector2.size() < itemVector1.size()) {
            itemVector1 = oModel.getItemsFrequency();
            itemVector2 = itemsFrequency;                                
            tokens1 = tokens2;
        }
                
        double numerator = 0;
        double denominator = 0; 
        for (String key1 : tokens1) {
            long frequency1 = itemVector1.getOrDefault(key1, 0);
            long frequency2 = itemVector2.getOrDefault(key1, 0);
            if (frequency2 != 0) {                                       
                numerator += 1.0 / (Math.log1p(frequency1*frequency2) / Math.log(2));                                
            } else {
                //System.out.println("Word "+key1+" appears only in E1 and it adds "+1.0 / (Math.log1p(frequency1) / Math.log(2))+ " to the denom.");
                denominator += 1.0 / (Math.log1p(frequency1) / Math.log(2));
            }            
        }     
        /*
        for (String key2 : tokens2) {
            long frequency1 = itemVector1.getOrDefault(key2, 0);
            long frequency2 = itemVector2.getOrDefault(key2, 0);
            if (frequency1 == 0) {                                                       
                //System.out.println("Word "+key2+" appears only in E2 and it adds "+1.0 / (Math.log1p(frequency2) / Math.log(2))+ " to the denom.");
                denominator += 1.0 / (Math.log1p(frequency2) / Math.log(2));
            }            
        }    
        */
        denominator += numerator;
        
        return denominator == 0 ? 0 : Math.min(numerator / denominator, 1); //in case the precision of the result is not very good and it exceeds 1
    }    
    

    public Map<String, Integer> getItemsFrequency() {
        return itemsFrequency;
    }
    
    private double getJaccardSimilarity(BagModel oModel) {
        final Set<String> commonKeys = new HashSet<String>(itemsFrequency.keySet());
        commonKeys.retainAll(oModel.getItemsFrequency().keySet());

        double numerator = commonKeys.size();
        double denominator = itemsFrequency.size() + oModel.getItemsFrequency().size() - numerator;
        return numerator / denominator;
    }

    public double getNoOfTotalTerms() {
        return noOfTotalTerms;
    }
    
    @Override
    public double getSimilarity(AbstractModel oModel) {
        switch (simMetric) {
            case COSINE_SIMILARITY:
                return getCosineSimilarity((BagModel) oModel);
            case ENHANCED_JACCARD_SIMILARITY:
                return getEnhancedJaccardSimilarity((BagModel) oModel);
            case GENERALIZED_JACCARD_SIMILARITY:
                return getGeneralizedJaccardSimilarity((BagModel) oModel);
            case JACCARD_SIMILARITY:
                return getJaccardSimilarity((BagModel) oModel);
            default:
                LOGGER.log(Level.SEVERE, "The given similarity metric is incompatible with the bag representation model!");
                System.exit(-1);
                return -1;
        }
    }
    
    private double getVectorMagnitude(BagModel model) {
        double magnitude = 0.0;
        for (Entry<String, Integer> entry : model.getItemsFrequency().entrySet()) {
            magnitude += Math.pow(entry.getValue()/model.getNoOfTotalTerms(), 2.0);
        }

        return Math.sqrt(magnitude);
    }
}
