/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package WorkflowBuilder;

import BlockProcessing.BlockRefinement.ComparisonsBasedBlockPurging;
import BlockProcessing.IBlockProcessing;
import DataModel.SimilarityPairs;
import EntityClustering.IEntityClustering;
import EntityClustering.UniqueMappingClustering;
import EntityMatching.IEntityMatching;
import Utilities.Enumerations.BlockBuildingMethod;

/**
 *
 * @author VASILIS
 */
public class Baseline extends AbstractWorkflowBuilder {

    public Baseline(String dataset1Path, String dataset2Path, String groundTruthPath, IEntityMatching similarityMethod) {
        super(dataset1Path, dataset2Path, groundTruthPath, BlockBuildingMethod.STANDARD_BLOCKING, null, similarityMethod, new UniqueMappingClustering());        
    }        
    
    @Override
    public void runWorkflow() {
        loadData();
        runBlocking();
        SimilarityPairs simPairs = runSimilarityComputations();
        runClustering(simPairs);
    }
    
    
    public void runBlockPurging() {
        
    }
    
    @Override
    public String toString() {
        return "Workflow: Baseline\n"
                +super.toString();
    }
    
}
