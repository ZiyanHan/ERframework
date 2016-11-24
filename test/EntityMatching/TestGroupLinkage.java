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

package EntityMatching;

import BlockBuilding.*;
import Utilities.DataStructures.AbstractDuplicatePropagation;
import BlockProcessing.IBlockProcessing;
import Utilities.DataStructures.UnilateralDuplicatePropagation;
import DataModel.AbstractBlock;
import DataModel.EntityProfile;
import DataModel.SimilarityPairs;
import DataReader.EntityReader.IEntityReader;
import DataReader.EntityReader.EntitySerializationReader;
import DataReader.GroundTruthReader.GtSerializationReader;
import DataReader.GroundTruthReader.IGroundTruthReader;
import Utilities.Enumerations.BlockBuildingMethod;
import Utilities.Enumerations.RepresentationModel;

import java.util.List;

/**
 *
 * @author G.A.P. II
 */

public class TestGroupLinkage {
    public static void main(String[] args) {
    	String entitiesFilePath1 = "/home/ethanos/Downloads/JEDAIfiles/im-identity/oaei2014_identity_aPROFILES";
    	//String entitiesFilePath2 = "/home/ethanos/Downloads/JEDAIfiles/im-identity/oaei2014_identity_aPROFILES";//
        String groundTruthFilePath = "/home/ethanos/Downloads/JEDAIfiles/cddbTestDuplicates";
        
        IEntityReader eReader1 = new EntitySerializationReader(entitiesFilePath1);
        List<EntityProfile> profiles1 = eReader1.getEntityProfiles();
        System.out.println("Input Entity Profiles\t:\t" + profiles1.size());
        
//        IEntityReader eReader2 = new EntitySerializationReader(entitiesFilePath2);
//        List<EntityProfile> profiles2 = eReader2.getEntityProfiles();
//        System.out.println("Input Entity Profiles\t:\t" + profiles2.size());
//        
        IGroundTruthReader gtReader = new GtSerializationReader(groundTruthFilePath);
        final AbstractDuplicatePropagation duplicatePropagation = new UnilateralDuplicatePropagation(gtReader.getDuplicatePairs(eReader1.getEntityProfiles()));
        System.out.println("Existing Duplicates\t:\t" + duplicatePropagation.getDuplicates().size());
        
        for (BlockBuildingMethod blbuMethod : BlockBuildingMethod.values()) {
        	if (blbuMethod.equals(BlockBuildingMethod.ATTRIBUTE_CLUSTERING))
        	{

            System.out.println("\n\nCurrent blocking metohd\t:\t" + blbuMethod);
            IBlockBuilding blockBuildingMethod = BlockBuildingMethod.getDefaultConfiguration(blbuMethod);
            List<AbstractBlock> blocks = blockBuildingMethod.getBlocks(profiles1);//
            System.out.println("Original blocks\t:\t" + blocks.size());
            
            IBlockProcessing blockCleaningMethod = BlockBuildingMethod.getDefaultBlockCleaning(blbuMethod);
            if (blockCleaningMethod != null) {
                blocks = blockCleaningMethod.refineBlocks(blocks);
            }
            
            IBlockProcessing comparisonCleaningMethod = BlockBuildingMethod.getDefaultComparisonCleaning(blbuMethod);
            if (comparisonCleaningMethod != null) {
                blocks = comparisonCleaningMethod.refineBlocks(blocks);
            }
            
//            for ( AbstractBlock bl : blocks) {
//            	System.out.println("new");
//            	ComparisonIterator iterator = bl.getComparisonIterator();
//                while (iterator.hasNext()) {
//                	Comparison currentComparison = iterator.next();
//                	System.out.println(currentComparison.getEntityId1()+" "+currentComparison.getEntityId2());
//                }
//            }


   

            long start = System.nanoTime(); 
            for (RepresentationModel model : RepresentationModel.values()) {
            	if (model.equals(RepresentationModel.CHARACTER_BIGRAMS))
            	{

                GroupLinkageWithGraph gp = new GroupLinkageWithGraph(model);
                gp.setSimilarityThreshold(0.1);
                SimilarityPairs simPairs = gp.executeComparisons(blocks, profiles1);//
                //System.out.println();
                for (int i = 0; i < simPairs.getNoOfComparisons(); i++) {
                    //System.out.println(simPairs.getEntityIds1()[i] + "\t\t" + simPairs.getEntityIds2()[i] + "\t\t" + simPairs.getSimilarities()[i]);
                }
            	}
            	
            }
            long elapsedTime = System.nanoTime() - start;
			System.out.println("time="+elapsedTime/1000000000.0);
        	}
        	
        }
    }
}
