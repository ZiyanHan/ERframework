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

import Utilities.DataStructures.AbstractDuplicatePropagation;
import DataModel.AbstractBlock;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author gap2
 */
public class ComparisonPropagation extends AbstractComparisonRefinementMethod {

    public ComparisonPropagation() {
        super();
    }

    @Override
    protected List<AbstractBlock> applyMainProcessing() {
        List<AbstractBlock> newBlocks = new ArrayList<>();
        if (cleanCleanER) {
            processBilateralBlocks(newBlocks);
        } else {
            processUnilateralBlocks(newBlocks);
        }
        return newBlocks;
    }

    @Override
    public void deduplicateBlocks(AbstractDuplicatePropagation adp, List<AbstractBlock> blocks) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public String getMethodInfo() {
        return "Comparison Propagation: it eliminates all redundant comparisons from a set of overlapping blocks.";
    }

    @Override
    public String getMethodParameters() {
        return "Comparisons Propagation is parameter-free approach.";
    }

    private void processBilateralBlocks(List<AbstractBlock> newBlocks) {
        for (int i = 0; i < datasetLimit; i++) {
            final int[] associatedBlocks = entityIndex.getEntityBlocks(i, 0);
            if (associatedBlocks.length != 0) {
                validEntities.clear();
                for (int blockIndex : associatedBlocks) {
                    for (int neighborId : bBlocks[blockIndex].getIndex2Entities()) {
                        validEntities.add(neighborId);
                    }
                }
                addDecomposedBlock(i, validEntities, newBlocks);
            }
        }
    }

    private void processUnilateralBlocks(List<AbstractBlock> newBlocks) {
        for (int i = 0; i < noOfEntities; i++) {
            final int[] associatedBlocks = entityIndex.getEntityBlocks(i, 0);
            if (associatedBlocks.length != 0) {
                validEntities.clear();
                for (int blockIndex : associatedBlocks) {
                    for (int neighborId : uBlocks[blockIndex].getEntities()) {
                        if (neighborId < i) {
                            validEntities.add(neighborId);
                        }
                    }
                }
                addDecomposedBlock(i, validEntities, newBlocks);
            }
        }
    }
}
