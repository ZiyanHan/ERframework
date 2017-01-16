/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package WorkflowBuilder;

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
//        SimilarityPairs simPairs = runSimilarityComputations();
//        runClustering(simPairs);
    }
    
    @Override
    public String toString() {
        return "Workflow: Baseline\n"
                +super.toString();
    }
    
    public static void main (String[] args) {
        //set data
        final String basePath = "/home/gpapadakis/data/";
        String dataset1 = basePath+"rexaProfiles";
        String dataset2 = basePath+"swetodblp_april_2008Profiles";
        String datasetGroundtruth = basePath+"rexa_dblp_goldstandardIdDuplicates";
        
        AbstractWorkflowBuilder baseline = new Baseline(dataset1, dataset2, datasetGroundtruth, null);
        baseline.runWorkflow();
    }
    
}
