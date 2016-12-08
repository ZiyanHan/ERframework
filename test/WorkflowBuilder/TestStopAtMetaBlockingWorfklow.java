/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package WorkflowBuilder;

import EntityMatching.ProfileWithNeighborMatcher;
import Utilities.Enumerations.BlockBuildingMethod;
import Utilities.Enumerations.RepresentationModel;

/**
 *
 * @author VASILIS
 */
public class TestStopAtMetaBlockingWorfklow {
    
    public static void main (String[] args) {
    
        final String basePath = "C:\\Users\\VASILIS\\Documents\\OAEI_Datasets\\OAEI2010\\restaurant\\";
        String dataset1 = basePath+"restaurant1Profiles";
        String dataset2 = basePath+"restaurant2Profiles";
        String datasetGroundtruth = basePath+"restaurantIdDuplicates";
        
        //parameters
        final double MATCHING_THRESHOLD = 0.65;
        BlockBuildingMethod blockingWorkflow = BlockBuildingMethod.STANDARD_BLOCKING;
        RepresentationModel repModel = RepresentationModel.CHARACTER_BIGRAM_GRAPHS;   
        ProfileWithNeighborMatcher similarity = new ProfileWithNeighborMatcher(repModel, null, MATCHING_THRESHOLD);

        AbstractWorkflowBuilder full = new StopAtMetaBlocking(
                dataset1, dataset2, datasetGroundtruth, 
                blockingWorkflow, 
                similarity);
                        
        System.out.println(full);
        full.runWorkflow();
    }
    
}
