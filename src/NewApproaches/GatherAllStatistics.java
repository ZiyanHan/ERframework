package NewApproaches;

import BlockBuilding.IBlockBuilding;
import BlockBuilding.StandardBlocking;
import BlockProcessing.BlockRefinement.BlockFiltering;
import BlockProcessing.BlockRefinement.ComparisonsBasedBlockPurging;
import BlockProcessing.ComparisonRefinement.CardinalityNodePruning;
import BlockProcessing.IBlockProcessing;
import DataModel.AbstractBlock;
import DataModel.Attribute;
import DataModel.EntityProfile;
import DataModel.IdDuplicates;
import DataReader.EntityReader.EntitySerializationReader;
import DataReader.EntityReader.IEntityReader;
import DataReader.GroundTruthReader.GtSerializationReader;
import DataReader.GroundTruthReader.IGroundTruthReader;
import Utilities.BlocksPerformance;
import Utilities.DataStructures.BilateralDuplicatePropagation;
import Utilities.Enumerations.WeightingScheme;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 *
 * @author G.A.P. II
 */

public class GatherAllStatistics {
    
    public static void getNVPairs(List<EntityProfile> profiles) {
        double noOfNVPairs = 0;
        for (EntityProfile eProfile : profiles) {
            for (Attribute attr : eProfile.getAttributes()) {
                noOfNVPairs++;
            }
        }
        System.out.println("Total name-value pairs\t:\t" + noOfNVPairs);
        System.out.println("Average name-value pairs per entity\t:\t" + (noOfNVPairs/profiles.size()));
    }
    
    public static void getTokensPerEntity(List<EntityProfile> profiles) {
        double noOfTokens = 0;
        for (EntityProfile eProfile : profiles) {
            for (Attribute attr : eProfile.getAttributes()) {
                String[] tokens = attr.getValue().split("[\\W_]");
                noOfTokens += tokens.length;
            }
        }
        System.out.println("Total number of tokens\t:\t" + noOfTokens);
        System.out.println("Average number of tokens per entity\t:\t" + (noOfTokens/profiles.size()));
    }
    
    public static void main(String[] args) throws UnsupportedEncodingException, FileNotFoundException, IOException {
        String mainDirectory = "/home/gpapadakis/newData/";
        String[] datasetsPaths = { mainDirectory + "restaurant/",
                                   mainDirectory + "rexa_dblp/",
                                   mainDirectory + "bbcMusic/",
                                   mainDirectory + "yago_imdb/",
        };
        
        String[] d1Datasets = { "restaurant1Profiles",
            "rexaProfiles",
            "bbc-musicNewNoRdfProfiles",
            "yagoProfiles"
        };

        String[] d2Datasets = { "restaurant2Profiles",
            "swetodblp_april_2008Profiles",
            "dbpedia37processedNewNoSameAsNoWikipediaSortedProfiles",
            "imdbProfiles"
        };

        String[] duplicates = {
            "restaurantIdDuplicates",
            "rexa_dblp_goldstandardIdDuplicates",
            "bbc-music_groundTruthUTF8IdDuplicates",
            "imdbgoldFinalIdDuplicates"
        };
        
        for (int datasetIndex = 0; datasetIndex < d1Datasets.length; datasetIndex++) {
            System.out.println("\n\n\n\n\nCurrent dataset\t:\t" + d1Datasets[datasetIndex]);
            
            IEntityReader eReader1 = new EntitySerializationReader(datasetsPaths[datasetIndex]+d1Datasets[datasetIndex]);
            List<EntityProfile> profiles1 = eReader1.getEntityProfiles();
            System.out.println("Input Entity Profiles1\t:\t" + profiles1.size());
            getNVPairs(profiles1);
            getTokensPerEntity(profiles1);
            
            IEntityReader eReader2 = new EntitySerializationReader(datasetsPaths[datasetIndex]+d2Datasets[datasetIndex]);
            List<EntityProfile> profiles2 = eReader2.getEntityProfiles();
            System.out.println("\nInput Entity Profiles2\t:\t" + profiles2.size());
            getNVPairs(profiles2);
            getTokensPerEntity(profiles2);

            IGroundTruthReader gtReader = new GtSerializationReader(datasetsPaths[datasetIndex] + duplicates[datasetIndex]);
            Set<IdDuplicates> duplicatePairs = gtReader.getDuplicatePairs(null);
            System.out.println("Pairs of duplicates\t:\t" + duplicatePairs.size());
            
            IBlockBuilding blockBuilding = new StandardBlocking();
            List<AbstractBlock> blocks = blockBuilding.getBlocks(profiles1, profiles2);

            System.out.println("\n\nToken Blocking Statistics");
            BlocksPerformance blpe = new BlocksPerformance(blocks, new BilateralDuplicatePropagation(duplicatePairs));
            blpe.setStatistics();
            blpe.printStatistics();
            
            IBlockProcessing blockPurging = new ComparisonsBasedBlockPurging();
            blocks = blockPurging.refineBlocks(blocks);
            
            System.out.println("\n\nBlock Purging Statistics");
            blpe = new BlocksPerformance(blocks, new BilateralDuplicatePropagation(duplicatePairs));
            blpe.setStatistics();
            blpe.printStatistics();
        
            IBlockProcessing blockCleaningMethod = new BlockFiltering(0.8);
            blocks = blockCleaningMethod.refineBlocks(blocks);
            System.out.println("Filtered blocks\t:\t" + blocks.size());

            System.out.println("\n\nBlock Filtering Statistics");
            blpe = new BlocksPerformance(blocks, new BilateralDuplicatePropagation(duplicatePairs));
            blpe.setStatistics();
            blpe.printStatistics();
            
            List<AbstractBlock> wjsBlocks = new ArrayList<>(blocks);
            IBlockProcessing cnp = new CardinalityNodePruning(WeightingScheme.WJS);
            wjsBlocks = cnp.refineBlocks(wjsBlocks);
            
            System.out.println("\n\nCNP-WJS Statistics");
            blpe = new BlocksPerformance(wjsBlocks, new BilateralDuplicatePropagation(duplicatePairs));
            blpe.setStatistics();
            blpe.printStatistics();
            
            cnp = new CardinalityNodePruning(WeightingScheme.ARCS);
            blocks = cnp.refineBlocks(blocks);
            
            System.out.println("\n\nCNP-ARCS Statistics");
            blpe = new BlocksPerformance(blocks, new BilateralDuplicatePropagation(duplicatePairs));
            blpe.setStatistics();
            blpe.printStatistics();
        }
    }
}   