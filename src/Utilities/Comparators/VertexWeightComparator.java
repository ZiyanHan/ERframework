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

package Utilities.Comparators;

import DataModel.SimilarityEdge;
import DataModel.VertexWeight;

import java.util.Comparator;

/**
 *
 * @author G.A.P. II
 */

public class VertexWeightComparator implements Comparator<VertexWeight> {

    @Override
    public int compare(VertexWeight vw1,VertexWeight vw2) {
    	double w1 = vw1.getWeight()/vw1.getNoOfAdj();
    	double w2 = vw2.getWeight()/vw2.getNoOfAdj();
    	double test = w1-w2 +  0.00001 * (double) vw1.Connections().size() - 0.00001 * (double) vw2.Connections().size();
        if (test > 0) {
            return -1;
        }

        if (test < 0) {
            return 1;
        }
        return 0;
    }
    
}