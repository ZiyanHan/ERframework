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
