/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package WorkflowBuilder;

import BlockProcessing.ComparisonRefinement.CardinalityNodePruning;
import BlockProcessing.IBlockProcessing;
import EntityClustering.CenterClustering;
import EntityClustering.IEntityClustering;
import EntityClustering.UniqueMappingClustering;
import EntityMatching.IEntityMatching;
import EntityMatching.ProfileMatcher;
import Utilities.Enumerations.BlockBuildingMethod;
import Utilities.Enumerations.RepresentationModel;
import Utilities.Enumerations.WeightingScheme;

/**
 *
 * @author VASILIS
 */
public class TestFullWorfklowWithClustering {
    
    public static void main (String[] args) {
    
        final String basePath = "C:\\Users\\VASILIS\\Documents\\OAEI_Datasets\\OAEI2016\\UOBM_small\\";
        String dataset1 = basePath+"Abox1Profiles";
        String dataset2 = basePath+"Abox2Profiles";
        String datasetGroundtruth = basePath+"UOBM_smallIdDuplicates";

        BlockBuildingMethod blockingWorkflow = BlockBuildingMethod.STANDARD_BLOCKING;
        IBlockProcessing metaBlocking = new CardinalityNodePruning(WeightingScheme.CBS);
        RepresentationModel repModel = RepresentationModel.CHARACTER_BIGRAM_GRAPHS;
        IEntityMatching similarity = new ProfileMatcher(repModel);
        IEntityClustering clustering =  new UniqueMappingClustering();
        

        AbstractWorkflowBuilder full = new FullWithClustering(
                dataset1, dataset2, datasetGroundtruth, 
                blockingWorkflow, 
                metaBlocking, 
                similarity, 
                clustering);
                
        System.out.println(full);
        full.runWorkflow();
    }
    
}
