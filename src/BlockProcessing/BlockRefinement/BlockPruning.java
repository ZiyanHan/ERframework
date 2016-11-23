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

package BlockProcessing.BlockRefinement;

import BlockProcessing.AbstractBlockProcessing;
import Utilities.DataStructures.AbstractDuplicatePropagation;
import BlockProcessing.IBlockProcessing;
import DataModel.AbstractBlock;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author gap2
 */

public class BlockPruning extends AbstractBlockProcessing implements IBlockProcessing {

    private static final Logger LOGGER = Logger.getLogger(BlockPruning.class.getName());
    
    public BlockPruning() {
    }

    @Override
    public void deduplicateBlocks(AbstractDuplicatePropagation adp, List<AbstractBlock> blocks) {
        printOriginalStatistics(blocks);
        
        int latestDuplicates = 0;
        double latestComparisons = 0;
        double totalComparisons = 0;
        double maxDuplicateOverhead = getMaxDuplicateOverhead(blocks);
        LOGGER.log(Level.INFO, "Maximum duplicate overhead\t:\t{0}", maxDuplicateOverhead);
        
        for (AbstractBlock aBlock : blocks) {
            latestComparisons += processBlock(aBlock, adp);
            
            int currentDuplicates = adp.getNoOfDuplicates();
            if (currentDuplicates == latestDuplicates) {
                continue;
            }

            int noOfNewDuplicates = currentDuplicates - latestDuplicates;
            double duplicateOverhead = latestComparisons / noOfNewDuplicates;
            if (maxDuplicateOverhead < duplicateOverhead) {
                totalComparisons += latestComparisons;
                break;
            }

            totalComparisons += latestComparisons;
            latestComparisons = 0;
            latestDuplicates = adp.getNoOfDuplicates();
        }

        LOGGER.log(Level.INFO, "Detected duplicates\t:\t{0}", adp.getNoOfDuplicates());
        LOGGER.log(Level.INFO, "Executed comparisons\t:\t{0}", totalComparisons);
    }
    
    private double getMaxDuplicateOverhead(List<AbstractBlock> blocks) {
        double totalComparisons = 0;
        for (AbstractBlock block : blocks) {
            totalComparisons += block.getNoOfComparisons();
        }
        return Math.pow(10, Math.log10(totalComparisons) / 2.0);
    }

    @Override
    public String getMethodInfo() {
        return "Block Pruning: it terminates entity matching when the cost of "
                + "detecting new duplicates exceeds a certain limit, "
                + "called maximum duplicate overhead.";
    }

    @Override
    public String getMethodParameters() {
        return "Block Pruning is a parameter-free approach. "
                + "The maximum duplicate overhead is set automatically.";
    }
    
    @Override
    public List<AbstractBlock> refineBlocks(List<AbstractBlock> blocks) {
        throw new UnsupportedOperationException("Not supported by this method. It is applied only in combination with Entity Matching.");
    }    
}