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
import java.util.HashSet;
import java.util.Set;

/**
 *
 * @author G.A.P. II
 */

public class TokenNGrams extends BagModel {
    
    public TokenNGrams(int n, RepresentationModel model, SimilarityMetric simMetric, String iName) {
        super(n, model, simMetric, iName);
    }
    
    @Override
    public void updateModel(String text) {
        noOfDocuments++;
             
        String[] tokens = gr.demokritos.iit.jinsect.utils.splitToWords(text);
        
        int noOfTokens = tokens.length;
        noOfTotalTerms += noOfTokens;
        for (int j = 0; j <= noOfTokens-nSize; j++) { //this missed the last token without <=
            final StringBuilder sb = new StringBuilder();
            for (int k = 0; k < nSize; k++) {
                sb.append(tokens[j+k]).append(" ");
            }
            String feature = sb.toString().trim();
            
            Integer frequency = itemsFrequency.get(feature);
            if (frequency == null) {
                frequency = new Integer(0);
            }
            frequency++;
            itemsFrequency.put(feature, frequency);
        }
    }
}