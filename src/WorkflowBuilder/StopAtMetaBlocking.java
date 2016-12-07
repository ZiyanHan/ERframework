/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package WorkflowBuilder;

import EntityMatching.IEntityMatching;
import EntityMatching.ProfileWithNeighborMatcher;
import Utilities.Enumerations.BlockBuildingMethod;

/**
 *
 * @author VASILIS
 */
public class StopAtMetaBlocking extends AbstractWorkflowBuilder {

    public StopAtMetaBlocking(String dataset1Path, String dataset2Path, String groundTruthPath, BlockBuildingMethod blockingMethod, IEntityMatching similarityMethod) {
        super(dataset1Path, dataset2Path, groundTruthPath, blockingMethod, null, similarityMethod, null);
    }        
    
    @Override
    public void runWorkflow() {
        loadData();        
        runBlocking();  
        if (similarityMethod instanceof ProfileWithNeighborMatcher) {
            ((ProfileWithNeighborMatcher) similarityMethod).setGroundTruth(groundTruth);
        }
        runSimilarityComputations();
    }
    
    @Override
    public String toString() {
        return "Workflow: StopAtMetaBlocking\n"
                +super.toString();
    }
    
}
