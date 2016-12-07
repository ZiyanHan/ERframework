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
package EntityClustering;

import EntityMatching.*;
import BlockBuilding.*;
import BlockProcessing.ComparisonRefinement.CardinalityNodePruning;
import Utilities.DataStructures.AbstractDuplicatePropagation;
import BlockProcessing.IBlockProcessing;
import DataModel.AbstractBlock;
import DataModel.EntityProfile;
import DataModel.EquivalenceCluster;
import DataModel.SimilarityPairs;
import DataReader.EntityReader.IEntityReader;
import DataReader.EntityReader.EntitySerializationReader;
import DataReader.GroundTruthReader.GtSerializationReader;
import DataReader.GroundTruthReader.IGroundTruthReader;
import Utilities.BlocksPerformance;
import Utilities.ClustersPerformance;
import Utilities.DataStructures.BilateralDuplicatePropagation;
import Utilities.Enumerations.BlockBuildingMethod;
import Utilities.Enumerations.RepresentationModel;
import Utilities.Enumerations.SimilarityMetric;
import Utilities.Enumerations.WeightingScheme;
import java.util.List;

/**
 *
 * @author G.A.P. II
 */
public class TestAllMethods {

    public static void main(String[] args) {
        BlockBuildingMethod blockingWorkflow = BlockBuildingMethod.STANDARD_BLOCKING;
        
        final String basePath = "C:\\Users\\VASILIS\\Documents\\OAEI_Datasets\\OAEI2016\\SPIMBENCH_small\\";
        
        String[] datasetProfiles = {
            basePath+"Abox1Profiles",
            basePath+"Abox2Profiles",
        };
        String datasetGroundtruth = basePath+"SPIMBENCH_smallIdDuplicates";

        IEntityReader eReader1 = new EntitySerializationReader(datasetProfiles[0]);
        List<EntityProfile> profiles1 = eReader1.getEntityProfiles();
        System.out.println("Input Entity Profiles1\t:\t" + profiles1.size());            
        
        IEntityReader eReader2 = new EntitySerializationReader(datasetProfiles[1]);
        List<EntityProfile> profiles2 = eReader2.getEntityProfiles();
        System.out.println("Input Entity Profiles2\t:\t" + profiles2.size());            

        IGroundTruthReader gtReader = new GtSerializationReader(datasetGroundtruth);
        final AbstractDuplicatePropagation duplicatePropagation = new BilateralDuplicatePropagation(gtReader.getDuplicatePairs(profiles1, profiles2));
        System.out.println("Existing Duplicates\t:\t" + duplicatePropagation.getDuplicates().size());

        IBlockBuilding blockBuildingMethod = BlockBuildingMethod.getDefaultConfiguration(blockingWorkflow);
        List<AbstractBlock> blocks = blockBuildingMethod.getBlocks(profiles1, profiles2);
        System.out.println("Original blocks\t:\t" + blocks.size());

        //block filtering
        IBlockProcessing blockCleaningMethod = BlockBuildingMethod.getDefaultBlockCleaning(blockingWorkflow);
        if (blockCleaningMethod != null) {
            blocks = blockCleaningMethod.refineBlocks(blocks);
        }

        IBlockProcessing comparisonCleaningMethod = new CardinalityNodePruning(WeightingScheme.CBS);
        blocks = comparisonCleaningMethod.refineBlocks(blocks);

        BlocksPerformance blp = new BlocksPerformance(blocks, duplicatePropagation);
        blp.setStatistics();
        blp.printStatistics();

        RepresentationModel[] repModels = {
            RepresentationModel.CHARACTER_BIGRAM_GRAPHS,
//                RepresentationModel.CHARACTER_TRIGRAM_GRAPHS,
//                RepresentationModel.CHARACTER_FOURGRAM_GRAPHS
        };

        for (RepresentationModel repModel : repModels) {
            System.out.println("\n\nCurrent model\t:\t" + repModel.toString());
            IEntityMatching em = new ProfileMatcher(repModel, SimilarityMetric.getModelDefaultSimMetric(repModel));
            SimilarityPairs simPairs = em.executeComparisons(blocks, profiles1, profiles2);                                    
            IEntityClustering ec =  
//                        new ConnectedComponentsClustering(); 
                        new CenterClustering();
//                        new MergeCenterClustering();
//                        new MarkovClustering();
            List<EquivalenceCluster> entityClusters = ec.getDuplicates(simPairs);

            ClustersPerformance clp = new ClustersPerformance(entityClusters, duplicatePropagation);
            clp.setStatistics();                
            clp.printStatistics();
        }
        
    }
}
