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

package BlockProcessing.ComparisonRefinement;

import Utilities.Enumerations.WeightingScheme;

/**
 *
 * @author G.A.P. II
 */
public class ReciprocalWeightedNodePruning extends WeightedNodePruning {

    public ReciprocalWeightedNodePruning(WeightingScheme scheme) {
        super(scheme);
    }

    @Override
    public String getMethodInfo() {
        return "Reciprocal Weighted Node Pruning: a Meta-blocking method that retains the comparisons "
                + "that correspond to edges in the blocking graph that are exceed the average edge weight "
                + "in both adjacent node neighborhoods.";
    }

    @Override
    public String getMethodParameters() {
        return "Reciprocal Weighted Node Pruning involves a single parameter:\n"
                + "the weighting scheme that assigns weights to the edges of the blcoking graph.";
    }
    
    @Override
    protected boolean isValidComparison(int entityId, int neighborId) {
        double weight = getWeight(entityId, neighborId);
        boolean inNeighborhood1 = averageWeight[entityId] <= weight;
        boolean inNeighborhood2 = averageWeight[neighborId] <= weight;
        
        if (inNeighborhood1 && inNeighborhood2) {
            return entityId < neighborId;
        }
        
        return false;
    }
}
