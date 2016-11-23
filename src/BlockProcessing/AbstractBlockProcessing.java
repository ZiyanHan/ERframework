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

package BlockProcessing;

import Utilities.DataStructures.AbstractDuplicatePropagation;
import BlockBuilding.AbstractBlockBuilding;
import DataModel.AbstractBlock;
import DataModel.Comparison;
import DataModel.ComparisonIterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author G.A.P. II
 */

public abstract class AbstractBlockProcessing implements IBlockProcessing {
    
    private static final Logger LOGGER = Logger.getLogger(AbstractBlockBuilding.class.getName());
            
    @Override
    public void deduplicateBlocks(AbstractDuplicatePropagation adp, List<AbstractBlock> inputBlocks) {
        printOriginalStatistics(inputBlocks);
        
        List<AbstractBlock> newBlocks = refineBlocks(inputBlocks);
        
        double totalComparisons = 0;
        for (AbstractBlock aBlock : newBlocks) {
            totalComparisons += processBlock(aBlock, adp);
        }
        
        LOGGER.log(Level.INFO, "Detected duplicates\t:\t{0}", adp.getNoOfDuplicates());
        LOGGER.log(Level.INFO, "Executed comparisons\t:\t{0}", totalComparisons);
    }
    
    protected double processBlock(AbstractBlock aBlock, AbstractDuplicatePropagation adp) {
        double noOfComparisons = 0;
        
        ComparisonIterator iterator = aBlock.getComparisonIterator();
        while (iterator.hasNext()) {
            Comparison comparison = iterator.next();
            if (!adp.isSuperfluous(comparison)) {
                noOfComparisons++;
            }
        }
        
        return noOfComparisons;
    }
    
    protected void printOriginalStatistics(List<AbstractBlock> inputBlocks) {
        double comparisons = 0;
        for (AbstractBlock aBlock : inputBlocks) {
            comparisons += aBlock.getNoOfComparisons();
        }
        LOGGER.log(Level.INFO, "Original blocks\t:\t{0}", inputBlocks.size());
        LOGGER.log(Level.INFO, "Original comparisons\t:\t{0}", comparisons);
    }
}
