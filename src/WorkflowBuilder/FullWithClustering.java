/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package WorkflowBuilder;

import BlockProcessing.IBlockProcessing;
import DataModel.SimilarityPairs;
import EntityClustering.IEntityClustering;
import EntityMatching.IEntityMatching;
import Utilities.Enumerations.BlockBuildingMethod;

/**
 *
 * @author VASILIS
 */
public class FullWithClustering extends AbstractWorkflowBuilder {

    public FullWithClustering(String dataset1Path, String dataset2Path, String groundTruthPath, BlockBuildingMethod blockingMethod, IBlockProcessing metaBlockingMethod, IEntityMatching similarityMethod, IEntityClustering clusteringMethod) {
        super(dataset1Path, dataset2Path, groundTruthPath, blockingMethod, metaBlockingMethod, similarityMethod, clusteringMethod);
    }        
    
    @Override
    public void runWorkflow() {
        loadData();
        runBlocking();
        runMetaBlocking();
        getBlockPerformance();
        SimilarityPairs simPairs = runSimilarityComputations();
        runClustering(simPairs);
    }
    
    @Override
    public String toString() {
        return "Workflow: FullWithClustering\n"
                +super.toString();
    }
    
}
