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
import java.util.HashMap;
import java.util.Map.Entry;

/**
 *
 * @author G.A.P. II
 */

public class CharacterNGrams extends AbstractModel {

    private final HashMap<String, Integer> nGrams;
    
    public CharacterNGrams(int n, RepresentationModel model, String iName) {
        super(n, model, iName);
        nGrams = new HashMap<String, Integer>();
    }

    private HashMap<String, Integer> getNGrams() {
        return nGrams;
    }
    
    @Override
    public double getSimilarity(AbstractModel oModel) {//Jaccard similarity
        final CharacterNGrams otherModel = (CharacterNGrams) oModel;
        
        double numerator = 0.0;
        for (Entry<String, Integer> entry : nGrams.entrySet()) {
            Integer frequency2 = otherModel.getNGrams().get(entry.getKey());
            if (frequency2 != null) {
                numerator += Math.min(entry.getValue(), frequency2);
            }
        }

        double denominator = getTotalFrequency(this.getNGrams())+getTotalFrequency(otherModel.getNGrams())-numerator;
        return numerator/denominator;
    }
    
    private double getTotalFrequency(HashMap<String, Integer> nGramsFrequency) {
        double totalFrequency = 0;
        for (Integer frequency : nGramsFrequency.values()) {
            totalFrequency += frequency;
        }
        return totalFrequency;
    }
    
    @Override
    public void updateModel(String text) {
        int currentPosition = 0;
        final int length = text.length() - (nSize-1);
        while (currentPosition < length) {
            final String term = text.substring(currentPosition, currentPosition + nSize);
            Integer frequency = nGrams.get(term);
            if (frequency == null) {
                frequency = new Integer(0);
            } 
            frequency++;
            nGrams.put(term, frequency);
            currentPosition++;
        }
    }
}