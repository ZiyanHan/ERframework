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
import BlockProcessing.IBlockProcessing;
import DataModel.AbstractBlock;
import Utilities.Comparators.BlockUtilityComparator;
import java.util.Collections;
import java.util.List;

/**
 *
 * @author gap2
 */

public class BlockScheduling extends AbstractBlockProcessing implements IBlockProcessing {
    
    public BlockScheduling() {
    }
    
    @Override
    public List<AbstractBlock> refineBlocks(List<AbstractBlock> blocks) {
        for (AbstractBlock block : blocks) {
            block.setUtilityMeasure();
        }
        Collections.sort(blocks, new BlockUtilityComparator());
        return blocks;
    }
    
    @Override
    public String getMethodInfo() {
        return "Block Scheduling: it determines the processing order of blocks "
                + "by sorting them in ascending order of block utility.";
    }

    @Override
    public String getMethodParameters() {
        return "Block Scheduling is a parameter-free appoach, "
                + "as the utility of blocks is set automatically.";
    }
} 
